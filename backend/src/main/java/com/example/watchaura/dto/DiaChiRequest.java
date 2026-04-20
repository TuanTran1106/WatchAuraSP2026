package com.example.watchaura.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiaChiRequest {

    @NotBlank(message = "Họ tên người nhận không được để trống")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    private String tenNguoiNhan;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^(0[35789])([0-9]{8})$", message = "Số điện thoại không hợp lệ")
    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    private String sdtNguoiNhan;

    @NotBlank(message = "Địa chỉ cụ thể không được để trống")
    @Size(max = 255, message = "Địa chỉ cụ thể tối đa 255 ký tự")
    private String diaChiCuThe;

    @Size(max = 100, message = "Phường/xã tối đa 100 ký tự")
    private String phuongXa;

    @Size(max = 100, message = "Quận/huyện tối đa 100 ký tự")
    private String quanHuyen;

    @NotBlank(message = "Tỉnh/thành phố không được để trống")
    @Size(max = 100, message = "Tỉnh/thành phố tối đa 100 ký tự")
    private String tinhThanh;

    /** Optional GHN codes for auto shipping fee */
    private Integer ghnProvinceId;
    private Integer ghnDistrictId;
    private String ghnWardCode;
}
