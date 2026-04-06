package com.example.watchaura.dto;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Kết quả áp khuyến mãi sản phẩm (theo giá niêm yết của biến thể).
 */
public final class KhuyenMaiPriceResult {
    private final BigDecimal giaGoc;
    private final BigDecimal giaSauGiam;
    private final BigDecimal soTienGiam;
    private final BigDecimal phanTramHienThi;
    private final boolean coKhuyenMai;
    private final String tenChuongTrinh;
    private final LoaiGiamApDung loaiGiamApDung;

    public KhuyenMaiPriceResult(
            BigDecimal giaGoc,
            BigDecimal giaSauGiam,
            BigDecimal soTienGiam,
            BigDecimal phanTramHienThi,
            boolean coKhuyenMai,
            String tenChuongTrinh,
            LoaiGiamApDung loaiGiamApDung
    ) {
        this.giaGoc = giaGoc;
        this.giaSauGiam = giaSauGiam;
        this.soTienGiam = soTienGiam;
        this.phanTramHienThi = phanTramHienThi;
        this.coKhuyenMai = coKhuyenMai;
        this.tenChuongTrinh = tenChuongTrinh;
        this.loaiGiamApDung = loaiGiamApDung;
    }

    public BigDecimal giaGoc() {
        return giaGoc;
    }

    public BigDecimal giaSauGiam() {
        return giaSauGiam;
    }

    public BigDecimal soTienGiam() {
        return soTienGiam;
    }

    public BigDecimal phanTramHienThi() {
        return phanTramHienThi;
    }

    public boolean coKhuyenMai() {
        return coKhuyenMai;
    }

    public String tenChuongTrinh() {
        return tenChuongTrinh;
    }

    public LoaiGiamApDung loaiGiamApDung() {
        return loaiGiamApDung;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (KhuyenMaiPriceResult) obj;
        return Objects.equals(this.giaGoc, that.giaGoc) &&
                Objects.equals(this.giaSauGiam, that.giaSauGiam) &&
                Objects.equals(this.soTienGiam, that.soTienGiam) &&
                Objects.equals(this.phanTramHienThi, that.phanTramHienThi) &&
                this.coKhuyenMai == that.coKhuyenMai &&
                Objects.equals(this.tenChuongTrinh, that.tenChuongTrinh) &&
                Objects.equals(this.loaiGiamApDung, that.loaiGiamApDung);
    }

    @Override
    public int hashCode() {
        return Objects.hash(giaGoc, giaSauGiam, soTienGiam, phanTramHienThi, coKhuyenMai, tenChuongTrinh, loaiGiamApDung);
    }

    @Override
    public String toString() {
        return "KhuyenMaiPriceResult[" +
                "giaGoc=" + giaGoc + ", " +
                "giaSauGiam=" + giaSauGiam + ", " +
                "soTienGiam=" + soTienGiam + ", " +
                "phanTramHienThi=" + phanTramHienThi + ", " +
                "coKhuyenMai=" + coKhuyenMai + ", " +
                "tenChuongTrinh=" + tenChuongTrinh + ", " +
                "loaiGiamApDung=" + loaiGiamApDung + ']';
    }

    /**
     * Dạng giảm đang áp dụng (để UI không nhầm % với số tiền).
     */
    public enum LoaiGiamApDung {
        PHAN_TRAM,
        TIEN,
        KHONG
    }

    public static KhuyenMaiPriceResult none(BigDecimal giaGoc) {
        BigDecimal g = giaGoc != null ? giaGoc : BigDecimal.ZERO;
        return new KhuyenMaiPriceResult(
                g, g, BigDecimal.ZERO, BigDecimal.ZERO, false, null, LoaiGiamApDung.KHONG);
    }
}
