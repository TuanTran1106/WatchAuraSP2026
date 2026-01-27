package com.example.watchaura.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
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
public class SanPhamChiTietRequest {
    @NotNull(message = "Sản phẩm không được để trống")
    private Integer idSanPham;

    private Integer idMauSac;
    private Integer idKichThuoc;
    private Integer idChatLieuDay;
    private Integer idLoaiMay;

    @Min(value = 0, message = "Số lượng tồn phải >= 0")
    private Integer soLuongTon = 0;

    @NotNull(message = "Giá bán không được để trống")
    @DecimalMin(value = "0.0", message = "Giá bán phải > 0")
    private BigDecimal giaBan;

    private Double duongKinh;
    private Integer doChiuNuoc;
    private Double beRongDay;
    private Double trongLuong;
    private Boolean trangThai = true;
}