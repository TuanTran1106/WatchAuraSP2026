package com.example.watchaura.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "serial_loi")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SerialLoi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_serial", unique = true)
    private String maSerial;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hoan_tra_id")
    private HoanTra hoanTra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hoa_don_chi_tiet_id")
    private HoaDonChiTiet hoaDonChiTiet;

    @Column(name = "san_pham_ten")
    private String sanPhamTen;

    @Column(name = "ly_do")
    private String lyDo;

    @Column(name = "trang_thai")
    private String trangThai = "CHUA_XU_LY";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_tao_id")
    private KhachHang nguoiTao;

    @CreationTimestamp
    @Column(name = "ngay_tao", updatable = false)
    private LocalDateTime ngayTao;

    @Column(name = "ngay_xu_ly")
    private LocalDateTime ngayXuLy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nguoi_xu_ly_id")
    private KhachHang nguoiXuLy;

    public static final String TRANG_THAI_CHUA_XU_LY = "CHUA_XU_LY";
    public static final String TRANG_THAI_DA_XU_LY = "DA_XU_LY";
    public static final String TRANG_THAI_HUY = "HUY";
}
