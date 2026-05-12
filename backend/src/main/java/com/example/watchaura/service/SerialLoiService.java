package com.example.watchaura.service;

import com.example.watchaura.dto.SerialLoiDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface SerialLoiService {

    SerialLoiDTO createSerialLoi(SerialLoiDTO dto);

    SerialLoiDTO getSerialLoiById(Integer id);

    SerialLoiDTO getSerialLoiByMaSerial(String maSerial);

    Page<SerialLoiDTO> getAllSerialLoi(Pageable pageable);

    Page<SerialLoiDTO> getSerialLoiByTrangThai(String trangThai, Pageable pageable);

    Page<SerialLoiDTO> searchSerialLoi(String trangThai, String keyword, Pageable pageable);

    List<SerialLoiDTO> getSerialLoiByHoanTraId(Integer hoanTraId);

    SerialLoiDTO xuLySerialLoi(Integer id, Integer nhanVienId);

    SerialLoiDTO huySerialLoi(Integer id, Integer nhanVienId);

    long countByTrangThai(String trangThai);

    Map<String, Long> getThongKe();
}
