package com.example.watchaura.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "Voucher")
public class Voucher {

    public interface OnCreate {}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_voucher", nullable = false, unique = true, length = 50)
    private String maVoucher;

    @Column(name = "ten_voucher", nullable = false, length = 255)
    private String tenVoucher;

    @Column(name = "mo_ta", length = 500)
    private String moTa;

    @Column(name = "loai_voucher", nullable = false, length = 20)
    private String loaiVoucher;

    @Column(name = "gia_tri", precision = 18, scale = 2, nullable = false)
    private BigDecimal giaTri;

    @Column(name = "gia_tri_toi_da", precision = 18, scale = 2)
    private BigDecimal giaTriToiDa;

    @Column(name = "don_hang_toi_thieu", precision = 18, scale = 2)
    private BigDecimal donHangToiThieu;

    @Column(name = "so_luong_tong")
    private Integer soLuongTong;

    @Column(name = "so_luong_da_dung")
    private Integer soLuongDaDung;

    @Column(name = "ngay_bat_dau")
    @FutureOrPresent(message = "Ngày bắt đầu không được ở quá khứ", groups = OnCreate.class)
    private LocalDateTime ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    @FutureOrPresent(message = "Ngày kết thúc không được ở quá khứ", groups = OnCreate.class)
    private LocalDateTime ngayKetThuc;

    @AssertTrue(message = "Ngày kết thúc phải sau hoặc trùng ngày bắt đầu")
    public boolean isNgayKetThucSauNgayBatDau() {
        if (ngayBatDau == null || ngayKetThuc == null) return true;
        return !ngayKetThuc.isBefore(ngayBatDau);
    }

    @Column(name = "trang_thai")
    private Boolean trangThai;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @Column(name = "ngay_cap_nhat")
    private LocalDateTime ngayCapNhat;
}
