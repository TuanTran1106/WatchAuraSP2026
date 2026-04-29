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

    // === DOI_HANG specific fields ===
    // Trạng thái cho biết serial cũ bị lỗi hay serial mới được đổi
    private Boolean serialCuLoi;
    private String serialCuLoiHienThi;
    // Danh sách serial mới được đổi (cho từng chi tiết)
    private List<String> serialsMoiList;
}
