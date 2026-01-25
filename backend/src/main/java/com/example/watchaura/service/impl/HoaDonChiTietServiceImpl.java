package com.example.watchaura.service.impl;

import com.example.watchaura.dto.HoaDonChiTietDTO;
import com.example.watchaura.entity.HoaDonChiTiet;
import com.example.watchaura.repository.HoaDonChiTietRepository;
import com.example.watchaura.service.HoaDonChiTietService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HoaDonChiTietServiceImpl implements HoaDonChiTietService {

    private final HoaDonChiTietRepository hoaDonChiTietRepository;

    @Override
    public List<HoaDonChiTietDTO> getAll() {
        return hoaDonChiTietRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public HoaDonChiTietDTO getById(Integer id) {
        HoaDonChiTiet chiTiet = hoaDonChiTietRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết hóa đơn với ID: " + id));
        return convertToDTO(chiTiet);
    }

    @Override
    public List<HoaDonChiTietDTO> getByHoaDonId(Integer hoaDonId) {
        return hoaDonChiTietRepository.findByHoaDonId(hoaDonId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private HoaDonChiTietDTO convertToDTO(HoaDonChiTiet chiTiet) {
        HoaDonChiTietDTO dto = new HoaDonChiTietDTO();
        dto.setId(chiTiet.getId());
        dto.setHoaDonId(chiTiet.getHoaDon().getId());
        dto.setSanPhamChiTietId(chiTiet.getSanPhamChiTiet().getId());
        
        if (chiTiet.getSanPhamChiTiet().getSanPham() != null) {
            dto.setTenSanPham(chiTiet.getSanPhamChiTiet().getSanPham().getTenSanPham());
        }
        
        dto.setSoLuong(chiTiet.getSoLuong());
        dto.setDonGia(chiTiet.getDonGia());
        
        if (dto.getDonGia() != null && dto.getSoLuong() != null) {
            dto.setThanhTien(dto.getDonGia().multiply(BigDecimal.valueOf(dto.getSoLuong())));
        } else {
            dto.setThanhTien(BigDecimal.ZERO);
        }

        return dto;
    }
}
