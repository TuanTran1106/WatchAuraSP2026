package com.example.watchaura.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HoaDonDTO {
    private Integer id;
    private String maDonHang;
    private Integer khachHangId;
    private String tenKhachHang;
    private Integer nhanVienId;
    private Integer voucherId;
    private String maVoucher;
    private BigDecimal tongTienTamTinh;
    private BigDecimal tienGiam;
    private BigDecimal tongTienThanhToan;
    private String phuongThucThanhToan;
    private String loaiHoaDon;
    private Boolean trangThai;
    private String trangThaiDonHang;
    private LocalDateTime ngayDat;
    private String diaChi;
    private String sdtKhachHang;
    private String ghiChu;
    private DiaChiGiaoHangDTO diaChiGiaoHang;
    private List<HoaDonChiTietDTO> items;
}
