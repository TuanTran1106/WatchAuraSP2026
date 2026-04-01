package com.example.watchaura.service.impl;

import com.example.watchaura.dto.DanhGiaDTO;
import com.example.watchaura.entity.DanhGia;
import com.example.watchaura.entity.SanPhamChiTiet;
import com.example.watchaura.repository.DanhGiaRepository;
import com.example.watchaura.service.DanhGiaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DanhGiaServiceImpl implements DanhGiaService {

    private final DanhGiaRepository danhGiaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DanhGiaDTO> getBySanPhamId(Integer sanPhamId) {
        if (sanPhamId == null) {
            return List.of();
        }
        return danhGiaRepository.findBySanPhamIdOrderByNgayDanhGiaDesc(sanPhamId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DanhGiaDTO> getBySanPhamChiTietId(Integer sanPhamChiTietId) {
        return danhGiaRepository.findByIdSanPhamChiTietOrderByNgayDanhGiaDesc(sanPhamChiTietId)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasUserReviewed(Integer sanPhamChiTietId, Integer khachHangId) {
        if (sanPhamChiTietId == null || khachHangId == null) {
            return false;
        }
        return danhGiaRepository.existsBySanPhamChiTietIdAndKhachHangId(sanPhamChiTietId, khachHangId);
    }

    @Override
    @Transactional(readOnly = true)
    public DanhGiaDTO getUserReview(Integer sanPhamChiTietId, Integer khachHangId) {
        if (sanPhamChiTietId == null || khachHangId == null) {
            return null;
        }
        return danhGiaRepository.findBySanPhamChiTietIdAndKhachHangId(sanPhamChiTietId, khachHangId)
                .map(this::convertToDTO)
                .orElse(null);
    }

    private DanhGiaDTO convertToDTO(DanhGia danhGia) {
        DanhGiaDTO dto = new DanhGiaDTO();
        dto.setId(danhGia.getId());
        dto.setSanPhamChiTietId(danhGia.getIdSanPhamChiTiet());
        dto.setSoSao(danhGia.getSoSao());
        dto.setNoiDung(danhGia.getNoiDung());
        dto.setNgayDanhGia(danhGia.getNgayDanhGia());
        if (danhGia.getKhachHang() != null) {
            dto.setTenKhachHang(danhGia.getKhachHang().getTenNguoiDung());
        }
        SanPhamChiTiet spct = danhGia.getSanPhamChiTiet();
        if (spct != null) {
            if (spct.getSanPham() != null) {
                dto.setTenSanPham(spct.getSanPham().getTenSanPham());
            }
            dto.setMoTaBienThe(buildMoTaBienThe(spct));
        }
        return dto;
    }

    /** Cùng quy tắc với HoaDonServiceImpl.buildTenBienTheForChiTiet (đơn hàng / giỏ). */
    private static String buildMoTaBienThe(SanPhamChiTiet spct) {
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
        var lm = spct.getSanPham() != null ? spct.getSanPham().getLoaiMay() : null;
        if (lm != null && lm.getTenLoaiMay() != null && !lm.getTenLoaiMay().isBlank()) {
            parts.add("Loại máy: " + lm.getTenLoaiMay().trim());
        }
        return parts.isEmpty() ? null : String.join(" · ", parts);
    }
}
