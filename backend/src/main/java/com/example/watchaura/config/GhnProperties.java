package com.example.watchaura.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ghn")
public class GhnProperties {
    /**
     * Base URL (test): https://dev-online-gateway.ghn.vn/shiip/public-api
     */
    private String baseUrl = "https://dev-online-gateway.ghn.vn/shiip/public-api";

    /** Header: Token */
    private String token;

    /** Header: ShopId */
    private Integer shopId;

    /** Default from location (shop) */
    private Integer fromDistrictId = 1454;
    private String fromWardCode = "21211";

    /** Default service config */
    private Integer serviceId = 53320;
    private Integer weight = 500;

    /** Miễn phí ship khi đơn >= ngưỡng (VND) */
    private BigDecimal freeShippingThreshold = new BigDecimal("2000000");
}

