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

    /** Legacy service config (không nên fix cứng; giữ lại để tương thích cấu hình cũ) */
    private Integer serviceId = 53320;
    private Integer weight = 500;
    private Integer length = 20;
    private Integer width = 15;
    private Integer height = 10;

    /** Miễn phí ship khi đơn >= ngưỡng (VND) */
    private BigDecimal freeShippingThreshold = new BigDecimal("2000000");
}

