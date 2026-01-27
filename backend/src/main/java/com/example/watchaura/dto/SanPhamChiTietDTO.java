package com.example.watchaura.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamChiTietDTO {
    private Integer id;
    private Integer idSanPham;
    private String tenSanPham;
    private String maSanPham;
    private Integer idMauSac;
    private String tenMauSac;
    private Integer idKichThuoc;
    private String tenKichThuoc;
    private Integer idChatLieuDay;
    private String tenChatLieuDay;
    private Integer idLoaiMay;
    private String tenLoaiMay;
    private Integer soLuongTon;
    private BigDecimal giaBan;
    private Double duongKinh;
    private Integer doChiuNuoc;
    private Double beRongDay;
    private Double trongLuong;
    private Boolean trangThai;
}