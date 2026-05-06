package com.example.watchaura.dto;

import com.example.watchaura.entity.KhuyenMai;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
public class KhuyenMaiRequest {

    private Integer id;

    private String maKhuyenMai;

    @NotBlank(message = "Tên chương trình không được để trống")
    private String tenChuongTrinh;

    private String moTa;

    private String danhMucApDung;

    @NotBlank(message = "Chọn loại giảm")
    private String loaiGiam;

    @NotNull(message = "Giá trị giảm không được để trống")
    private BigDecimal giaTriGiam;

    private BigDecimal giamToiDa;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime ngayBatDau;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime ngayKetThuc;

    private BigDecimal donToiThieu;

    private Integer gioiHanLuotDung;

    private Integer soLuotDaDung;

    private KhuyenMai.PhamViApDung phamViApDung;

    private Boolean trangThai;
}
