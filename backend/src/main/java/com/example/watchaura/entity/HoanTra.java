package com.example.watchaura.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@Table(name = "HoanTra")
@JsonIgnoreProperties(ignoreUnknown = true, value = { "hibernateLazyInitializer", "handler" })
public class HoanTra {

    public static final String TRANG_THAI_CHO_XU_LY = "CHO_XU_LY";
    public static final String TRANG_THAI_DANG_XU_LY = "DANG_XU_LY";
    public static final String TRANG_THAI_DA_XU_LY = "DA_XU_LY";
    public static final String TRANG_THAI_TU_CHOI = "TU_CHOI";
    public static final String TRANG_THAI_DA_DUYET = "DA_DUYET";
    public static final String TRANG_THAI_DA_NHAN_HANG = "DA_NHAN_HANG";
    public static final String TRANG_THAI_DA_HOAN_TIEN = "DA_HOAN_TIEN";

    // === DOI_HANG specific statuses ===
    // LUONG: CHỜ DUYỆT → ĐÃ DUYỆT → ĐÃ NHẬN HÀNG → CHỌN SERIAL MỚI → ĐÃ ĐỔI → KẾT THÚC
    public static final String TRANG_THAI_CHO_DUYET_DOI = "CHO_DUYET_DOI";
    public static final String TRANG_THAI_DA_DUYET_DOI = "DA_DUYET_DOI";
    public static final String TRANG_THAI_DA_NHAN_HANG_DOI = "DA_NHAN_HANG_DOI";
    public static final String TRANG_THAI_CHON_SERIAL_MOI = "CHON_SERIAL_MOI";
    public static final String TRANG_THAI_DA_DOI = "DA_DOI";
    public static final String TRANG_THAI_KET_THUC = "KET_THUC";

    public static final String LOAI_TRA_HANG = "TRA_HANG";
    public static final String LOAI_DOI_HANG = "DOI_HANG";

    public static final String PHUONG_THUC_TIEN_MAT = "TIEN_MAT";
    public static final String PHUONG_THUC_CHUYEN_KHOAN = "CHUYEN_KHOAN";
    public static final String PHUONG_THUC_VI_DIEN_TU = "VI_DIEN_TU";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "ma_hoan_tra", nullable = false, unique = true, length = 50)
    private String maHoanTra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_hoa_don", nullable = false)
    private HoaDon hoaDon;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang", nullable = false)
    private KhachHang khachHang;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_nhan_vien_xu_ly")
    private KhachHang nhanVienXuLy;

    @Column(name = "ly_do", nullable = false, length = 500)
    private String lyDo;

    @Column(name = "so_tien_hoan_tra", precision = 18, scale = 2)
    private BigDecimal soTienHoanTra;

    @Column(name = "trang_thai", nullable = false, length = 50)
    private String trangThai;

    @Column(name = "ghi_chu_xu_ly", length = 500)
    private String ghiChuXuLy;

    @Column(name = "loai_hoan_tra", length = 20)
    private String loaiHoanTra;

    @Column(name = "ngay_yeu_cau")
    private LocalDateTime ngayYeuCau;

    @Column(name = "ngay_xu_ly")
    private LocalDateTime ngayXuLy;

    @Column(name = "phuong_thuc_hoan_tien", length = 50)
    private String phuongThucHoanTien;

    @Column(name = "so_tien_hoan_thuc_te", precision = 18, scale = 2)
    private java.math.BigDecimal soTienHoanThucTe;

    @Column(name = "ghi_chu_hoan_tien", length = 500)
    private String ghiChuHoanTien;

    @Column(name = "ngay_hoan_tien")
    private LocalDateTime ngayHoanTien;

    @OneToMany(mappedBy = "hoanTra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<HoanTraChiTiet> chiTietList = new ArrayList<>();
}
