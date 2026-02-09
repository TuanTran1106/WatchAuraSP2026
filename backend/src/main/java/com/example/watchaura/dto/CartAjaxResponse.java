package com.example.watchaura.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartAjaxResponse {
    private boolean success;
    private String error;
    private BigDecimal tongTien;
    private int soLuongGioHang;
    private Integer itemId;
    private Integer soLuong;
    private BigDecimal thanhTien;
}
