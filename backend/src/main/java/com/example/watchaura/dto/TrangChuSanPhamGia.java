package com.example.watchaura.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Giá hiển thị trên card trang chủ: giá thấp nhất toàn cục + (tuỳ chọn) dòng KM biến thể khác.
 * POJO + Lombok getters để Thymeleaf resolve thuộc tính đáng tin cậy hơn so với Java record.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TrangChuSanPhamGia {

    /** Giá niêm yết (gạch) — khi biến thể có giá thấp nhất cũng đang KM. */
    private BigDecimal giaNiemyetGachNgang;
    /** Giá sau KM hoặc giá niêm yết thấp nhất (mức “Từ”). */
    private BigDecimal giaSauKhuyenMaiHienThi;
    private boolean coKhuyenMai;
    /**
     * Khi giá thấp nhất đến từ biến không KM nhưng còn biến khác đang KM: niêm yết của lựa chọn KM tốt nhất.
     */
    private BigDecimal giaNiemyetBienTheKhacKm;
    /** Giá sau KM của biến thể đó. */
    private BigDecimal giaSauBienTheKhacKm;
    /** Có hiển thị dòng phụ KM hay không. */
    private boolean coDongKmBienTheKhac;

    public static TrangChuSanPhamGia bangGiaNiemyet(BigDecimal gia) {
        BigDecimal g = gia != null ? gia : BigDecimal.ZERO;
        return new TrangChuSanPhamGia(null, g, false, null, null, false);
    }
}
