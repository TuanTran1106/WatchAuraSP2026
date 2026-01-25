package com.example.watchaura.service;

import com.example.watchaura.dto.GioHangChiTietDTO;
import com.example.watchaura.dto.GioHangChiTietRequest;

import java.util.List;

public interface GioHangChiTietService {
    List<GioHangChiTietDTO> getAll();
    GioHangChiTietDTO getById(Integer id);
    List<GioHangChiTietDTO> getByGioHangId(Integer gioHangId);
    GioHangChiTietDTO create(GioHangChiTietRequest request);
    GioHangChiTietDTO update(Integer id, GioHangChiTietRequest request);
    void delete(Integer id);
}
