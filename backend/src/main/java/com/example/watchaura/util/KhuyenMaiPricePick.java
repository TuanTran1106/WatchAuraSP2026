package com.example.watchaura.util;

import com.example.watchaura.dto.KhuyenMaiPriceResult;

import java.math.BigDecimal;

/**
 * Chọn kết quả KM tốt hơn (giá sau giảm thấp hơn / cùng giá thì ưu tiên giảm lớn hơn).
 */
public final class KhuyenMaiPricePick {

    private KhuyenMaiPricePick() {
    }

    public static KhuyenMaiPriceResult pickBetter(
            KhuyenMaiPriceResult a,
            KhuyenMaiPriceResult b,
            BigDecimal base) {
        boolean ca = a != null && a.coKhuyenMai();
        boolean cb = b != null && b.coKhuyenMai();
        if (!ca && !cb) {
            return KhuyenMaiPriceResult.none(base);
        }
        if (!ca) {
            return b;
        }
        if (!cb) {
            return a;
        }
        int cmp = a.giaSauGiam().compareTo(b.giaSauGiam());
        if (cmp < 0) {
            return a;
        }
        if (cmp > 0) {
            return b;
        }
        return a.soTienGiam().compareTo(b.soTienGiam()) >= 0 ? a : b;
    }
}
