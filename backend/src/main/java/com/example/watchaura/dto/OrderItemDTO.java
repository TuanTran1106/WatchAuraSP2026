package com.example.watchaura.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    private Integer id;
    private String hinhAnh;
    private String tenSanPham;
    private String tenBienThe;
    private Integer soLuong;
    private BigDecimal thanhTien;

    public static OrderItemDTO fromChiTiet(com.example.watchaura.entity.HoaDonChiTiet ct) {
        OrderItemDTO dto = new OrderItemDTO();
        dto.setId(ct.getId());
        dto.setSoLuong(ct.getSoLuong());
        dto.setThanhTien(ct.getThanhTien());
        
        if (ct.getSanPhamChiTiet() != null) {
            var spct = ct.getSanPhamChiTiet();
            
            if (spct.getSanPham() != null) {
                dto.setTenSanPham(spct.getSanPham().getTenSanPham());
                dto.setHinhAnh(spct.getSanPham().getHinhAnh());
            }
            
            StringBuilder bienThe = new StringBuilder();
            if (spct.getMauSac() != null && spct.getMauSac().getTenMauSac() != null) {
                bienThe.append(spct.getMauSac().getTenMauSac());
            }
            if (spct.getKichThuoc() != null && spct.getKichThuoc().getTenKichThuoc() != null) {
                if (bienThe.length() > 0) bienThe.append(" / ");
                bienThe.append(spct.getKichThuoc().getTenKichThuoc());
            }
            if (spct.getChatLieuDay() != null && spct.getChatLieuDay().getTenChatLieu() != null) {
                if (bienThe.length() > 0) bienThe.append(" / ");
                bienThe.append(spct.getChatLieuDay().getTenChatLieu());
            }
            if (bienThe.length() > 0) {
                dto.setTenBienThe(bienThe.toString());
            }
        }
        return dto;
    }
}
