package com.example.watchaura.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoaDonSerialSelectionDTO {

    private Integer hoaDonId;
    private String maDonHang;
    private String trangThaiDonHang;
    private String loaiHoaDon; // OFFLINE hoặc ONLINE - để biết có phải đơn tại quầy không
    private List<BienTheSerialGroup> bienTheGroups;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BienTheSerialGroup {
        private Integer hoaDonChiTietId;
        private Integer sanPhamChiTietId;
        private String tenSanPham;
        private String tenBienThe;
        private Integer soLuongMua;
        private Integer soLuongDaChon;
        private List<SerialSelectionDTO> serials;
    }
}
