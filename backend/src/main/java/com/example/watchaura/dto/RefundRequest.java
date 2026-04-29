package com.example.watchaura.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundRequest {

    @NotNull(message = "ID hoàn trả không được để trống")
    private Integer hoanTraId;

    @NotNull(message = "Số tiền hoàn không được để trống")
    @Positive(message = "Số tiền hoàn phải lớn hơn 0")
    private BigDecimal soTienHoan;

    @NotBlank(message = "Phương thức hoàn tiền không được để trống")
    private String phuongThucHoanTien;

    private String ghiChu;
}
