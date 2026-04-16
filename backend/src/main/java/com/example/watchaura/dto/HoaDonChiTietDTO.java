package com.example.watchaura.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HoaDonChiTietDTO {
    private Integer id;
    private Integer hoaDonId;
    private Integer sanPhamChiTietId;
    private String tenSanPham;
    private Integer soLuong;
    private BigDecimal donGia;
    /** Giá gốc chưa khuyến mãi (lấy từ SanPhamChiTiet.giaBan tại thời điểm đặt). */
    private BigDecimal giaGoc;
    private BigDecimal thanhTien;
    private String hinhAnh;
    private String tenBienThe;
    private Integer soLuongKhaDung; // tồn kho khả dụng hiện tại của sản phẩm chi tiết
    private List<String> maSerials;
}
