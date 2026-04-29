package com.example.watchaura.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ChiTietPhieuNhapKho")
public class ChiTietPhieuNhapKho {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_phieu_nhap_kho", nullable = false)
    private PhieuNhapKho phieuNhapKho;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_san_pham_chi_tiet", nullable = false)
    private SanPhamChiTiet sanPhamChiTiet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_serial")
    private SerialSanPham serialSanPham;

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;

    @Column(name = "don_gia", precision = 18, scale = 2)
    private BigDecimal donGia;

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;
}
