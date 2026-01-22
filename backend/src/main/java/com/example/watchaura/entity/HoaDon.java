package com.example.watchaura.entity;

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
@Table(name = "HoaDon")
public class HoaDon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_don_hang", nullable = false, unique = true, length = 50)
    private String maDonHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang", nullable = false)
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nhan_vien")
    private KhachHang nhanVien;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_voucher")
    private Voucher voucher;

    @Column(name = "tong_tien_tam_tinh", precision = 18, scale = 2, nullable = false)
    private BigDecimal tongTienTamTinh;

    @Column(name = "tien_giam", precision = 18, scale = 2)
    private BigDecimal tienGiam;

    @Column(name = "tong_tien_thanh_toan", precision = 18, scale = 2, nullable = false)
    private BigDecimal tongTienThanhToan;

    @Column(name = "phuong_thuc_thanh_toan", length = 50, nullable = false)
    private String phuongThucThanhToan;

    @Column(name = "loai_hoa_don", length = 50, nullable = false)
    private String loaiHoaDon;

    @Column(name = "trang_thai")
    private Boolean trangThai;

    @Column(name = "trang_thai_don_hang", length = 50, nullable = false)
    private String trangThaiDonHang;

    @Column(name = "ngay_dat")
    private LocalDateTime ngayDat;

    @Column(name = "dia_chi", length = 255, nullable = false)
    private String diaChi;

    @Column(name = "ten_khach_hang", length = 100, nullable = false)
    private String tenKhachHang;

    @Column(name = "sdt_khach_hang", length = 20, nullable = false)
    private String sdtKhachHang;

    @Column(name = "ghi_chu", length = 255)
    private String ghiChu;
}
