package com.example.watchaura.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GioHangRequest {
    @NotNull(message = "Khách hàng không được để trống")
    private Integer khachHangId;
    
    private Boolean trangThai = true;
}
