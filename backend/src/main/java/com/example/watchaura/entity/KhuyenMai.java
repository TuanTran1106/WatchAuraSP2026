package com.example.watchaura.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "KhuyenMai")
public class KhuyenMai {

    public enum PhamViApDung {
        ALL,
        CATEGORY,
        SKU
    }

    public interface OnCreate {}

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_khuyen_mai", unique = true, length = 50)
    private String maKhuyenMai;

    @Column(name = "ten_chuong_trinh", nullable = false, length = 255)
    private String tenChuongTrinh;

    @Column(name = "mo_ta", length = 500)
    private String moTa;

    @Column(name = "danh_muc_ap_dung", length = 255)
    private String danhMucApDung;

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "pham_vi_ap_dung")
    private PhamViApDung phamViApDung;

    @Column(name = "loai_giam", nullable = false, length = 20)
    private String loaiGiam;

    @Column(name = "gia_tri_giam", precision = 18, scale = 2, nullable = false)
    private BigDecimal giaTriGiam;

    @Column(name = "giam_toi_da", precision = 18, scale = 2)
    private BigDecimal giamToiDa;

    @Column(name = "don_toi_thieu", precision = 18, scale = 2)
    private BigDecimal donToiThieu;

    @Column(name = "gioi_han_luot_dung")
    private Integer gioiHanLuotDung;

    @Column(name = "so_luot_da_dung")
    private Integer soLuotDaDung;

    @Column(name = "ngay_bat_dau")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    @FutureOrPresent(message = "Ngày bắt đầu không được ở quá khứ", groups = OnCreate.class)
    private LocalDateTime ngayBatDau;

    @Column(name = "ngay_ket_thuc")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
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
