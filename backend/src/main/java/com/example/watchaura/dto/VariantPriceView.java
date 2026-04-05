package com.example.watchaura.dto;

import java.math.BigDecimal;

/**
 * Giá hiển thị / JS đổi biến thể: đồng bộ với {@link KhuyenMaiPriceResult} (chương trình + KM sản phẩm).
 */
public record VariantPriceView(
        BigDecimal giaGoc,
        BigDecimal giaSau,
        boolean coKhuyenMai,
        String loaiGiam,
        BigDecimal soTienGiam,
        BigDecimal phanTram,
        String tenChuongTrinh
) {
    public static VariantPriceView from(KhuyenMaiPriceResult r) {
        if (r == null) {
            return new VariantPriceView(BigDecimal.ZERO, BigDecimal.ZERO, false, null,
                    BigDecimal.ZERO, BigDecimal.ZERO, null);
        }
        String loai = r.loaiGiamApDung() == KhuyenMaiPriceResult.LoaiGiamApDung.KHONG
                ? null
                : r.loaiGiamApDung().name();
        return new VariantPriceView(
                r.giaGoc(),
                r.giaSauGiam(),
                r.coKhuyenMai(),
                loai,
                r.soTienGiam(),
                r.phanTramHienThi(),
                r.tenChuongTrinh()
        );
    }
}
