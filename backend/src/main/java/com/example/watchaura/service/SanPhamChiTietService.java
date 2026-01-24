package com.example.watchaura.service;

import com.example.watchaura.dto.SanPhamChiTietDTO;
import com.example.watchaura.dto.SanPhamChiTietRequest;
import org.springframework.stereotype.Service;

import java.util.List;

public interface SanPhamChiTietService {
    List<SanPhamChiTietDTO> getAllSanPhamChiTiet();

    SanPhamChiTietDTO getSanPhamChiTietById(Integer id);

    SanPhamChiTietDTO createSanPhamChiTiet(SanPhamChiTietRequest request);

    SanPhamChiTietDTO updateSanPhamChiTiet(Integer id, SanPhamChiTietRequest request);

    void deleteSanPhamChiTiet(Integer id);
}
