package com.example.watchaura.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Table(name = "SanPham")
@JsonIgnoreProperties(ignoreUnknown = true, value = { "hibernateLazyInitializer", "handler" })
public class SanPham {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_san_pham", nullable = false, unique = true, length = 50)
    private String maSanPham;

    @Column(name = "ten_san_pham", length = 255)
    private String tenSanPham;

    @Column(name = "mo_ta", columnDefinition = "NVARCHAR(MAX)")
    private String moTa;

    @Column(name = "hinh_anh", length = 255)
    private String hinhAnh;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_thuong_hieu")
    private ThuongHieu thuongHieu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_danh_muc")
    private DanhMuc danhMuc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_loai_may")
    private LoaiMay loaiMay;

    @Column(name = "phong_cach", length = 100)
    private String phongCach;

    @Column(name = "trang_thai")
    private Boolean trangThai;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    /** PHAN_TRAM / PERCENT / % hoặc TIEN / TIEN_MAT / FIXED — cùng quy ước với {@link KhuyenMai}. */
    @Column(name = "loai_khuyen_mai", length = 20)
    private String loaiKhuyenMai;

    @Column(name = "gia_tri_khuyen_mai", precision = 18, scale = 2)
    private BigDecimal giaTriKhuyenMai;

    @Column(name = "ngay_bat_dau_khuyen_mai")
    private LocalDateTime ngayBatDauKhuyenMai;

    @Column(name = "ngay_ket_thuc_khuyen_mai")
    private LocalDateTime ngayKetThucKhuyenMai;

    /** Bật/tắt khuyến mãi cấp sản phẩm (độc lập với trang_thai bán hàng). */
    @Column(name = "trang_thai_khuyen_mai")
    private Boolean trangThaiKhuyenMai;
}
