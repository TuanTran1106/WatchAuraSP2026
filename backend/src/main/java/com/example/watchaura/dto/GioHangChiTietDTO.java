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
public class GioHangChiTietDTO {
    private Integer id;
    private Integer gioHangId;
    private Integer sanPhamChiTietId;
    private Integer idSanPham;
    private String tenSanPham;
    private String hinhAnh;
    private BigDecimal giaBan;
    private Integer soLuong;
    /** Số lượng tồn kho (để hiển thị giới hạn trên UI) */
    private Integer soLuongTon;
    private BigDecimal thanhTien;
}
