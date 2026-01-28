package com.example.watchaura.service;

import com.example.watchaura.entity.KhuyenMai;

import java.util.List;

public interface KhuyenMaiService {

    List<KhuyenMai> findAll();

    KhuyenMai findById(Integer id);

    KhuyenMai save(KhuyenMai khuyenMai);

    KhuyenMai update(Integer id, KhuyenMai khuyenMai);

    void delete(Integer id);

    boolean existsByMaKhuyenMai(String maKhuyenMai);
}
