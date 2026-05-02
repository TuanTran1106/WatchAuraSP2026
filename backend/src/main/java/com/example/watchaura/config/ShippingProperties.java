package com.example.watchaura.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "shipping.ghn")
@Getter
@Setter
public class ShippingProperties {

    private String baseUrl = "https://online-gateway.ghn.vn";
    private String token = "";
    private Integer shopId = 0;
    private Integer warehouseDistrictId = 0;
    private String warehouseWardCode = "";
    private Long fallbackFee = 35000L;
    private Integer defaultWeightGram = 500;
    private Integer defaultLengthCm = 20;
    private Integer defaultWidthCm = 15;
    private Integer defaultHeightCm = 10;
}
