package com.example.watchaura.service;

import com.example.watchaura.dto.SanPhamChiTietDTO;
import com.example.watchaura.dto.SanPhamChiTietRequest;


import java.util.List;

public interface SanPhamChiTietService {
    List<SanPhamChiTietDTO> getAllSanPhamChiTiet();

    SanPhamChiTietDTO getSanPhamChiTietById(Integer id);

    SanPhamChiTietDTO createSanPhamChiTiet(SanPhamChiTietRequest request);

    SanPhamChiTietDTO updateSanPhamChiTiet(Integer id, SanPhamChiTietRequest request);

    void deleteSanPhamChiTiet(Integer id);

    List<SanPhamChiTietDTO> getSanPhamChiTietBySanPhamId(Integer sanPhamId);
}

