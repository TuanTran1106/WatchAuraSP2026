package com.example.watchaura.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DanhGiaDTO {
    private Integer id;
    private Integer sanPhamChiTietId;
    private String tenSanPham;
    private String moTaBienThe;
    private String tenKhachHang;
    private Integer soSao;
    private String noiDung;
    private LocalDateTime ngayDanhGia;
}
