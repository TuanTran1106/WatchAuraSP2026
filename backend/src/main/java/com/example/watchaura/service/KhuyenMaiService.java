package com.example.watchaura.service;

import com.example.watchaura.entity.KhuyenMai;

import java.util.List;

import org.springframework.data.domain.Page;

public interface KhuyenMaiService {

    Page<KhuyenMai> findAll(int page, int size);

    KhuyenMai findById(Integer id);

    KhuyenMai save(KhuyenMai khuyenMai);

    KhuyenMai update(Integer id, KhuyenMai khuyenMai);

    void delete(Integer id);

    boolean existsByMaKhuyenMai(String maKhuyenMai);
}
