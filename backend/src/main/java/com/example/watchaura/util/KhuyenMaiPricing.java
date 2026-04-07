package com.example.watchaura.util;

import com.example.watchaura.dto.KhuyenMaiPriceResult;
import com.example.watchaura.entity.KhuyenMai;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;

/**
 * Tính giảm giá từ {@link KhuyenMai} và giá niêm yết.
 * <ul>
 *   <li>{@code PHAN_TRAM} / {@code PERCENT}: {@code giaTriGiam} = phần trăm (0–100).
 *       {@code giamToiDa} = trần <strong>số tiền</strong> được giảm (VNĐ), có thể để trống.</li>
 *   <li>{@code TIEN} / {@code FIXED}: {@code giaTriGiam} = số tiền trừ thẳng (VNĐ). {@code giamToiDa} không dùng.</li>
 * </ul>
 */
public final class KhuyenMaiPricing {

    public enum LoaiGiamTinh {
        /** Giảm theo % — {@code giaTriGiam} là phần trăm. */
        PHAN_TRAM,
        /** Giảm số tiền cố định — {@code giaTriGiam} là VNĐ. */
        TIEN,
        /** Không nhận dạng được (không áp dụng khi tính giá). */
        KHONG_RO
    }

    private KhuyenMaiPricing() {
    }

    /**
     * Chuẩn hóa mã lưu DB: {@code PHAN_TRAM} hoặc {@code TIEN}; {@code null} nếu rỗng / không hiểu.
     */
    public static String chuanHoaMaLuu(String loaiGiamRaw) {
        return switch (phanLoai(loaiGiamRaw)) {
            case PHAN_TRAM -> "PHAN_TRAM";
            case TIEN -> "TIEN";
            case KHONG_RO -> null;
        };
    }

    public static LoaiGiamTinh phanLoai(String loaiGiamRaw) {
        String loai = normalizeLoai(loaiGiamRaw);
        if (loai.isEmpty()) {
            return LoaiGiamTinh.KHONG_RO;
        }
        if (isPercentType(loai)) {
            return LoaiGiamTinh.PHAN_TRAM;
        }
        if (isFixedType(loai)) {
            return LoaiGiamTinh.TIEN;
        }
        return LoaiGiamTinh.KHONG_RO;
    }

    /**
     * Tránh lỗi phổ biến: chọn "giảm tiền (VNĐ)" nhưng nhập {@code 10} nghĩa là 10% →
     * hệ thống trừ 10₫ thay vì 10% (vd. 1.500.000 → 1.499.990).
     * <p>
     * Nếu loại là TIEN, giá trị là số nguyên 1–10, giá niêm yết đủ lớn, và số tiền giảm
     * nhỏ hơn 1% giá niêm yết, coi là nhập nhầm và tính như {@code PHAN_TRAM}.
     */
    static LoaiGiamTinh dieuChinhLoaiNeuNhamTienVoiPhanTram(
            LoaiGiamTinh kind, BigDecimal giaTriGiam, BigDecimal giaGoc) {
        if (kind != LoaiGiamTinh.TIEN || giaTriGiam == null || giaGoc == null) {
            return kind;
        }
        if (giaGoc.compareTo(BigDecimal.valueOf(100_000L)) < 0) {
            return kind;
        }
        // Giá trị là số nguyên 1…10 (cho phép lệch làm tròn DECIMAL từ DB, vd 9.999 → 10)
        BigDecimal vRound = giaTriGiam.setScale(0, RoundingMode.HALF_UP);
        if (giaTriGiam.subtract(vRound).abs().compareTo(new BigDecimal("0.02")) > 0) {
            return kind;
        }
        if (vRound.compareTo(BigDecimal.ONE) < 0 || vRound.compareTo(BigDecimal.valueOf(10)) > 0) {
            return kind;
        }
        BigDecimal motPhanTramCuaGia = giaGoc.divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        if (motPhanTramCuaGia.compareTo(BigDecimal.ZERO) <= 0) {
            return kind;
        }
        BigDecimal tienGiamNeuLaTienMat = vRound.min(giaGoc);
        if (tienGiamNeuLaTienMat.compareTo(motPhanTramCuaGia) < 0) {
            return LoaiGiamTinh.PHAN_TRAM;
        }
        return kind;
    }

    /**
     * Lớp 2: sau khi đã tính theo TIEN, nếu vẫn là “trừ vài đồng” trên giá rất lớn thì coi là nhầm %.
     * (Tránh trường hợp {@link #dieuChinhLoaiNeuNhamTienVoiPhanTram} không khớp do version cũ / kiểu DB.)
     */
    private static boolean nenTinhLaiNhuPhanTramSauTien(
            KhuyenMai km, BigDecimal giaGoc, BigDecimal soTienGiamTheoTien) {
        if (km == null || km.getGiaTriGiam() == null || soTienGiamTheoTien == null) {
            return false;
        }
        if (giaGoc.compareTo(BigDecimal.valueOf(100_000L)) < 0) {
            return false;
        }
        if (soTienGiamTheoTien.compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        // Giá gốc phải đủ lớn so với mức "trừ vài đồng" (vd 10₫): với hệ số 1_000_000,
        // giá ~1,5M không vượt qua (1,5M ≤ 10M) nên lớp 2 không chạy — badge % / dữ liệu vẫn sai.
        if (giaGoc.compareTo(soTienGiamTheoTien.multiply(BigDecimal.valueOf(100_000L))) <= 0) {
            return false;
        }
        BigDecimal motPhanTram = giaGoc.divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        if (soTienGiamTheoTien.compareTo(motPhanTram) >= 0) {
            return false;
        }
        BigDecimal vr = km.getGiaTriGiam().setScale(0, RoundingMode.HALF_UP);
        if (vr.compareTo(BigDecimal.ONE) < 0 || vr.compareTo(BigDecimal.valueOf(10)) > 0) {
            return false;
        }
        if (soTienGiamTheoTien.subtract(vr).abs().compareTo(new BigDecimal("0.02")) > 0) {
            return false;
        }
        return true;
    }

    private static void apPhanTramTuGiaTriGiam(
            KhuyenMai km, BigDecimal giaGoc, BigDecimal[] soTienGiam, BigDecimal[] phanTramHienThi) {
        BigDecimal percent = km.getGiaTriGiam().min(BigDecimal.valueOf(100));
        if (percent.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        BigDecimal st = giaGoc.multiply(percent).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        if (km.getGiamToiDa() != null && km.getGiamToiDa().compareTo(BigDecimal.ZERO) > 0) {
            st = st.min(km.getGiamToiDa().setScale(0, RoundingMode.HALF_UP));
        }
        st = st.min(giaGoc);
        soTienGiam[0] = st;
        phanTramHienThi[0] = percent;
    }

    public static KhuyenMaiPriceResult compute(KhuyenMai km, BigDecimal giaGoc) {
        if (giaGoc == null) {
            return KhuyenMaiPriceResult.none(BigDecimal.ZERO);
        }
        if (giaGoc.compareTo(BigDecimal.ZERO) <= 0) {
            return KhuyenMaiPriceResult.none(giaGoc);
        }
        if (km == null || Boolean.FALSE.equals(km.getTrangThai())) {
            return KhuyenMaiPriceResult.none(giaGoc);
        }
        if (km.getGiaTriGiam() == null || km.getGiaTriGiam().compareTo(BigDecimal.ZERO) <= 0) {
            return KhuyenMaiPriceResult.none(giaGoc);
        }

        String loaiRawNorm = normalizeLoai(km.getLoaiGiam());
        boolean loaiDaChuanHoaTuAdmin = "TIEN".equals(loaiRawNorm) || "PHAN_TRAM".equals(loaiRawNorm);
        LoaiGiamTinh kind = phanLoai(km.getLoaiGiam());
        if (!loaiDaChuanHoaTuAdmin) {
            kind = dieuChinhLoaiNeuNhamTienVoiPhanTram(kind, km.getGiaTriGiam(), giaGoc);
        }

        // Gán mặc định để thỏa definite assignment (mọi nhánh hợp lệ đều gán lại hoặc return sớm).
        BigDecimal soTienGiam = BigDecimal.ZERO;
        BigDecimal phanTramHienThi = BigDecimal.ZERO;
        boolean tinhLaiNhuPhanTram = false;

        switch (kind) {
            case PHAN_TRAM -> {
                BigDecimal percent = km.getGiaTriGiam().min(BigDecimal.valueOf(100));
                if (percent.compareTo(BigDecimal.ZERO) <= 0) {
                    return KhuyenMaiPriceResult.none(giaGoc);
                }
                soTienGiam = giaGoc.multiply(percent)
                        .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
                if (km.getGiamToiDa() != null && km.getGiamToiDa().compareTo(BigDecimal.ZERO) > 0) {
                    soTienGiam = soTienGiam.min(km.getGiamToiDa().setScale(0, RoundingMode.HALF_UP));
                }
                soTienGiam = soTienGiam.min(giaGoc);
                phanTramHienThi = percent;
            }
            case TIEN -> {
                soTienGiam = km.getGiaTriGiam().setScale(0, RoundingMode.HALF_UP).min(giaGoc);
                if (soTienGiam.compareTo(BigDecimal.ZERO) <= 0) {
                    return KhuyenMaiPriceResult.none(giaGoc);
                }
                if (!loaiDaChuanHoaTuAdmin && nenTinhLaiNhuPhanTramSauTien(km, giaGoc, soTienGiam)) {
                    BigDecimal[] st = {BigDecimal.ZERO};
                    BigDecimal[] pt = {BigDecimal.ZERO};
                    apPhanTramTuGiaTriGiam(km, giaGoc, st, pt);
                    soTienGiam = st[0];
                    phanTramHienThi = pt[0];
                    tinhLaiNhuPhanTram = true;
                } else {
                    phanTramHienThi = giaGoc.compareTo(BigDecimal.ZERO) > 0
                            ? soTienGiam.multiply(BigDecimal.valueOf(100))
                            .divide(giaGoc, 2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                }
            }
            case KHONG_RO -> {
                return KhuyenMaiPriceResult.none(giaGoc);
            }
        }

        BigDecimal giaSau = giaGoc.subtract(soTienGiam);
        if (giaSau.compareTo(BigDecimal.ZERO) < 0) {
            giaSau = BigDecimal.ZERO;
        }
        giaSau = giaSau.setScale(0, RoundingMode.HALF_UP);

        String ten = km.getTenChuongTrinh() != null ? km.getTenChuongTrinh() : "";
        KhuyenMaiPriceResult.LoaiGiamApDung loaiOut;
        if (tinhLaiNhuPhanTram) {
            loaiOut = KhuyenMaiPriceResult.LoaiGiamApDung.PHAN_TRAM;
        } else {
            loaiOut = switch (kind) {
                case PHAN_TRAM -> KhuyenMaiPriceResult.LoaiGiamApDung.PHAN_TRAM;
                case TIEN -> KhuyenMaiPriceResult.LoaiGiamApDung.TIEN;
                case KHONG_RO -> KhuyenMaiPriceResult.LoaiGiamApDung.KHONG;
            };
        }
        KhuyenMaiPriceResult built = new KhuyenMaiPriceResult(
                giaGoc,
                giaSau,
                soTienGiam,
                phanTramHienThi,
                true,
                ten.isBlank() ? "Khuyến mãi" : ten,
                loaiOut
        );
        if (loaiDaChuanHoaTuAdmin) {
            return built;
        }
        return postFixNeuConLaTienNhungGiaTriLaPhanTram(built, km);
    }

    /**
     * Lưới an toàn: vẫn còn {@link KhuyenMaiPriceResult.LoaiGiamApDung#TIEN} nhưng giá trị KM là số nguyên 1–10
     * khớp số tiền trừ (vd 10₫) và nếu hiểu theo % thì mức giảm lớn hơn rõ rệt → coi là nhập nhầm %.
     */
    private static KhuyenMaiPriceResult postFixNeuConLaTienNhungGiaTriLaPhanTram(KhuyenMaiPriceResult r, KhuyenMai km) {
        if (!r.coKhuyenMai() || km == null || km.getGiaTriGiam() == null) {
            return r;
        }
        if (r.loaiGiamApDung() != KhuyenMaiPriceResult.LoaiGiamApDung.TIEN) {
            return r;
        }
        BigDecimal g = r.giaGoc();
        BigDecimal off = r.soTienGiam();
        if (g == null || off == null) {
            return r;
        }
        if (g.compareTo(BigDecimal.valueOf(100_000L)) < 0) {
            return r;
        }
        if (off.compareTo(BigDecimal.ZERO) <= 0 || off.compareTo(BigDecimal.valueOf(15)) > 0) {
            return r;
        }
        BigDecimal raw = km.getGiaTriGiam();
        BigDecimal vr = raw.setScale(0, RoundingMode.HALF_UP);
        if (raw.subtract(vr).abs().compareTo(new BigDecimal("0.02")) > 0) {
            return r;
        }
        if (vr.compareTo(BigDecimal.ONE) < 0 || vr.compareTo(BigDecimal.TEN) > 0) {
            return r;
        }
        if (off.subtract(vr).abs().compareTo(new BigDecimal("0.02")) > 0) {
            return r;
        }
        BigDecimal asPercent = g.multiply(vr).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);
        if (km.getGiamToiDa() != null && km.getGiamToiDa().compareTo(BigDecimal.ZERO) > 0) {
            asPercent = asPercent.min(km.getGiamToiDa().setScale(0, RoundingMode.HALF_UP));
        }
        asPercent = asPercent.min(g);
        if (asPercent.subtract(off).compareTo(BigDecimal.valueOf(1_000L)) < 0) {
            return r;
        }
        BigDecimal newSau = g.subtract(asPercent).setScale(0, RoundingMode.HALF_UP);
        if (newSau.compareTo(BigDecimal.ZERO) < 0) {
            newSau = BigDecimal.ZERO;
        }
        String ten = r.tenChuongTrinh() != null ? r.tenChuongTrinh() : "Khuyến mãi";
        return new KhuyenMaiPriceResult(
                g,
                newSau,
                asPercent,
                vr,
                true,
                ten,
                KhuyenMaiPriceResult.LoaiGiamApDung.PHAN_TRAM);
    }

    /**
     * Giỏ hàng / đơn / thanh toán: nếu KM đang trả về mức giảm rất nhỏ (≤15₫, gần số nguyên) trên giá niêm yết đủ lớn,
     * mà cùng con số hiểu như % sẽ tiết kiệm thêm ít nhất 50.000₫ → coi là nhầm “trừ tiền” vs “phần trăm” (vd 10₫ vs 10%).
     */
    public static KhuyenMaiPriceResult sanitizeMisappliedFlatPromo(KhuyenMaiPriceResult r) {
        if (r == null || !r.coKhuyenMai()) {
            return r;
        }
        BigDecimal g = r.giaGoc();
        BigDecimal sau = r.giaSauGiam();
        if (g == null || sau == null) {
            return r;
        }
        if (g.compareTo(BigDecimal.valueOf(500_000L)) < 0) {
            return r;
        }
        BigDecimal off = g.subtract(sau);
        if (off.compareTo(BigDecimal.ZERO) <= 0 || off.compareTo(BigDecimal.valueOf(15)) > 0) {
            return r;
        }
        BigDecimal pctWhole = off.setScale(0, RoundingMode.HALF_UP);
        if (off.subtract(pctWhole).abs().compareTo(new BigDecimal("0.02")) > 0) {
            return r;
        }
        if (pctWhole.compareTo(BigDecimal.ONE) < 0 || pctWhole.compareTo(BigDecimal.valueOf(15)) > 0) {
            return r;
        }
        BigDecimal asPercentOff = g.multiply(pctWhole)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                .min(g);
        if (asPercentOff.subtract(off).compareTo(BigDecimal.valueOf(50_000L)) < 0) {
            return r;
        }
        BigDecimal newSau = g.subtract(asPercentOff).setScale(0, RoundingMode.HALF_UP).max(BigDecimal.ZERO);
        String ten = r.tenChuongTrinh() != null && !r.tenChuongTrinh().isBlank()
                ? r.tenChuongTrinh()
                : "Khuyến mãi";
        return new KhuyenMaiPriceResult(
                g,
                newSau,
                asPercentOff,
                pctWhole,
                true,
                ten,
                KhuyenMaiPriceResult.LoaiGiamApDung.PHAN_TRAM);
    }

    static String normalizeLoai(String loaiGiam) {
        if (loaiGiam == null) {
            return "";
        }
        return loaiGiam.trim().toUpperCase(Locale.ROOT);
    }

    private static boolean isPercentType(String loai) {
        if (loai == null || loai.isEmpty()) {
            return false;
        }
        if ("PERCENT".equals(loai) || "PHAN_TRAM".equals(loai) || "%".equals(loai)) {
            return true;
        }
        if (loai.contains("PHAN_TRAM") || loai.contains("PERCENT")) {
            return true;
        }
        if (loai.contains("%")) {
            return true;
        }
        if (loai.contains("GIẢM") && (loai.contains("%") || loai.contains("PHẦN") || loai.contains("TRĂM"))) {
            return true;
        }
        return loai.contains("GIAM") && loai.contains("%");
    }

    /**
     * Giảm tiền: ưu tiên sau khi đã loại trừ dạng phần trăm (tránh nhầm chuỗi chứa cả “phần” và “tiền”).
     */
    private static boolean isFixedType(String loai) {
        if (loai == null || loai.isEmpty()) {
            return false;
        }
        if (isPercentType(loai)) {
            return false;
        }
        if ("FIXED".equals(loai) || "TIEN".equals(loai) || "TIEN_MAT".equals(loai)) {
            return true;
        }
        if (loai.contains("FIXED") || loai.contains("TIEN_MAT")) {
            return true;
        }
        boolean coTien = loai.contains("TIEN") || loai.contains("TIỀN");
        if (coTien && !loai.contains("PHAN") && !loai.contains("PHẦN") && !loai.contains("TRĂM")) {
            return true;
        }
        return loai.contains("GIẢM")
                && (loai.contains("TIỀN") || loai.contains("MẶT") || loai.contains("TIEN"));
    }
}
