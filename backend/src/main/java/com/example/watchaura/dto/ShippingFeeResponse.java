package com.example.watchaura.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ShippingFeeResponse {
    private long shippingFee;
    private String currency;
    private String provider;
    private boolean fallbackApplied;
    private String providerErrorCode;
    private String message;
}
