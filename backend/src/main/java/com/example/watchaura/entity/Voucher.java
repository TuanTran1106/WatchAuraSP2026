package com.example.watchaura.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Voucher")
public class Voucher {

    /** Trần số tiền (₫) hợp lệ cho cột decimal(18,2) — đủ lớn cho đơn hàng thực tế. */
    private static final BigDecimal MAX_DONG_AMOUNT = new BigDecimal("999999999999.99");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Mã voucher không được để trống")
    @Pattern(regexp = "^[A-Z0-9]{1,50}$", message = "Mã chỉ gồm chữ in hoa A–Z và số 0–9, không khoảng trắng")
    @Column(name = "ma_voucher", nullable = false, unique = true, length = 50)
    private String maVoucher;

    @NotBlank(message = "Tên voucher không được để trống")
    @Size(max = 255, message = "Tên voucher tối đa 255 ký tự")
    @Column(name = "ten_voucher", nullable = false, length = 255)
    private String tenVoucher;

    @Size(max = 500, message = "Mô tả tối đa 500 ký tự")
    @Column(name = "mo_ta", length = 500)
    private String moTa;

    @NotBlank(message = "Vui lòng chọn loại voucher")
    @Column(name = "loai_voucher", nullable = false, length = 20)
    private String loaiVoucher;

    @NotNull(message = "Giá trị không được để trống")
    @DecimalMin(value = "0.01", inclusive = true, message = "Giá trị phải lớn hơn 0")
    @Digits(integer = 16, fraction = 2, message = "Giá trị: tối đa 16 chữ số phần nguyên và 2 chữ số thập phân")
    @Column(name = "gia_tri", precision = 18, scale = 2, nullable = false)
    private BigDecimal giaTri;

    @Digits(integer = 16, fraction = 2, message = "Giá trị tối đa (₫): tối đa 16 chữ số phần nguyên và 2 chữ số thập phân")
    @DecimalMax(value = "999999999999.99", inclusive = true, message = "Giá trị tối đa (₫) vượt ngưỡng cho phép")
    @Column(name = "gia_tri_toi_da", precision = 18, scale = 2)
    private BigDecimal giaTriToiDa;

    @Digits(integer = 16, fraction = 2, message = "Đơn hàng tối thiểu: tối đa 16 chữ số phần nguyên và 2 chữ số thập phân")
    @DecimalMax(value = "999999999999.99", inclusive = true, message = "Đơn hàng tối thiểu vượt ngưỡng cho phép")
    @Column(name = "don_hang_toi_thieu", precision = 18, scale = 2)
    private BigDecimal donHangToiThieu;

    @Min(value = 1, message = "Số lượng tổng phải ít nhất là 1")
    @Max(value = 999_999_999, message = "Số lượng tổng vượt ngưỡng cho phép")
    @Column(name = "so_luong_tong")
    private Integer soLuongTong;

    @Column(name = "so_luong_da_dung")
    private Integer soLuongDaDung;

    @Column(name = "gioi_han_moi_user")
    private Boolean gioiHanMoiUser;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(name = "ngay_bat_dau")
    private LocalDateTime ngayBatDau;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @Column(name = "ngay_ket_thuc")
    private LocalDateTime ngayKetThuc;

    @NotNull(message = "Vui lòng chọn trạng thái")
    @Column(name = "trang_thai")
    private Boolean trangThai;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;

    @Size(max = 255, message = "Danh mục áp dụng tối đa 255 ký tự")
    @Column(name = "danh_muc_ap_dung")
    private String danhMucApDung;

    @AssertTrue(message = "Ngày kết thúc phải sau hoặc trùng ngày bắt đầu")
    public boolean isNgayKetThucSauNgayBatDau() {
        if (ngayBatDau == null || ngayKetThuc == null) return true;
        return !ngayKetThuc.isBefore(ngayBatDau);
    }

    @AssertTrue(message = "Loại voucher không hợp lệ")
    public boolean isLoaiVoucherHopLe() {
        if (loaiVoucher == null) return false;
        String t = loaiVoucher.trim().toUpperCase();
        return "PHAN_TRAM".equals(t) || "PERCENT".equals(t) || "TIEN".equals(t);
    }

    @AssertTrue(message = "Giá trị % phải từ 0,01 đến 100")
    public boolean isGiaTriPhanTramHopLe() {
        if (loaiVoucher == null || giaTri == null) return true;
        String t = loaiVoucher.trim().toUpperCase();
        if (!"PHAN_TRAM".equals(t) && !"PERCENT".equals(t)) return true;
        return giaTri.compareTo(new BigDecimal("0.01")) >= 0 && giaTri.compareTo(new BigDecimal("100")) <= 0;
    }

    @AssertTrue(message = "Khi giảm theo %, phải nhập giá trị tối đa (₫) lớn hơn 0")
    public boolean isGiaTriToiDaKhiPhanTram() {
        if (loaiVoucher == null) return true;
        String t = loaiVoucher.trim().toUpperCase();
        if (!"PHAN_TRAM".equals(t) && !"PERCENT".equals(t)) return true;
        return giaTriToiDa != null && giaTriToiDa.compareTo(BigDecimal.ZERO) > 0;
    }

    @AssertTrue(message = "Đơn hàng tối thiểu phải để trống hoặc lớn hơn 0")
    public boolean isDonHangToiThieuHopLe() {
        if (donHangToiThieu == null) return true;
        return donHangToiThieu.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Chỉ áp khi tạo mới ({@code id == null}): không cho chọn mốc bắt đầu đã qua.
     * Khi sửa voucher cũ, cho phép giữ ngày trong quá khứ.
     */
    @AssertTrue(message = "Ngày bắt đầu không được trong quá khứ khi tạo voucher mới")
    public boolean isNgayBatDauHopLeKhiTao() {
        if (id != null) return true;
        if (ngayBatDau == null) return true;
        return !ngayBatDau.isBefore(LocalDateTime.now());
    }

    @AssertTrue(message = "Ngày kết thúc không được trong quá khứ khi tạo voucher mới")
    public boolean isNgayKetThucHopLeKhiTao() {
        if (id != null) return true;
        if (ngayKetThuc == null) return true;
        return !ngayKetThuc.isBefore(LocalDateTime.now());
    }

    @AssertTrue(message = "Giảm tiền (₫) không được vượt 999.999.999.999,99 ₫")
    public boolean isGiaTriTienTrongNguong() {
        if (loaiVoucher == null || giaTri == null) return true;
        String t = loaiVoucher.trim().toUpperCase();
        if (!"TIEN".equals(t)) return true;
        return giaTri.compareTo(MAX_DONG_AMOUNT) <= 0;
    }

    @AssertTrue(message = "Đang hoạt động nhưng đã quá ngày kết thúc — hãy tắt trạng thái hoặc gia hạn ngày kết thúc")
    public boolean isTrangThaiKhongMoKhiHetHan() {
        if (!Boolean.TRUE.equals(trangThai)) return true;
        if (ngayKetThuc == null) return true;
        return !ngayKetThuc.isBefore(LocalDateTime.now());
    }

    /**
     * Khi tạo mới: nếu có giới hạn lượt và đang bật hoạt động, tổng lượt phải lớn hơn 0 đã dùng (thường là 0).
     * Khi sửa: kiểm tra đầy đủ nằm ở controller vì {@code soLuongDaDung} có thể không gửi từ form.
     */
    @AssertTrue(message = "Đang hoạt động nhưng số lượng tổng không còn lượt — hãy tăng số lượng hoặc tắt trạng thái")
    public boolean isTrangThaiDongBoSoLuongKhiTao() {
        if (id != null) return true;
        if (!Boolean.TRUE.equals(trangThai)) return true;
        Integer tong = soLuongTong;
        if (tong == null) return true;
        int dd = soLuongDaDung != null ? soLuongDaDung : 0;
        return tong > dd;
    }
}
