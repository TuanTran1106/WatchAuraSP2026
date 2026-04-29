package com.example.watchaura.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SerialCheckResponse {

    private boolean valid;
    private String maSerial;
    private String errorMessage;
    
    private Integer serialId;
    private String trangThai;
    private String trangThaiHienThi;
    private Integer trangThaiCode;
    
    private Integer hoaDonId;
    private String maDonHang;
    private String tenSanPham;
    private String maSanPham;
    private String tenBienThe;
    private BigDecimal donGia;
    private Integer sanPhamChiTietId;
    private Integer hoaDonChiTietId;
    private Integer hoanTraId;
    private String maHoanTra;
}
