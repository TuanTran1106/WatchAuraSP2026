package com.example.watchaura.service;

import com.example.watchaura.dto.HoaDonChiTietDTO;

import java.util.List;

public interface HoaDonChiTietService {
    List<HoaDonChiTietDTO> getAll();
    HoaDonChiTietDTO getById(Integer id);
    List<HoaDonChiTietDTO> getByHoaDonId(Integer hoaDonId);
}
