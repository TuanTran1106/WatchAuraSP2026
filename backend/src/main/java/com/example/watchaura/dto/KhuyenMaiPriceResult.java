package com.example.watchaura.dto;

import java.math.BigDecimal;

/**
 * Kết quả áp khuyến mãi sản phẩm (theo giá niêm yết của biến thể).
 */
public record KhuyenMaiPriceResult(
        BigDecimal giaGoc,
        BigDecimal giaSauGiam,
        BigDecimal soTienGiam,
        BigDecimal phanTramHienThi,
        boolean coKhuyenMai,
        String tenChuongTrinh,
        LoaiGiamApDung loaiGiamApDung
) {
    /**
     * Dạng giảm đang áp dụng (để UI không nhầm % với số tiền).
     */
    public enum LoaiGiamApDung {
        PHAN_TRAM,
        TIEN,
        KHONG
    }

    public static KhuyenMaiPriceResult none(BigDecimal giaGoc) {
        BigDecimal g = giaGoc != null ? giaGoc : BigDecimal.ZERO;
        return new KhuyenMaiPriceResult(
                g, g, BigDecimal.ZERO, BigDecimal.ZERO, false, null, LoaiGiamApDung.KHONG);
    }
}
