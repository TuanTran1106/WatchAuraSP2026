package com.example.watchaura.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamDTO {

    private Integer id;
    private String maSanPham;
    private String tenSanPham;
    private String moTa;
    private String hinhAnh;
    private Integer idThuongHieu;
    private String tenThuongHieu;
    private Integer idDanhMuc;
    private String tenDanhMuc;
    private String phongCach;
    private Boolean trangThai;
}