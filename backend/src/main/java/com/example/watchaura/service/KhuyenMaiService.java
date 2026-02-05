package com.example.watchaura.service;

import com.example.watchaura.entity.KhuyenMai;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface KhuyenMaiService {

    List<KhuyenMai> getAll();

    /** Khuyến mãi đang diễn ra (trong khoảng ngày, trạng thái bật) */
    List<KhuyenMai> getActivePromotions(LocalDateTime now);

    Page<KhuyenMai> getPage(Pageable pageable);

    Page<KhuyenMai> searchPage(String keyword, Boolean trangThai, Pageable pageable);

    KhuyenMai getById(Integer id);

    KhuyenMai create(KhuyenMai khuyenMai);

    KhuyenMai update(Integer id, KhuyenMai khuyenMai);

    void delete(Integer id);

    void toggleTrangThai(Integer id);
}
