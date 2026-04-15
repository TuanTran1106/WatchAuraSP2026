package com.example.watchaura.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AddressDefaultResponse {
    private Integer addressId;
    private String tenNguoiNhan;
    private String sdtNguoiNhan;
    private String diaChiTomTat;

    private Integer ghnProvinceId;
    private Integer ghnDistrictId;
    private String ghnWardCode;
}

