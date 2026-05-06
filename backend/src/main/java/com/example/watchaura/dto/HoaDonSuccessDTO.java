package com.example.watchaura.dto;

import com.example.watchaura.entity.HoaDon;
import com.example.watchaura.entity.HoaDonChiTiet;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HoaDonSuccessDTO {
    private Integer id;
    private String maDonHang;
    private String tenKhachHang;
    private String email;
    private String sdtKhachHang;
    private String diaChi;
    private String ghiChu;
    private BigDecimal tongTienTamTinh;
    private BigDecimal tienGiam;
    private BigDecimal tongTienThanhToan;
    private BigDecimal phiVanChuyen;
    private String phuongThucThanhToan;
    private String trangThaiDonHang;
    private LocalDateTime ngayDat;
    private String maVoucher;
    private List<OrderItemDTO> items;

    public static HoaDonSuccessDTO fromHoaDon(HoaDon hoaDon, List<HoaDonChiTiet> chiTietList) {
        HoaDonSuccessDTO dto = new HoaDonSuccessDTO();
        dto.setId(hoaDon.getId());
        dto.setMaDonHang(hoaDon.getMaDonHang());
        dto.setTenKhachHang(hoaDon.getTenKhachHang());
        dto.setEmail(hoaDon.getEmail());
        dto.setSdtKhachHang(hoaDon.getSdtKhachHang());
        dto.setDiaChi(hoaDon.getDiaChi());
        dto.setGhiChu(hoaDon.getGhiChu());
        dto.setTongTienTamTinh(hoaDon.getTongTienTamTinh());
        dto.setTienGiam(hoaDon.getTienGiam());
        dto.setTongTienThanhToan(hoaDon.getTongTienThanhToan());
        dto.setPhiVanChuyen(hoaDon.getPhiVanChuyen());
        dto.setPhuongThucThanhToan(hoaDon.getPhuongThucThanhToan());
        dto.setTrangThaiDonHang(hoaDon.getTrangThaiDonHang());
        dto.setNgayDat(hoaDon.getNgayDat());
        
        if (hoaDon.getVoucher() != null) {
            dto.setMaVoucher(hoaDon.getVoucher().getMaVoucher());
        }
        
        if (chiTietList != null) {
            dto.setItems(chiTietList.stream()
                    .map(OrderItemDTO::fromChiTiet)
                    .collect(Collectors.toList()));
        }
        return dto;
    }
}
