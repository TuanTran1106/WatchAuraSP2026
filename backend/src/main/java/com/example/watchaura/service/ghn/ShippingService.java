package com.example.watchaura.service.ghn;

import com.example.watchaura.config.GhnProperties;
import com.example.watchaura.dto.ghn.GhnAvailableServicesRequest;
import com.example.watchaura.dto.ghn.GhnApiResponse;
import com.example.watchaura.dto.ghn.GhnFeeDataDTO;
import com.example.watchaura.dto.ghn.GhnFeeRequest;
import com.example.watchaura.dto.ghn.GhnServiceDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShippingService {
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
        // Ưu tiên serviceId cấu hình nếu còn dùng được
        Integer configured = ghnProperties.getServiceId();
        List<GhnServiceDTO> services = getAvailableServices(toDistrictId);
        if (services == null || services.isEmpty()) return null;
        if (configured != null) {
            boolean ok = services.stream().anyMatch(s -> s != null && configured.equals(s.getServiceId()));
            if (ok) return configured;
        }
        return services.get(0) != null ? services.get(0).getServiceId() : null;
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

