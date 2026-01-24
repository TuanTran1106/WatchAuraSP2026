package com.example.watchaura.service;

import com.example.watchaura.dto.SanPhamDTO;
import com.example.watchaura.dto.SanPhamRequest;
import org.springframework.stereotype.Service;

import java.util.List;


public interface SanPhamService {

    List<SanPhamDTO> getAllSanPham();

    SanPhamDTO getSanPhamById(Integer id);

    SanPhamDTO createSanPham(SanPhamRequest request);

    SanPhamDTO updateSanPham(Integer id, SanPhamRequest request);

    void deleteSanPham(Integer id);
}
