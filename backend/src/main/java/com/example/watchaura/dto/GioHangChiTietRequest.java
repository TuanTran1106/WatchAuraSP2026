package com.example.watchaura.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GioHangChiTietRequest {
    @NotNull(message = "Giỏ hàng không được để trống")
    private Integer gioHangId;
    
    @NotNull(message = "Sản phẩm chi tiết không được để trống")
    private Integer sanPhamChiTietId;
    
    @NotNull(message = "Số lượng không được để trống")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer soLuong;
}
