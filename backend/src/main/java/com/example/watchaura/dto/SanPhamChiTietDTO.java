package com.example.watchaura.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

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
    private Integer soLuongDaDat;
    private Integer soLuongKhaDung;
    private BigDecimal giaBan;
    private Double duongKinh;
    private Integer doChiuNuoc;
    private Double beRongDay;
    private Double trongLuong;
    private Boolean trangThai;
    private String hinhAnh;
    private Integer soLuongSerialTrongKho;
    private List<String> serials;
}