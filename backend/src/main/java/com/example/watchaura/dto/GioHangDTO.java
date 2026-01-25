package com.example.watchaura.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GioHangDTO {
    private Integer id;
    private Integer khachHangId;
    private String tenKhachHang;
    private Boolean trangThai;
    private LocalDateTime ngayTao;
    private BigDecimal tongTien;
    private List<GioHangChiTietDTO> items;
}
