package com.example.watchaura.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SerialSelectionDTO {

    private Integer id;
    private String maSerial;
    private Integer sanPhamChiTietId;
    private String tenSanPham;
    private String tenBienThe;
    private Integer trangThai;
    private LocalDateTime ngayTao;
    private boolean daChon;
}
