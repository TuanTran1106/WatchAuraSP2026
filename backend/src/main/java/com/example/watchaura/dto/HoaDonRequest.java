package com.example.watchaura.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HoaDonRequest {
    @NotNull(message = "Khách hàng không được để trống")
    private Integer khachHangId;
    
    private Integer nhanVienId;
    private Integer voucherId;
    
    @NotBlank(message = "Phương thức thanh toán không được để trống")
    private String phuongThucThanhToan;
    
    @NotBlank(message = "Loại hóa đơn không được để trống")
    private String loaiHoaDon;
    
    @NotBlank(message = "Địa chỉ không được để trống")
    private String diaChi;
    
    @NotBlank(message = "Tên khách hàng không được để trống")
    private String tenKhachHang;
    
    @NotBlank(message = "Số điện thoại không được để trống")
    private String sdtKhachHang;
    
    private String ghiChu;
    
    private DiaChiGiaoHangRequest diaChiGiaoHang;

    /** Có thể null khi thêm nhanh từ form trên list; controller sẽ set thành empty list. */
    private List<HoaDonChiTietRequest> items;
}
