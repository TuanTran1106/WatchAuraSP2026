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
public class SerialLoiDTO {

    private Integer id;
    private String maSerial;
    private Integer hoanTraId;
    private String maHoanTra;
    private Integer hoaDonChiTietId;
    private String sanPhamTen;
    private String lyDo;
    private String trangThai;
    private String trangThaiText;
    private String nguoiTaoTen;
    private LocalDateTime ngayTao;
    private LocalDateTime ngayXuLy;
    private String nguoiXuLyTen;
}
