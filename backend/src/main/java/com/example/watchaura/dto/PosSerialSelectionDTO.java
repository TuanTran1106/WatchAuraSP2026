package com.example.watchaura.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PosSerialSelectionDTO {

    private Integer hoaDonId;
    private String maDonHang;

    // Danh sách serial cần chọn cho từng sản phẩm trong giỏ
    private List<SerialGroupItem> serialGroups;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SerialGroupItem {
        private Integer hoaDonChiTietId;
        private Integer sanPhamChiTietId;
        private String tenSanPham;
        private String tenBienThe;
        private Integer soLuongMua;
        private Integer soLuongDaChon;
        private List<SerialItem> serials;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SerialItem {
        private Integer id;
        private String maSerial;
        private Boolean daChon;
        private LocalDateTime ngayTao;
    }
}
