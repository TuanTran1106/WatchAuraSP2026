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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "SanPhamChiTiet")
@JsonIgnoreProperties(ignoreUnknown = true, value = { "hibernateLazyInitializer", "handler" })
public class SanPhamChiTiet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_san_pham", nullable = false)
    private SanPham sanPham;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_mau_sac")
    private MauSac mauSac;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_kich_thuoc")
    private KichThuoc kichThuoc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_chat_lieu_day")
    private ChatLieuDay chatLieuDay;

    @Column(name = "so_luong_ton")
    private Integer soLuongTon;

    @Column(name = "so_luong_da_dat")
    private Integer soLuongDaDat;

    @Column(name = "gia_ban", precision = 18, scale = 2)
    private BigDecimal giaBan;

    @Column(name = "duong_kinh")
    private Double duongKinh;

    @Column(name = "do_chiu_nuoc")
    private Integer doChiuNuoc;

    @Column(name = "be_rong_day")
    private Double beRongDay;

    @Column(name = "trong_luong")
    private Double trongLuong;

    @Column(name = "trang_thai")
    private Boolean trangThai;

    /**
     * Số lượng khả dụng để bán (FIFO: không trừ giữ hàng).
     * Với kiểu FIFO, ai thanh toán trước được mua trước, không giữ hàng trước.
     */
    public Integer getSoLuongKhaDung() {
        return soLuongTon != null ? soLuongTon : 0;
    }
}
