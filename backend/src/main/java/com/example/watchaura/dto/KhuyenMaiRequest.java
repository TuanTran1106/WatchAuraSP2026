package com.example.watchaura.dto;

import com.example.watchaura.entity.KhuyenMai;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
public class KhuyenMaiRequest {

    private Integer id;

    @NotBlank(message = "Mã khuyến mãi không được để trống")
    @Size(max = 50, message = "Mã khuyến mãi tối đa 50 ký tự")
    @Pattern(regexp = "^[A-Za-z0-9_\\-]+$", message = "Mã khuyến mãi chỉ gồm chữ cái, số, dấu gạch ngang và gạch dưới")
    private String maKhuyenMai;

    @NotBlank(message = "Tên chương trình không được để trống")
    @Size(max = 255, message = "Tên chương trình tối đa 255 ký tự")
    private String tenChuongTrinh;

    @Size(max = 500, message = "Mô tả tối đa 500 ký tự")
    private String moTa;

    private String danhMucApDung;

    @NotBlank(message = "Chọn loại giảm")
    private String loaiGiam;

    @NotNull(message = "Giá trị giảm không được để trống")
    @DecimalMin(value = "0.01", inclusive = true, message = "Giá trị giảm phải lớn hơn 0")
    private BigDecimal giaTriGiam;

    @NotNull(message = "Ngày bắt đầu không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime ngayBatDau;

    @NotNull(message = "Ngày kết thúc không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime ngayKetThuc;

    private KhuyenMai.PhamViApDung phamViApDung;

    private Boolean trangThai;
}
