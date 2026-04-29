package com.example.watchaura.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhieuNhapKhoDTO {

    private Integer id;
    private String maPhieu;
    private String loaiPhieu;
    private String loaiPhieuHienThi;
    private Integer idHoanTra;
    private String maHoanTra;
    private Integer idNhanVien;
    private String tenNhanVien;
    private BigDecimal soTienHoan;
    private String soTienHoanFormatted;
    private String phuongThucHoanTien;
    private String phuongThucHoanTienHienThi;
    private String ghiChu;
    private LocalDateTime ngayTao;
    private String ngayTaoFormatted;
    private List<ChiTietPhieuNhapKhoDTO> chiTietList;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ChiTietPhieuNhapKhoDTO {
        private Integer id;
        private Integer idPhieuNhapKho;
        private Integer idSanPhamChiTiet;
        private String tenSanPham;
        private String maSanPham;
        private String tenBienThe;
        private String mauSac;
        private String kichThuoc;
        private String chatLieuDay;
        private Integer idSerial;
        private String maSerial;
        private Integer soLuong;
        private BigDecimal donGia;
        private String ghiChu;
    }
}
