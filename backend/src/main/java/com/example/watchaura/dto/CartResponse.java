package com.example.watchaura.dto;

import com.example.watchaura.entity.HoaDonChiTiet;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class CartResponse {

    private List<HoaDonChiTiet> items;

    private BigDecimal tamTinh;

    private BigDecimal giamGia;

    private BigDecimal tongThanhToan;

}