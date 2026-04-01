package com.example.watchaura.util;

import java.math.BigDecimal;

/**
 * Phí giao hàng online — đồng bộ với trang thanh toán (thanh-toan.html).
 */
public final class ShippingFeeUtil {

    public static final BigDecimal FEE = new BigDecimal("40000");
    public static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("1000000");

    private ShippingFeeUtil() {
    }

    /**
     * @param merchandiseSubtotal tổng tiền hàng (sau giảm giá voucher nếu có), chưa cộng phí ship
     */
    public static BigDecimal feeForMerchandiseSubtotal(BigDecimal merchandiseSubtotal) {
        if (merchandiseSubtotal == null || merchandiseSubtotal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        if (merchandiseSubtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0) {
            return BigDecimal.ZERO;
        }
        return FEE;
    }
}
