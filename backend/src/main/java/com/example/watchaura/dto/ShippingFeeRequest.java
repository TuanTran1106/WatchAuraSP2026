package com.example.watchaura.dto;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ShippingFeeRequest {
    private Integer addressId;
    private Integer toDistrictId;
    private String toWardCode;

    @Min(value = 0, message = "weightGram không hợp lệ")
    private Integer weightGram;

    @Min(value = 0, message = "lengthCm không hợp lệ")
    private Integer lengthCm;

    @Min(value = 0, message = "widthCm không hợp lệ")
    private Integer widthCm;

    @Min(value = 0, message = "heightCm không hợp lệ")
    private Integer heightCm;

    @Min(value = 0, message = "insuranceValue không hợp lệ")
    private Long insuranceValue;
}
