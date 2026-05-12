package com.example.watchaura.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartResponse {

    private List<CartItemResponse> items;

    private BigDecimal tamTinh;

    private BigDecimal giamGia;

    private BigDecimal tongThanhToan;

    private String tenKhachHang;

    private String sdtKhachHang;

    private String emailKhachHang;

    /** Map: hoaDonChiTietId -> danh sách mã serial đã gán */
    private List<SerialInfo> serials;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItemResponse {
        private Integer id;
        private Integer soLuong;
        private BigDecimal donGia;
        private String tenSanPham;
        private String hinhAnh;
        private MauSacInfo mauSac;
        private KichThuocInfo kichThuoc;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MauSacInfo {
        private Integer id;
        private String tenMauSac;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KichThuocInfo {
        private Integer id;
        private String tenKichThuoc;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SerialInfo {
        private Integer hoaDonChiTietId;
        private List<String> maSerials;
    }
}
