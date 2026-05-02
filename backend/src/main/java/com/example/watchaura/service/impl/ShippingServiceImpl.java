package com.example.watchaura.service.impl;

import com.example.watchaura.config.ShippingProperties;
import com.example.watchaura.dto.ShippingFeeRequest;
import com.example.watchaura.dto.ShippingFeeResponse;
import com.example.watchaura.dto.ShippingLocationOption;
import com.example.watchaura.entity.DiaChi;
import com.example.watchaura.repository.DiaChiRepository;
import com.example.watchaura.service.ShippingService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class ShippingServiceImpl implements ShippingService {

    private final DiaChiRepository diaChiRepository;
    private final ShippingProperties shippingProperties;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public ShippingFeeResponse calculateFee(ShippingFeeRequest request, Integer khachHangId) {
        Destination destination = resolveDestination(request, khachHangId);
        return doCalculateFee(request, destination);
    }

    @Override
    public ShippingFeeResponse calculateGuestFee(ShippingFeeRequest request) {
        Destination destination = resolveDestination(request, null);
        return doCalculateFee(request, destination);
    }

    private ShippingFeeResponse doCalculateFee(ShippingFeeRequest request, Destination destination) {
        if (shippingProperties.getToken() == null || shippingProperties.getToken().isBlank()) {
            return fallback("missing_provider_token", "Thiếu GHN token, đang dùng phí tạm.");
        }
        if (shippingProperties.getShopId() == null || shippingProperties.getShopId() <= 0) {
            return fallback("missing_provider_shop_id", "Thiếu GHN shop id, đang dùng phí tạm.");
        }

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("service_type_id", 2);
        body.put("from_district_id", shippingProperties.getWarehouseDistrictId());
        body.put("from_ward_code", shippingProperties.getWarehouseWardCode());
        body.put("to_district_id", destination.toDistrictId());
        body.put("to_ward_code", destination.toWardCode());
        body.put("weight", safePositive(request.getWeightGram(), shippingProperties.getDefaultWeightGram()));
        body.put("length", safePositive(request.getLengthCm(), shippingProperties.getDefaultLengthCm()));
        body.put("width", safePositive(request.getWidthCm(), shippingProperties.getDefaultWidthCm()));
        body.put("height", safePositive(request.getHeightCm(), shippingProperties.getDefaultHeightCm()));
        body.put("insurance_value", request.getInsuranceValue() != null && request.getInsuranceValue() > 0 ? request.getInsuranceValue() : 0);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", shippingProperties.getToken());
        headers.set("ShopId", String.valueOf(shippingProperties.getShopId()));

        String url = shippingProperties.getBaseUrl() + "/shiip/public-api/v2/shipping-order/fee";
        try {
            ResponseEntity<JsonNode> response = restTemplate.postForEntity(
                    url,
                    new HttpEntity<>(body, headers),
                    JsonNode.class
            );

            JsonNode root = response.getBody();
            int code = root != null && root.has("code") ? root.get("code").asInt(-1) : -1;
            long fee = root != null ? root.path("data").path("total").asLong(-1) : -1;
            if (code != 200 || fee < 0) {
                return fallback("provider_non_200", "Không lấy được phí từ GHN, đang dùng phí tạm.");
            }

            return new ShippingFeeResponse(
                    fee,
                    "VND",
                    "GHN",
                    false,
                    null,
                    "success"
            );
        } catch (RestClientException ex) {
            return fallback("provider_connection_error", "Kết nối GHN thất bại, đang dùng phí tạm.");
        } catch (Exception ex) {
            return fallback("unexpected_error", "Lỗi không xác định, đang dùng phí tạm.");
        }
    }

    @Override
    public List<ShippingLocationOption> getProvinces() {
        JsonNode data = callMasterData("/shiip/public-api/master-data/province", null);
        return mapLocationOptions(data, "ProvinceID", "ProvinceName");
    }

    @Override
    public List<ShippingLocationOption> getDistricts(Integer provinceId) {
        if (provinceId == null || provinceId <= 0) {
            throw new IllegalArgumentException("provinceId không hợp lệ.");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("province_id", provinceId);
        JsonNode data = callMasterData("/shiip/public-api/master-data/district", body);
        return mapLocationOptions(data, "DistrictID", "DistrictName");
    }

    @Override
    public List<ShippingLocationOption> getWards(Integer districtId) {
        if (districtId == null || districtId <= 0) {
            throw new IllegalArgumentException("districtId không hợp lệ.");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("district_id", districtId);
        JsonNode data = callMasterData("/shiip/public-api/master-data/ward", body);
        return mapLocationOptions(data, "WardCode", "WardName");
    }

    private JsonNode callMasterData(String path, Map<String, Object> body) {
        if (shippingProperties.getToken() == null || shippingProperties.getToken().isBlank()) {
            throw new IllegalStateException("Thiếu GHN token.");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", shippingProperties.getToken());

        HttpEntity<?> entity = body == null ? new HttpEntity<>(headers) : new HttpEntity<>(body, headers);
        String url = shippingProperties.getBaseUrl() + path;
        ResponseEntity<JsonNode> response = restTemplate.postForEntity(url, entity, JsonNode.class);
        JsonNode root = response.getBody();
        if (root == null) {
            throw new IllegalStateException("GHN master-data không có dữ liệu.");
        }
        int code = root != null && root.has("code") ? root.get("code").asInt(-1) : -1;
        if (code != 200) {
            throw new IllegalStateException("GHN master-data trả về không hợp lệ.");
        }
        return root.path("data");
    }

    private List<ShippingLocationOption> mapLocationOptions(JsonNode data, String idKey, String nameKey) {
        if (data == null || !data.isArray()) {
            return List.of();
        }
        return StreamSupport.stream(data.spliterator(), false)
                .map(node -> new ShippingLocationOption(
                        node.path(idKey).asText(""),
                        node.path(nameKey).asText("")
                ))
                .filter(x -> x.getId() != null && !x.getId().isBlank() && x.getName() != null && !x.getName().isBlank())
                .toList();
    }

    private Destination resolveDestination(ShippingFeeRequest request, Integer khachHangId) {
        if (request.getAddressId() != null) {
            DiaChi diaChi = diaChiRepository.findByIdAndKhachHangIdAndIsDeletedFalse(request.getAddressId(), khachHangId)
                    .orElseThrow(() -> new IllegalArgumentException("Địa chỉ không tồn tại hoặc không thuộc tài khoản hiện tại."));

            if (diaChi.getGhnDistrictId() == null || diaChi.getGhnWardCode() == null || diaChi.getGhnWardCode().isBlank()) {
                throw new IllegalArgumentException("Địa chỉ chưa có mã GHN district/ward hợp lệ.");
            }
            return new Destination(diaChi.getGhnDistrictId(), diaChi.getGhnWardCode().trim());
        }

        if (request.getToDistrictId() == null || request.getToWardCode() == null || request.getToWardCode().isBlank()) {
            throw new IllegalArgumentException("Thiếu toDistrictId/toWardCode để tính phí vận chuyển.");
        }
        return new Destination(request.getToDistrictId(), request.getToWardCode().trim());
    }

    private int safePositive(Integer value, Integer fallback) {
        if (value == null || value <= 0) {
            return fallback != null && fallback > 0 ? fallback : 1;
        }
        return value;
    }

    private ShippingFeeResponse fallback(String errorCode, String message) {
        return new ShippingFeeResponse(
                shippingProperties.getFallbackFee() != null ? shippingProperties.getFallbackFee() : 35000L,
                "VND",
                "GHN",
                true,
                errorCode,
                message
        );
    }

    private record Destination(Integer toDistrictId, String toWardCode) {
    }
}
