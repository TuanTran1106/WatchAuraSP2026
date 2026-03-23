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
    private String maSanPham;
    private String tenSanPham;
    private String hinhAnh;
    private BigDecimal giaBan;
    private Integer soLuong;
    /** Số lượng tồn kho (để hiển thị giới hạn trên UI) */
    private Integer soLuongTon;
    /** Số lượng khả dụng = tồn kho - đã đặt (giữ hàng) */
    private Integer soLuongKhaDung;
    private BigDecimal thanhTien;
    private String tenDanhMuc;
    /** Mô tả biến thể ngắn (màu, kích thước, dây, loại máy) để phân biệt trong giỏ */
    private String moTaBienThe;
}
