package com.example.watchaura.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HoanTraRequest {

    @NotNull(message = "ID hóa đơn không được để trống")
    private Integer idHoaDon;

    @NotBlank(message = "Lý do không được để trống")
    @Size(max = 500, message = "Lý do không được vượt quá 500 ký tự")
    private String lyDo;

    private String ghiChuXuLy;

    @NotBlank(message = "Loại hoàn trả không được để trống")
    private String loaiHoanTra;

    private List<HoanTraChiTietRequest> chiTietList;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HoanTraChiTietRequest {
        
        @NotNull(message = "ID sản phẩm chi tiết không được để trống")
        private Integer idSanPhamChiTiet;

        @NotNull(message = "ID hóa đơn chi tiết không được để trống")
        private Integer idHoaDonChiTiet;

        @NotNull(message = "Số lượng hoàn trả không được để trống")
        private Integer soLuongHoanTra;

        private BigDecimal donGiaTaiThoiDiemMua;

        private List<String> serialsHoanTra;

        private String hinhAnh;

        // === DOI_HANG: Serial mới thay thế ===
        private String serialMoi;

        // Trạng thái serial cũ:
        // - true: serial cũ bị lỗi (không cấp serial mới)
        // - false: serial mới được đổi
        private Boolean serialCuLoi;
    }
}
