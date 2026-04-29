package com.example.watchaura.service;

import com.example.watchaura.dto.PhieuNhapKhoDTO;
import com.example.watchaura.dto.RefundRequest;
import com.example.watchaura.dto.SerialCheckResponse;

import java.math.BigDecimal;
import java.util.List;

public interface PhieuNhapKhoService {

    PhieuNhapKhoDTO createFromHoanTra(RefundRequest request, Integer nhanVienId);

    PhieuNhapKhoDTO getByHoanTraId(Integer hoanTraId);

    PhieuNhapKhoDTO getById(Integer id);

    List<PhieuNhapKhoDTO> getAll();

    SerialCheckResponse checkSerial(String maSerial);

    BigDecimal calculateRefundAmount(Integer hoanTraId);
}
