package com.example.watchaura.service;

import com.example.watchaura.dto.HoaDonDTO;
import com.example.watchaura.dto.HoaDonRequest;

import java.util.List;

public interface HoaDonService {
    List<HoaDonDTO> getAll();
    HoaDonDTO getById(Integer id);
    HoaDonDTO getByMaDonHang(String maDonHang);
    List<HoaDonDTO> getByKhachHangId(Integer khachHangId);
    List<HoaDonDTO> getByTrangThaiDonHang(String trangThaiDonHang);
    HoaDonDTO create(HoaDonRequest request);
    HoaDonDTO update(Integer id, HoaDonRequest request);
    HoaDonDTO updateTrangThaiDonHang(Integer id, String trangThaiDonHang);
    void delete(Integer id);
    String generateMaDonHang();
}
