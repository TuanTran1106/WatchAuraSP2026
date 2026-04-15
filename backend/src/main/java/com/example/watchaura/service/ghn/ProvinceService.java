package com.example.watchaura.service.ghn;

import com.example.watchaura.dto.ghn.GhnApiResponse;
import com.example.watchaura.dto.ghn.ProvinceDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProvinceService {
    private final GhnApiClient ghnApiClient;

    public List<ProvinceDTO> getProvinces() {
        try {
            GhnApiResponse<List<ProvinceDTO>> resp = ghnApiClient.get(
                    "/master-data/province",
                    new ParameterizedTypeReference<>() {}
            );
            if (resp == null || resp.getData() == null) return Collections.emptyList();
            return resp.getData();
        } catch (HttpClientErrorException.Unauthorized e) {
            return Collections.emptyList();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}

