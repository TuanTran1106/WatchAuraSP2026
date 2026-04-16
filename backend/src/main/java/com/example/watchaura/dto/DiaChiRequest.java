package com.example.watchaura.dto;

import jakarta.validation.constraints.NotBlank;
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
}
