package com.example.watchaura.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class GuestOrderTrackResponse {
    private String orderCode;
    private String orderStatus;
    private String customerName;
    private String phoneMasked;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
}
