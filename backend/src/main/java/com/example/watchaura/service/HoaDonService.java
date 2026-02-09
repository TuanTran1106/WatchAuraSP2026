package com.example.watchaura.service;

import com.example.watchaura.dto.HoaDonDTO;
import com.example.watchaura.dto.HoaDonRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface HoaDonService {
    List<HoaDonDTO> getAll();
    List<HoaDonDTO> search(String keyword);
    Page<HoaDonDTO> searchPage(String keyword, String trangThai, Pageable pageable);
    HoaDonDTO getById(Integer id);
    HoaDonDTO getByMaDonHang(String maDonHang);
    List<HoaDonDTO> getByKhachHangId(Integer khachHangId);
    List<HoaDonDTO> getByTrangThaiDonHang(String trangThaiDonHang);
    HoaDonDTO create(HoaDonRequest request);
    HoaDonDTO update(Integer id, HoaDonRequest request);
    HoaDonDTO updateTrangThaiDonHang(Integer id, String trangThaiDonHang);
    void delete(Integer id);
    String generateMaDonHang();
    byte[] exportPdf(HoaDonDTO dto);
}
