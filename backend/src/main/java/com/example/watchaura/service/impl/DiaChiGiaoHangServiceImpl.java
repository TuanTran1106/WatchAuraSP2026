package com.example.watchaura.service.impl;

import com.example.watchaura.dto.DiaChiGiaoHangDTO;
import com.example.watchaura.dto.DiaChiGiaoHangRequest;
import com.example.watchaura.entity.DiaChiGiaoHang;
import com.example.watchaura.entity.HoaDon;
import com.example.watchaura.repository.DiaChiGiaoHangRepository;
import com.example.watchaura.repository.HoaDonRepository;
import com.example.watchaura.service.DiaChiGiaoHangService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiaChiGiaoHangServiceImpl implements DiaChiGiaoHangService {

    private final DiaChiGiaoHangRepository diaChiGiaoHangRepository;
    private final HoaDonRepository hoaDonRepository;

    @Override
    public List<DiaChiGiaoHangDTO> getAll() {
        return diaChiGiaoHangRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public DiaChiGiaoHangDTO getById(Integer id) {
        DiaChiGiaoHang diaChi = diaChiGiaoHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ giao hàng với ID: " + id));
        return convertToDTO(diaChi);
    }

    @Override
    public DiaChiGiaoHangDTO getByHoaDonId(Integer hoaDonId) {
        DiaChiGiaoHang diaChi = diaChiGiaoHangRepository.findByHoaDonId(hoaDonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ giao hàng cho hóa đơn"));
        return convertToDTO(diaChi);
    }

    @Override
    @Transactional
    public DiaChiGiaoHangDTO create(Integer hoaDonId, DiaChiGiaoHangRequest request) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        DiaChiGiaoHang diaChi = new DiaChiGiaoHang();
        diaChi.setHoaDon(hoaDon);
        diaChi.setTenNguoiNhan(request.getTenNguoiNhan());
        diaChi.setSdtNguoiNhan(request.getSdtNguoiNhan());
        diaChi.setDiaChiCuThe(request.getDiaChiCuThe());
        diaChi.setPhuongXa(request.getPhuongXa());
        diaChi.setQuanHuyen(request.getQuanHuyen());
        diaChi.setTinhThanh(request.getTinhThanh());
        diaChi.setGhiChu(request.getGhiChu());

        return convertToDTO(diaChiGiaoHangRepository.save(diaChi));
    }

    @Override
    @Transactional
    public DiaChiGiaoHangDTO update(Integer id, DiaChiGiaoHangRequest request) {
        DiaChiGiaoHang diaChi = diaChiGiaoHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ giao hàng"));

        if (request.getTenNguoiNhan() != null) {
            diaChi.setTenNguoiNhan(request.getTenNguoiNhan());
        }
        if (request.getSdtNguoiNhan() != null) {
            diaChi.setSdtNguoiNhan(request.getSdtNguoiNhan());
        }
        if (request.getDiaChiCuThe() != null) {
            diaChi.setDiaChiCuThe(request.getDiaChiCuThe());
        }
        if (request.getPhuongXa() != null) {
            diaChi.setPhuongXa(request.getPhuongXa());
        }
        if (request.getQuanHuyen() != null) {
            diaChi.setQuanHuyen(request.getQuanHuyen());
        }
        if (request.getTinhThanh() != null) {
            diaChi.setTinhThanh(request.getTinhThanh());
        }
        if (request.getGhiChu() != null) {
            diaChi.setGhiChu(request.getGhiChu());
        }

        return convertToDTO(diaChiGiaoHangRepository.save(diaChi));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (!diaChiGiaoHangRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy địa chỉ giao hàng với ID: " + id);
        }
        diaChiGiaoHangRepository.deleteById(id);
    }

    private DiaChiGiaoHangDTO convertToDTO(DiaChiGiaoHang diaChi) {
        DiaChiGiaoHangDTO dto = new DiaChiGiaoHangDTO();
        dto.setId(diaChi.getId());
        dto.setHoaDonId(diaChi.getHoaDon().getId());
        dto.setTenNguoiNhan(diaChi.getTenNguoiNhan());
        dto.setSdtNguoiNhan(diaChi.getSdtNguoiNhan());
        dto.setDiaChiCuThe(diaChi.getDiaChiCuThe());
        dto.setPhuongXa(diaChi.getPhuongXa());
        dto.setQuanHuyen(diaChi.getQuanHuyen());
        dto.setTinhThanh(diaChi.getTinhThanh());
        dto.setGhiChu(diaChi.getGhiChu());
        return dto;
    }
}
