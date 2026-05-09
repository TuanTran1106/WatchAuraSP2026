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
public class HoanTraDTO {

    private Integer id;
    private String maHoanTra;
    private Integer idHoaDon;
    private String maDonHang;
    private Integer idKhachHang;
    private String tenKhachHang;
    private String sdtKhachHang;
    private Integer idNhanVienXuLy;
    private String tenNhanVienXuLy;
    private String lyDo;
    private BigDecimal soTienHoanTra;
    private String trangThai;
    private String trangThaiHienThi;
    private String ghiChuXuLy;
    private LocalDateTime ngayYeuCau;
    private LocalDateTime ngayXuLy;
    private String loaiHoanTra;
    private String loaiHoanTraHienThi;
    private java.util.List<HoanTraChiTietDTO> chiTietList;

    // Refund fields
    private String phuongThucHoanTien;
    private String phuongThucHoanTienHienThi;
    private BigDecimal soTienHoanThucTe;
    private String ghiChuHoanTien;
    private LocalDateTime ngayHoanTien;
    private Integer phieuNhapKhoId;
    private String maPhieuNhapKho;

    // Thông tin tài khoản ngân hàng để hoàn tiền
    private String soTaiKhoan;
    private String tenNganHang;
    private String tenChuTaiKhoan;

    
    // Thông tin hóa đơn gốc cho hoàn trả
    private BigDecimal tongTienHoaDon;  // Tổng tiền hàng (tổng tiền sản phẩm + voucher đã áp, khuyến mãi)
    private BigDecimal phiGiaoHang;     // Phí giao hàng (sẽ trừ khi hoàn)
    private BigDecimal voucherGiam;      // Số tiền voucher đã giảm
}
