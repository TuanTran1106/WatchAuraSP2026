package com.example.watchaura.service;

import com.example.watchaura.dto.DiaChiGiaoHangDTO;
import com.example.watchaura.dto.DiaChiGiaoHangRequest;

import java.util.List;

public interface DiaChiGiaoHangService {
    List<DiaChiGiaoHangDTO> getAll();
    DiaChiGiaoHangDTO getById(Integer id);
    DiaChiGiaoHangDTO getByHoaDonId(Integer hoaDonId);
    DiaChiGiaoHangDTO create(Integer hoaDonId, DiaChiGiaoHangRequest request);
    DiaChiGiaoHangDTO update(Integer id, DiaChiGiaoHangRequest request);
    void delete(Integer id);
}
