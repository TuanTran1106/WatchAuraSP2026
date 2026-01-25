package com.example.watchaura.dto;

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
    private String sdtNguoiNhan;
    private String diaChiCuThe;
    private String phuongXa;
    private String quanHuyen;
    private String tinhThanh;
    private String ghiChu;
}
