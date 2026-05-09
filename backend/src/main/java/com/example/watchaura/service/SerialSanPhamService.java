package com.example.watchaura.service;

import com.example.watchaura.dto.HoaDonSerialSelectionDTO;
import com.example.watchaura.dto.SerialSelectionDTO;

import java.util.List;
import java.util.Map;

public interface SerialSanPhamService {

    HoaDonSerialSelectionDTO getSerialSelectionData(Integer hoaDonId);

    void assignSerialsToOrder(Integer hoaDonId, Map<Integer, List<Integer>> serialsByBienThe);

    List<SerialSelectionDTO> getAvailableSerialsBySanPhamChiTietId(Integer sanPhamChiTietId);
}
