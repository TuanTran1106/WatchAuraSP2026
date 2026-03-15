package com.example.watchaura.service.impl;

import com.example.watchaura.dto.DanhGiaDTO;
import com.example.watchaura.entity.DanhGia;
import com.example.watchaura.repository.DanhGiaRepository;
import com.example.watchaura.service.DanhGiaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DanhGiaServiceImpl implements DanhGiaService {

    private final DanhGiaRepository danhGiaRepository;

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
        return dto;
    }
}
