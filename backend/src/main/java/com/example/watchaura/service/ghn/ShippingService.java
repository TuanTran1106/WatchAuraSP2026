package com.example.watchaura.service.ghn;

import com.example.watchaura.config.GhnProperties;
import com.example.watchaura.dto.ghn.GhnAvailableServicesRequest;
import com.example.watchaura.dto.ghn.GhnApiResponse;
import com.example.watchaura.dto.ghn.GhnFeeDataDTO;
import com.example.watchaura.dto.ghn.GhnFeeRequest;
import com.example.watchaura.dto.ghn.GhnServiceDTO;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ShippingService {
    private static final Logger log = LoggerFactory.getLogger(ShippingService.class);

    private final GhnApiClient ghnApiClient;
    private final GhnProperties ghnProperties;

    public BigDecimal calculateShippingFee(BigDecimal merchandiseSubtotal, Integer toDistrictId, String toWardCode) {
        BigDecimal sub = merchandiseSubtotal != null ? merchandiseSubtotal : BigDecimal.ZERO;
        BigDecimal threshold = ghnProperties.getFreeShippingThreshold() != null
                ? ghnProperties.getFreeShippingThreshold()
                : new BigDecimal("2000000");
        if (sub.compareTo(threshold) >= 0) {
            return BigDecimal.ZERO;
        }
        return calculateShippingFee(toDistrictId, toWardCode);
    }

    public BigDecimal calculateShippingFee(Integer toDistrictId, String toWardCode) {
        if (toDistrictId == null || toWardCode == null || toWardCode.isBlank()) {
            return BigDecimal.ZERO;
        }

        Integer serviceId = resolveServiceId(toDistrictId);
        log.info("[GHN] toDistrictId={} -> serviceId={}", toDistrictId, serviceId);
        if (serviceId == null) {
            throw new IllegalStateException("Không tìm thấy dịch vụ vận chuyển phù hợp (GHN)");
        }

        GhnFeeRequest req = new GhnFeeRequest();
        req.setFromDistrictId(ghnProperties.getFromDistrictId());
        req.setFromWardCode(ghnProperties.getFromWardCode());
        req.setToDistrictId(toDistrictId);
        req.setToWardCode(toWardCode.trim());
        req.setServiceId(serviceId);
        req.setWeight(ghnProperties.getWeight());
        req.setLength(ghnProperties.getLength());
        req.setWidth(ghnProperties.getWidth());
        req.setHeight(ghnProperties.getHeight());

        GhnApiResponse<GhnFeeDataDTO> resp = ghnApiClient.post(
                "/v2/shipping-order/fee",
                req,
                new ParameterizedTypeReference<>() {}
        );
        if (resp == null || resp.getData() == null || resp.getData().getTotal() == null) {
            throw new IllegalStateException("GHN response missing data.total");
        }
        return BigDecimal.valueOf(resp.getData().getTotal());
    }

    private Integer resolveServiceId(Integer toDistrictId) {
        List<GhnServiceDTO> services = getAvailableServices(toDistrictId);
        if (services == null || services.isEmpty()) {
            return null;
        }

        // Chọn service nhất quán theo loại dịch vụ: ưu tiên "Tiêu chuẩn".
        Integer standardServiceId = services.stream()
        .filter(s -> s != null && s.getServiceId() != null)
        .filter(s -> isStandardService(s.getShortName()))
        .map(GhnServiceDTO::getServiceId)
        .findFirst()
        .orElse(null);
    
    if (standardServiceId != null) {
        return standardServiceId;
    }

        // Fallback ổn định: nếu không có "Tiêu chuẩn", lấy service_id nhỏ nhất.
        return services.stream()
    .filter(s -> s != null && s.getServiceId() != null)
    .map(GhnServiceDTO::getServiceId)
    .findFirst()
    .orElse(null);
    }

    private boolean isStandardService(String shortName) {
        if (shortName == null || shortName.isBlank()) {
            return false;
        }
        String normalized = shortName.trim().toLowerCase(Locale.ROOT);
        return normalized.contains("tiêu chuẩn")
                || normalized.contains("tieu chuan")
                || normalized.contains("standard");
    }

    private List<GhnServiceDTO> getAvailableServices(Integer toDistrictId) {
        GhnAvailableServicesRequest req = new GhnAvailableServicesRequest();
        req.setShopId(ghnProperties.getShopId());
        req.setFromDistrict(ghnProperties.getFromDistrictId());
        req.setToDistrict(toDistrictId);

        GhnApiResponse<List<GhnServiceDTO>> resp = ghnApiClient.post(
                "/v2/shipping-order/available-services",
                req,
                new ParameterizedTypeReference<>() {}
        );
        return resp != null ? resp.getData() : null;
    }
}

