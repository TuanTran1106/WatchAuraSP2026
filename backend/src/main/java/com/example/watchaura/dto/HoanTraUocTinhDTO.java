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
public class HoanTraUocTinhDTO {
    
    private Integer idHoaDon;
    private String maDonHang;
    
    // Thông tin tổng hợp
    private BigDecimal tongTienMatHang;          // Tổng tiền mặt hàng hoàn
    private BigDecimal phiVanChuyen;              // Phí vận chuyển (sẽ bị trừ)
    private BigDecimal tienVoucherGiam;          // Số tiền voucher được trừ (theo tỷ lệ)
    
    // Kết quả
    private BigDecimal soTienHoanUocTinh;        // Số tiền hoàn ước tính = tongTienMatHang - phiVanChuyen - tienVoucherGiam
    private Integer soLuongHoan;                 // Số lượng sản phẩm hoàn
    
    // Thông tin hóa đơn gốc (để so sánh)
    private BigDecimal tongTienTamTinhGoc;       // Tổng tiền tạm tính gốc của đơn
    private BigDecimal tienGiamGoc;              // Tiền giảm gốc của đơn
    private BigDecimal tongTienThanhToanGoc;     // Tổng tiền thanh toán gốc
    
    private Boolean coTheHoanTra;               // Có thể hoàn trả không
    private String loiThuong;
}
