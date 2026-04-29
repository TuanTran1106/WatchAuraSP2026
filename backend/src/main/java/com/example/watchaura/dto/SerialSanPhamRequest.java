package com.example.watchaura.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SerialSanPhamRequest {

    @NotNull(message = "ID sản phẩm chi tiết không được để trống")
    private Integer idSanPhamChiTiet;

    private List<String> serials;
}
