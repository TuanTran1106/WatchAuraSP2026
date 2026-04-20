package com.example.watchaura.dto;

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
public class DiaChiGiaoHangRequest {
    private String tenNguoiNhan;

    @Pattern(regexp = "^(0[3|5|7|8|9])([0-9]{8})$", message = "Số điện thoại không hợp lệ")
    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    private String sdtNguoiNhan;

    private String diaChiCuThe;
    private String phuongXa;
    private String quanHuyen;
    private String tinhThanh;
    private String ghiChu;
}
