package com.example.watchaura.service;

import com.example.watchaura.dto.DanhGiaDTO;

import java.util.List;

public interface DanhGiaService {
    List<DanhGiaDTO> getBySanPhamChiTietId(Integer sanPhamChiTietId);
    boolean hasUserReviewed(Integer sanPhamChiTietId, Integer khachHangId);
    DanhGiaDTO getUserReview(Integer sanPhamChiTietId, Integer khachHangId);
}
