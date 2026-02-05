package com.example.watchaura.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamRequest {

    @NotBlank(message = "Mã sản phẩm không được để trống")
    private String maSanPham;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String tenSanPham;

    private String moTa;
    private String hinhAnh;

    @NotNull(message = "Thương hiệu không được để trống")
    private Integer idThuongHieu;

    @NotNull(message = "Danh mục không được để trống")
    private Integer idDanhMuc;

    private String phongCach;
    private Boolean trangThai = true;

    /** Giá bán (dùng khi tạo/cập nhật biến thể mặc định) */
    private BigDecimal giaBan;
    /** Số lượng tồn (dùng khi tạo/cập nhật biến thể mặc định) */
    private Integer soLuongTon;

    /** Màu sắc (biến thể mặc định) */
    private Integer idMauSac;
    /** Kích thước / Loại dây (biến thể mặc định) */
    private Integer idKichThuoc;
    /** Chất liệu dây (biến thể mặc định) */
    private Integer idChatLieuDay;
    /** Loại máy (biến thể mặc định) */
    private Integer idLoaiMay;

    /** Đường kính (mm) - biến thể mặc định */
    private Double duongKinh;
    /** Độ chịu nước (m) - biến thể mặc định */
    private Integer doChiuNuoc;
    /** Bề rộng dây (mm) - biến thể mặc định */
    private Double beRongDay;
    /** Trọng lượng (g) - biến thể mặc định */
    private Double trongLuong;
}