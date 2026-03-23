package com.example.watchaura.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockWarningItem {

    private Integer sanPhamChiTietId;
    private String tenSanPham;
    private String bienThe;
    private Integer soLuongYeuCau;
    private Integer soLuongKhaDung;
    private Integer soLuongConLaiSauKhiDat;
    private BigDecimal giaBan;
    private Integer soLuongDuocDat;
}
