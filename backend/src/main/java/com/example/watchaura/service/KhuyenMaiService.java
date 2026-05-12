package com.example.watchaura.service;

import com.example.watchaura.entity.KhuyenMai;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface KhuyenMaiService {

    List<KhuyenMai> getActivePromotions(LocalDateTime now);

    Page<KhuyenMai> findAll(int page, int size);

    Page<KhuyenMai> searchPage(String q, Boolean trangThai, Pageable pageable);

    Page<KhuyenMai> searchPage(String q,
                              Boolean trangThai,
                              LocalDate fromDate,
                              LocalDate toDate,
                              KhuyenMai.PhamViApDung phamViApDung,
                              Pageable pageable);

    KhuyenMai findById(Integer id);

    KhuyenMai save(KhuyenMai khuyenMai);

    KhuyenMai update(Integer id, KhuyenMai khuyenMai);

    void delete(Integer id);

    void toggleTrangThai(Integer id);

    void deactivate(Integer id);

    boolean existsByMaKhuyenMai(String maKhuyenMai);

    boolean existsByMaKhuyenMaiAndIdNot(String maKhuyenMai, Integer id);
}
