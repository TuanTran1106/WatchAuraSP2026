package com.example.watchaura.service;

import com.example.watchaura.dto.KhuyenMaiPriceResult;
import com.example.watchaura.dto.TrangChuSanPhamGia;
import com.example.watchaura.entity.KhuyenMai;
import com.example.watchaura.entity.SanPhamChiTiet;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface SanPhamChiTietKhuyenMaiService {
    TrangChuSanPhamGia resolveLowestPriceForSanPham(Integer sanPhamId);

    default KhuyenMaiPriceResult resolveForSanPhamChiTiet(Integer id, BigDecimal giaBan) {
        return resolveForSanPhamChiTiet(id, giaBan, null, null);
    }

    /**
     * @param chuongTrinhDangChay danh sách KM đang hiệu lực (vd. từ {@link KhuyenMaiService#getActivePromotions}); null = tự tải
     * @param tenDanhMucSanPham     tên danh mục sản phẩm; null = tự lấy từ biến thể
     */
    KhuyenMaiPriceResult resolveForSanPhamChiTiet(
            Integer id,
            BigDecimal giaBan,
            List<KhuyenMai> chuongTrinhDangChay,
            String tenDanhMucSanPham);

    /**
     * KM tốt nhất cho một dòng (chương trình + KM cấp sản phẩm), đồng bộ với card/listing.
     *
     * @param chuongTrinhDangChay null = gọi {@link com.example.watchaura.service.KhuyenMaiService#getActivePromotions}
     */
    default KhuyenMaiPriceResult resolveBestForCartOrOrderLine(SanPhamChiTiet spct) {
        return resolveBestForCartOrOrderLine(spct, null, null);
    }

    KhuyenMaiPriceResult resolveBestForCartOrOrderLine(
            SanPhamChiTiet spct,
            LocalDateTime now,
            List<KhuyenMai> chuongTrinhDangChay);
}
