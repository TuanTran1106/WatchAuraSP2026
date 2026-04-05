package com.example.watchaura.util;

import com.example.watchaura.dto.GioHangChiTietDTO;
import com.example.watchaura.dto.KhuyenMaiPriceResult;
import com.example.watchaura.entity.KhuyenMai;
import com.example.watchaura.entity.SanPhamChiTiet;
import com.example.watchaura.service.SanPhamChiTietKhuyenMaiService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Gán giá sau KM vào dòng giỏ (đồng bộ với listing / tạo đơn).
 */
public final class GioHangChiTietPromoUtil {

    private GioHangChiTietPromoUtil() {
    }

    public static void applySanPhamKhuyenMai(
            SanPhamChiTietKhuyenMaiService khuyenMaiService,
            GioHangChiTietDTO dto,
            SanPhamChiTiet spct
    ) {
        applySanPhamKhuyenMai(khuyenMaiService, dto, spct, null, null);
    }

    public static void applySanPhamKhuyenMai(
            SanPhamChiTietKhuyenMaiService khuyenMaiService,
            GioHangChiTietDTO dto,
            SanPhamChiTiet spct,
            LocalDateTime now,
            List<KhuyenMai> chuongTrinhDangChay
    ) {
        if (dto == null || spct == null || khuyenMaiService == null) {
            return;
        }
        KhuyenMaiPriceResult r = khuyenMaiService.resolveBestForCartOrOrderLine(spct, now, chuongTrinhDangChay);
        dto.setGiaBan(r.giaSauGiam());
        if (r.coKhuyenMai()) {
            dto.setGiaGoc(r.giaGoc());
            dto.setTenKhuyenMai(r.tenChuongTrinh());
        } else {
            dto.setGiaGoc(null);
            dto.setTenKhuyenMai(null);
        }
        Integer sl = dto.getSoLuong();
        if (sl != null && r.giaSauGiam() != null) {
            dto.setThanhTien(r.giaSauGiam().multiply(BigDecimal.valueOf(sl)));
        } else {
            dto.setThanhTien(BigDecimal.ZERO);
        }
    }
}
