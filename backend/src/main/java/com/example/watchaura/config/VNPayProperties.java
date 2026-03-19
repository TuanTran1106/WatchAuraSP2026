//package com.example.watchaura.config;
//
//import lombok.Getter;
//import lombok.Setter;
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//@ConfigurationProperties(prefix = "vnpay")
//@Getter
//@Setter
//public class VNPayProperties {
//
//    /** Mã website/merchant (TMN Code) từ VN Pay */
//    private String tmnCode = "";
//    /** Secret key dùng để ký HMAC (Hash Secret) */
//    private String hashSecret = "";
//    /** URL cổng thanh toán (sandbox: https://sandbox.vnpayment.vn/paymentv2/vpcpay.html) */
//    private String url = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
//}
package com.example.watchaura.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "vnpay")
public class VNPayProperties {
    /** Mã merchant — lấy từ portal VNPay sandbox */
    private String tmnCode;

    /** Chuỗi bí mật ký HMAC-SHA512 — lấy từ portal VNPay sandbox */
    private String hashSecret;

    /** URL cổng thanh toán VNPay */
    private String payUrl;
}
