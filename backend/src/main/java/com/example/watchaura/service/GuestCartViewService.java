package com.example.watchaura.service;

import com.example.watchaura.dto.GioHangChiTietDTO;
import com.example.watchaura.dto.GioHangDTO;
import com.example.watchaura.entity.SanPhamChiTiet;
import com.example.watchaura.repository.SanPhamChiTietRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Build {@link GioHangDTO} từ session giỏ khách (Map spctId → số lượng).
 */
@Service
@RequiredArgsConstructor
public class GuestCartViewService {

    private final SanPhamChiTietRepository sanPhamChiTietRepository;

    public GioHangDTO buildCartDto(HttpSession session) {
        Map<Integer, Integer> cartSession = (Map<Integer, Integer>) session.getAttribute("cart");
        GioHangDTO dto = new GioHangDTO();
        dto.setItems(new ArrayList<>());
        dto.setTongTien(BigDecimal.ZERO);
        if (cartSession == null || cartSession.isEmpty()) {
            return dto;
        }
        List<GioHangChiTietDTO> lines = new ArrayList<>();
        BigDecimal tongTien = BigDecimal.ZERO;
        for (Map.Entry<Integer, Integer> entry : cartSession.entrySet()) {
            Optional<SanPhamChiTiet> opt = sanPhamChiTietRepository.findByIdWithDetails(entry.getKey());
            SanPhamChiTiet spct = opt.orElse(null);
            if (spct == null) {
                continue;
            }
            GioHangChiTietDTO line = toGuestLineDto(spct, entry.getValue());
            lines.add(line);
            if (line.getThanhTien() != null) {
                tongTien = tongTien.add(line.getThanhTien());
            }
        }
        dto.setItems(lines);
        dto.setTongTien(tongTien);
        return dto;
    }

    private GioHangChiTietDTO toGuestLineDto(SanPhamChiTiet spct, int soLuong) {
        GioHangChiTietDTO dto = new GioHangChiTietDTO();
        dto.setId(spct.getId());
        dto.setSanPhamChiTietId(spct.getId());
        dto.setGioHangId(null);
        BigDecimal gia = spct.getGiaBan() != null ? spct.getGiaBan() : BigDecimal.ZERO;
        dto.setGiaBan(gia);
        dto.setSoLuong(soLuong);
        dto.setSoLuongTon(spct.getSoLuongTon());
        dto.setSoLuongKhaDung(spct.getSoLuongKhaDung());
        if (spct.getSanPham() != null) {
            dto.setIdSanPham(spct.getSanPham().getId());
            dto.setMaSanPham(spct.getSanPham().getMaSanPham());
            dto.setTenSanPham(spct.getSanPham().getTenSanPham());
            dto.setHinhAnh(spct.getSanPham().getHinhAnh());
            if (spct.getSanPham().getDanhMuc() != null) {
                dto.setTenDanhMuc(spct.getSanPham().getDanhMuc().getTenDanhMuc());
            }
        }
        dto.setMoTaBienThe(buildMoTaBienTheGuest(spct));
        dto.setThanhTien(gia.multiply(BigDecimal.valueOf(soLuong)));
        return dto;
    }

    private static String buildMoTaBienTheGuest(SanPhamChiTiet spct) {
        if (spct == null) {
            return null;
        }
        List<String> parts = new ArrayList<>();
        if (spct.getMauSac() != null && spct.getMauSac().getTenMauSac() != null
                && !spct.getMauSac().getTenMauSac().isBlank()) {
            parts.add("Màu: " + spct.getMauSac().getTenMauSac().trim());
        }
        if (spct.getKichThuoc() != null && spct.getKichThuoc().getTenKichThuoc() != null
                && !spct.getKichThuoc().getTenKichThuoc().isBlank()) {
            parts.add("Kích thước: " + spct.getKichThuoc().getTenKichThuoc().trim());
        }
        if (spct.getChatLieuDay() != null && spct.getChatLieuDay().getTenChatLieu() != null
                && !spct.getChatLieuDay().getTenChatLieu().isBlank()) {
            parts.add("Dây: " + spct.getChatLieuDay().getTenChatLieu().trim());
        }
        if (spct.getLoaiMay() != null && spct.getLoaiMay().getTenLoaiMay() != null
                && !spct.getLoaiMay().getTenLoaiMay().isBlank()) {
            parts.add("Loại máy: " + spct.getLoaiMay().getTenLoaiMay().trim());
        }
        return parts.isEmpty() ? null : String.join(" · ", parts);
    }
}
