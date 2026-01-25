package com.example.watchaura.service;

import com.example.watchaura.dto.GioHangDTO;
import com.example.watchaura.dto.GioHangRequest;

import java.util.List;

public interface GioHangService {
    List<GioHangDTO> getAll();
    GioHangDTO getById(Integer id);
    GioHangDTO getByKhachHangId(Integer khachHangId);
    GioHangDTO create(GioHangRequest request);
    GioHangDTO update(Integer id, GioHangRequest request);
    void delete(Integer id);
    GioHangDTO getOrCreateCart(Integer khachHangId);
}
