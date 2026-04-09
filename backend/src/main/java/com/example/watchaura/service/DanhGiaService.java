package com.example.watchaura.service;

import com.example.watchaura.dto.DanhGiaDTO;

import java.util.List;
import java.util.Set;

public interface DanhGiaService {
    /** Đánh giá của mọi biến thể thuộc cùng một sản phẩm (trang chi tiết SP). */
    List<DanhGiaDTO> getBySanPhamId(Integer sanPhamId);

    List<DanhGiaDTO> getBySanPhamChiTietId(Integer sanPhamChiTietId);
    boolean hasUserReviewed(Integer sanPhamChiTietId, Integer khachHangId);
    DanhGiaDTO getUserReview(Integer sanPhamChiTietId, Integer khachHangId);

    /** Batch check: trả về Set các sanPhamChiTietId mà user đã đánh giá */
    Set<Integer> getReviewedProductIds(Set<Integer> sanPhamChiTietIds, Integer khachHangId);
}
