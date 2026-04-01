package com.example.watchaura.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "vnpay")
@Getter
@Setter
public class VNPayProperties {

    /** Mã website/merchant (TMN Code) từ VN Pay */
    private String tmnCode = "";

    /** Secret key dùng để ký HMAC (Hash Secret) */
    private String hashSecret = "";

    /** URL cổng thanh toán (sandbox: https://sandbox.vnpayment.vn/paymentv2/vpcpay.html) */
    private String url = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";

    /** URL redirect sau khi thanh toán (VN Pay sẽ redirect về URL này) */
    private String returnUrl = "http://localhost:8080/thanh-toan/vnpay/return";
}
