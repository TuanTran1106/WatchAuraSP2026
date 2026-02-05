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
public class SanPhamDTO {

    private Integer id;
    private String maSanPham;
    private String tenSanPham;
    private String moTa;
    private String hinhAnh;
    private Integer idThuongHieu;
    private String tenThuongHieu;
    private Integer idDanhMuc;
    private String tenDanhMuc;
    private String phongCach;
    private Boolean trangThai;
    /** Giá bán (thấp nhất trong các biến thể), null nếu chưa có chi tiết */
    private BigDecimal giaBan;
    /** Tổng số lượng tồn của tất cả biến thể */
    private Integer soLuongTon;

    private Integer idMauSac;
    private Integer idKichThuoc;
    private Integer idChatLieuDay;
    private Integer idLoaiMay;

    private Double duongKinh;
    private Integer doChiuNuoc;
    private Double beRongDay;
    private Double trongLuong;
}