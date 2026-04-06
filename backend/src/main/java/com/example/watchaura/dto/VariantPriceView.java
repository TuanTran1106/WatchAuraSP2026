package com.example.watchaura.dto;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Giá hiển thị / JS đổi biến thể: đồng bộ với {@link KhuyenMaiPriceResult} (chương trình + KM sản phẩm).
 */
public final class VariantPriceView {
    private final BigDecimal giaGoc;
    private final BigDecimal giaSau;
    private final boolean coKhuyenMai;
    private final String loaiGiam;
    private final BigDecimal soTienGiam;
    private final BigDecimal phanTram;
    private final String tenChuongTrinh;

    VariantPriceView(
            BigDecimal giaGoc,
            BigDecimal giaSau,
            boolean coKhuyenMai,
            String loaiGiam,
            BigDecimal soTienGiam,
            BigDecimal phanTram,
            String tenChuongTrinh
    ) {
        this.giaGoc = giaGoc;
        this.giaSau = giaSau;
        this.coKhuyenMai = coKhuyenMai;
        this.loaiGiam = loaiGiam;
        this.soTienGiam = soTienGiam;
        this.phanTram = phanTram;
        this.tenChuongTrinh = tenChuongTrinh;
    }

    public BigDecimal giaGoc() {
        return giaGoc;
    }

    public BigDecimal giaSau() {
        return giaSau;
    }

    public boolean coKhuyenMai() {
        return coKhuyenMai;
    }

    public String loaiGiam() {
        return loaiGiam;
    }

    public BigDecimal soTienGiam() {
        return soTienGiam;
    }

    public BigDecimal phanTram() {
        return phanTram;
    }

    public String tenChuongTrinh() {
        return tenChuongTrinh;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (VariantPriceView) obj;
        return Objects.equals(this.giaGoc, that.giaGoc) &&
                Objects.equals(this.giaSau, that.giaSau) &&
                this.coKhuyenMai == that.coKhuyenMai &&
                Objects.equals(this.loaiGiam, that.loaiGiam) &&
                Objects.equals(this.soTienGiam, that.soTienGiam) &&
                Objects.equals(this.phanTram, that.phanTram) &&
                Objects.equals(this.tenChuongTrinh, that.tenChuongTrinh);
    }

    @Override
    public int hashCode() {
        return Objects.hash(giaGoc, giaSau, coKhuyenMai, loaiGiam, soTienGiam, phanTram, tenChuongTrinh);
    }

    @Override
    public String toString() {
        return "VariantPriceView[" +
                "giaGoc=" + giaGoc + ", " +
                "giaSau=" + giaSau + ", " +
                "coKhuyenMai=" + coKhuyenMai + ", " +
                "loaiGiam=" + loaiGiam + ", " +
                "soTienGiam=" + soTienGiam + ", " +
                "phanTram=" + phanTram + ", " +
                "tenChuongTrinh=" + tenChuongTrinh + ']';
    }

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
