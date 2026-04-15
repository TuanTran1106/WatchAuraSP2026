package com.example.watchaura.service.ghn;

import com.example.watchaura.dto.ghn.GhnApiResponse;
import com.example.watchaura.dto.ghn.WardDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WardService {
    private final GhnApiClient ghnApiClient;

    public List<WardDTO> getWardsByDistrict(Integer districtId) {
        if (districtId == null) return Collections.emptyList();
        try {
            GhnApiResponse<List<WardDTO>> resp = ghnApiClient.get(
                    "/master-data/ward",
                    "district_id",
                    districtId,
                    new ParameterizedTypeReference<>() {}
            );
            if (resp == null || resp.getData() == null) {
                return Collections.emptyList();
            }
            return resp.getData();
        } catch (HttpClientErrorException.Unauthorized e) {
            return Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}

