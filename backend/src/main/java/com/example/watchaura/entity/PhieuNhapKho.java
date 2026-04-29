package com.example.watchaura.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "PhieuNhapKho")
public class PhieuNhapKho {

    public static final String LOAI_TU_HoanTra = "TU_HOAN_TRA";
    public static final String LOAI_NHAP_KHO_BINH_THUONG = "NHAP_KHO";

    public static final String PHUONG_THUC_TIEN_MAT = "TIEN_MAT";
    public static final String PHUONG_THUC_CHUYEN_KHOAN = "CHUYEN_KHOAN";
    public static final String PHUONG_THUC_VI_DIEN_TU = "VI_DIEN_TU";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_phieu", nullable = false, unique = true, length = 50)
    private String maPhieu;

    @Column(name = "loai_phieu", nullable = false, length = 50)
    private String loaiPhieu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_hoan_tra")
    private HoanTra hoanTra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nhan_vien", nullable = false)
    private KhachHang nhanVien;

    @Column(name = "so_tien_hoan", precision = 18, scale = 2)
    private BigDecimal soTienHoan;

    @Column(name = "phuong_thuc_hoan_tien", length = 50)
    private String phuongThucHoanTien;

    @Column(name = "ghi_chu", length = 500)
    private String ghiChu;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;

    @OneToMany(mappedBy = "phieuNhapKho", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChiTietPhieuNhapKho> chiTietList = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (ngayTao == null) {
            ngayTao = LocalDateTime.now();
        }
    }
}
