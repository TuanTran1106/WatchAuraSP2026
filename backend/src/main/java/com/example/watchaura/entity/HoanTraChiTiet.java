package com.example.watchaura.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@Table(name = "HoanTraChiTiet")
@JsonIgnoreProperties(ignoreUnknown = true, value = { "hibernateLazyInitializer", "handler" })
public class HoanTraChiTiet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_hoan_tra", nullable = false)
    private HoanTra hoanTra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_san_pham_chi_tiet", nullable = false)
    private SanPhamChiTiet sanPhamChiTiet;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_hoa_don_chi_tiet", nullable = false)
    private HoaDonChiTiet hoaDonChiTiet;

    @Column(name = "so_luong_hoan_tra", nullable = false)
    private Integer soLuongHoanTra;

    @Column(name = "don_gia_tai_thoi_diem_mua", precision = 18, scale = 2, nullable = false)
    private BigDecimal donGiaTaiThoiDiemMua;

    @Column(name = "so_tien_hoan", precision = 18, scale = 2, nullable = false)
    private BigDecimal soTienHoan;

    @Column(name = "hinh_anh", length = 1000)
    private String hinhAnh;

}
