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
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "DiaChi")
public class DiaChi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_khach_hang", nullable = false)
    private KhachHang khachHang;

    @Column(name = "dia_chi_cu_the", length = 255)
    private String diaChiCuThe;

    @Column(name = "phuong_xa", length = 100)
    private String phuongXa;

    @Column(name = "quan_huyen", length = 100)
    private String quanHuyen;

    @Column(name = "tinh_thanh", length = 100)
    private String tinhThanh;

    @Column(name = "mac_dinh")
    private Boolean macDinh;
}
