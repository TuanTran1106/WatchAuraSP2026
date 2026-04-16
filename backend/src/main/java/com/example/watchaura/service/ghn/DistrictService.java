package com.example.watchaura.service.ghn;

import com.example.watchaura.dto.ghn.DistrictDTO;
import com.example.watchaura.dto.ghn.GhnApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DistrictService {
    private final GhnApiClient ghnApiClient;

    public List<DistrictDTO> getDistrictsByProvince(Integer provinceId) {
        if (provinceId == null) return Collections.emptyList();
        try {
            GhnApiResponse<List<DistrictDTO>> resp = ghnApiClient.get(
                    "/master-data/district",
                    "province_id",
                    provinceId,
                    new ParameterizedTypeReference<>() {}
            );
            if (resp == null || resp.getData() == null) {
                return Collections.emptyList();
            }
            return resp.getData();
        } catch (HttpClientErrorException.Unauthorized e) {
            // Token/ShopId sai hoặc hết hạn → không làm sập trang
            return Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}

