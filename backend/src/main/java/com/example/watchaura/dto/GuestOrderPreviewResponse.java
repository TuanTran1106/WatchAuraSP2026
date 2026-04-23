package com.example.watchaura.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class GuestOrderPreviewResponse {
    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal total;
    private String currency;
    private String provider;
    private boolean fallbackApplied;
    private String providerErrorCode;
    private String message;
    private int totalItems;
}
