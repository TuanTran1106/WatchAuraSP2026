package com.example.watchaura.util;

import com.example.watchaura.dto.KhuyenMaiPriceResult;
import com.example.watchaura.entity.KhuyenMai;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Tính giá cho khuyến mãi sản phẩm.
 *
 * Luồng hiện tại đã refactor theo hướng chỉ còn giảm theo phần trăm.
 */
public final class KhuyenMaiPricing {

    private KhuyenMaiPricing() {
    }

    /**
     * Chuẩn hóa loại lưu DB cho khuyến mãi sản phẩm: chỉ nhận {@code PHAN_TRAM}.
     */
    public static String chuanHoaMaLuu(String loaiGiamRaw) {
        if (loaiGiamRaw == null) {
            return null;
        }
        String normalized = loaiGiamRaw.trim().toUpperCase();
        return switch (normalized) {
            case "PHAN_TRAM", "PERCENT", "%" -> "PHAN_TRAM";
            default -> null;
        };
    }

    /**
     * Tính khuyến mãi sản phẩm theo phần trăm.
     */
    public static KhuyenMaiPriceResult compute(KhuyenMai km, BigDecimal giaGoc) {
        if (giaGoc == null || giaGoc.compareTo(BigDecimal.ZERO) <= 0) {
            return KhuyenMaiPriceResult.none(giaGoc != null ? giaGoc : BigDecimal.ZERO);
        }
        if (km == null || Boolean.FALSE.equals(km.getTrangThai())) {
            return KhuyenMaiPriceResult.none(giaGoc);
        }
        if (km.getGiaTriGiam() == null || km.getGiaTriGiam().compareTo(BigDecimal.ZERO) <= 0) {
            return KhuyenMaiPriceResult.none(giaGoc);
        }

        String loai = chuanHoaMaLuu(km.getLoaiGiam());
        if (loai == null) {
            return KhuyenMaiPriceResult.none(giaGoc);
        }

        BigDecimal percent = km.getGiaTriGiam().min(BigDecimal.valueOf(100));
        if (percent.compareTo(BigDecimal.ZERO) <= 0) {
            return KhuyenMaiPriceResult.none(giaGoc);
        }

        BigDecimal soTienGiam = giaGoc.multiply(percent)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        soTienGiam = soTienGiam.min(giaGoc);

        BigDecimal giaSau = giaGoc.subtract(soTienGiam).max(BigDecimal.ZERO).setScale(0, RoundingMode.HALF_UP);
        String ten = km.getTenChuongTrinh() != null && !km.getTenChuongTrinh().isBlank()
                ? km.getTenChuongTrinh()
                : "Khuyến mãi sản phẩm";

        return new KhuyenMaiPriceResult(
                giaGoc,
                giaSau,
                soTienGiam,
                percent,
                true,
                ten,
                KhuyenMaiPriceResult.LoaiGiamApDung.PHAN_TRAM
        );
    }
}
