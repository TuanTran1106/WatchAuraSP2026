package com.example.watchaura.service.ghn;

import com.example.watchaura.config.GhnProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class GhnApiClient {
    private final RestTemplate restTemplate;
    private final GhnProperties ghnProperties;

    private HttpHeaders ghnHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Token", ghnProperties.getToken());
        if (ghnProperties.getShopId() != null) {
            headers.set("ShopId", String.valueOf(ghnProperties.getShopId()));
        }
        return headers;
    }

    public <T> T get(String path, ParameterizedTypeReference<T> type) throws RestClientException {
        String baseUrl = ghnProperties.getBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("Missing GHN baseUrl");
        }
        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .path(path)
                .toUriString();
        ResponseEntity<T> resp = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(ghnHeaders()), type);
        return resp.getBody();
    }

    public <T> T get(String path, String queryName, Object queryValue, ParameterizedTypeReference<T> type) throws RestClientException {
        String baseUrl = ghnProperties.getBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("Missing GHN baseUrl");
        }
        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .path(path)
                .queryParam(queryName, queryValue)
                .toUriString();
        ResponseEntity<T> resp = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(ghnHeaders()), type);
        return resp.getBody();
    }

    public <TReq, TResp> TResp post(String path, TReq body, ParameterizedTypeReference<TResp> type) throws RestClientException {
        String baseUrl = ghnProperties.getBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            throw new IllegalStateException("Missing GHN baseUrl");
        }
        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .path(path)
                .toUriString();
        ResponseEntity<TResp> resp = restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, ghnHeaders()), type);
        return resp.getBody();
    }
}

