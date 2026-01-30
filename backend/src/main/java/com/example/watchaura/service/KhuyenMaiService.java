package com.example.watchaura.service;

import com.example.watchaura.entity.KhuyenMai;

import java.util.List;

public interface KhuyenMaiService {

    List<KhuyenMai> getAll();

    KhuyenMai getById(Integer id);

    KhuyenMai create(KhuyenMai khuyenMai);

    KhuyenMai update(Integer id, KhuyenMai khuyenMai);

    void delete(Integer id);
}
