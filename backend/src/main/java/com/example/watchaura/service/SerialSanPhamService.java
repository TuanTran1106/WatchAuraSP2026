package com.example.watchaura.service;

import com.example.watchaura.dto.HoaDonSerialSelectionDTO;
import com.example.watchaura.dto.SerialSelectionDTO;
import com.example.watchaura.entity.SerialSanPham;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SerialSanPhamService {

    HoaDonSerialSelectionDTO getSerialSelectionData(Integer hoaDonId);

    void assignSerialsToOrder(Integer hoaDonId, Map<Integer, List<Integer>> serialsByBienThe);

    List<SerialSelectionDTO> getAvailableSerialsBySanPhamChiTietId(Integer sanPhamChiTietId);

    Optional<SerialSanPham> getSerialById(Integer serialId);
}
