package com.example.watchaura.service;

import com.example.watchaura.dto.SanPhamChiTietDTO;
import com.example.watchaura.dto.SanPhamDTO;
import com.example.watchaura.dto.SanPhamRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface SanPhamService {
    List<SanPhamDTO> getAllSanPham();

    Page<SanPhamDTO> getPage(Pageable pageable);

    Page<SanPhamDTO> searchPage(String keyword, Boolean trangThai, Pageable pageable);

    SanPhamDTO getSanPhamById(Integer id);

    SanPhamDTO createSanPham(SanPhamRequest request);

    SanPhamDTO updateSanPham(Integer id, SanPhamRequest request);

    SanPhamDTO updateSanPhamImage(Integer id, String newFilePath);

    void deleteSanPham(Integer id);

    void toggleTrangThai(Integer id);
}

