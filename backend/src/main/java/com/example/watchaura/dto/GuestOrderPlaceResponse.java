package com.example.watchaura.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class GuestOrderPlaceResponse {
    private Integer orderId;
    private String orderCode;
    private String trackingToken;
    private String message;
}
