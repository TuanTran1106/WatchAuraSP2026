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
@Table(name = "SerialSanPham")
@JsonIgnoreProperties(ignoreUnknown = true, value = { "hibernateLazyInitializer", "handler" })
public class SerialSanPham {

    public static final int TRANG_THAI_TRONG_KHO = 0;
    public static final int TRANG_THAI_DA_BAN = 1;
    public static final int TRANG_THAI_BAO_HANH = 2;
    public static final int TRANG_THAI_DA_TRA_HANG = 3;
    public static final int TRANG_THAI_LOI = 4;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_san_pham_chi_tiet", nullable = false)
    private SanPhamChiTiet sanPhamChiTiet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_hoa_don_chi_tiet")
    private HoaDonChiTiet hoaDonChiTiet;

    @Column(name = "ma_serial", nullable = false, length = 100, unique = true)
    private String maSerial;

    @Column(name = "trang_thai", nullable = false)
    private Integer trangThai;

    @Column(name = "ngay_xuat_kho")
    private LocalDateTime ngayXuatKho;

    @Column(name = "ngay_het_bao_hanh")
    private LocalDateTime ngayHetBaoHanh;

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;

    @Column(name = "ngay_tao")
    private LocalDateTime ngayTao;
}
