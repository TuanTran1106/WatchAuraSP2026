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
    /** URL redirect VnPay (chỉ set khi chọn thanh toán VnPay) */
    private String redirectUrl;
    /** true nếu cần redirect sang VnPay */
    private Boolean needVnPayRedirect;
}
