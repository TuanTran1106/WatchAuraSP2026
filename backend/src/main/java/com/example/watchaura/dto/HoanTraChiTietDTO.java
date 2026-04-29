package com.example.watchaura.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoanTraChiTietDTO {

    private Integer id;
    private Integer idHoanTra;
    private Integer idSanPhamChiTiet;
    private String tenSanPham;
    private String maSanPham;
    private String mauSac;
    private String kichThuoc;
    private String chatLieuDay;
    private String tenBienThe;
    private Integer idHoaDonChiTiet;
    private Integer soLuongHoanTra;
    private Integer soLuongDaHoanTra;
    private Integer soLuongConLai;
    private BigDecimal donGiaTaiThoiDiemMua;
    private BigDecimal soTienHoan;
    private String hinhAnh;
    private List<String> serialsHoanTra;
    private List<SerialInfo> serialsChiTiet;

    // === DOI_HANG: Serial mới thay thế ===
    // Serial mới được cấp khi đổi hàng
    private String serialMoi;

    // Trạng thái serial cũ:
    // - true: serial cũ bị lỗi
    // - false: serial mới được đổi
    private Boolean serialCuLoi;
    private String serialCuLoiHienThi;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SerialInfo {
        private String maSerial;
        private String trangThai;
        private String trangThaiHienThi;
        private Boolean daDuocChon;
    }
}
