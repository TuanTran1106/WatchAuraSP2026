package com.example.watchaura.util;

import com.example.watchaura.dto.KhuyenMaiPriceResult;
import com.example.watchaura.entity.KhuyenMai;
import com.example.watchaura.entity.SanPham;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Kiểm tra khuyến mãi cấp {@link SanPham} và tính giá (tái sử dụng {@link KhuyenMaiPricing}).
 */
public final class SanPhamKhuyenMaiPricing {

    private SanPhamKhuyenMaiPricing() {
    }

    public static boolean isActive(SanPham sp, LocalDateTime now) {
        if (sp == null || now == null) {
            return false;
        }
        if (!Boolean.TRUE.equals(sp.getTrangThaiKhuyenMai())) {
            return false;
        }
        if (sp.getLoaiKhuyenMai() == null || sp.getLoaiKhuyenMai().isBlank()) {
            return false;
        }
        if (sp.getGiaTriKhuyenMai() == null || sp.getGiaTriKhuyenMai().compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        if (sp.getNgayBatDauKhuyenMai() != null && sp.getNgayBatDauKhuyenMai().isAfter(now)) {
            return false;
        }
        if (sp.getNgayKetThucKhuyenMai() != null && sp.getNgayKetThucKhuyenMai().isBefore(now)) {
            return false;
        }
        return true;
    }

    /**
     * @param giaGoc giá niêm yết cơ sở (vd. biến thể có giá thấp nhất)
     */
    public static KhuyenMaiPriceResult compute(SanPham sp, BigDecimal giaGoc, LocalDateTime now) {
        if (!isActive(sp, now)) {
            return KhuyenMaiPriceResult.none(giaGoc);
        }
        KhuyenMai proxy = new KhuyenMai();
        proxy.setLoaiGiam(sp.getLoaiKhuyenMai());
        proxy.setGiaTriGiam(sp.getGiaTriKhuyenMai());
        proxy.setGiamToiDa(null);
        proxy.setTrangThai(true);
        proxy.setTenChuongTrinh("Khuyến mãi sản phẩm");
        return KhuyenMaiPricing.compute(proxy, giaGoc);
    }
}
