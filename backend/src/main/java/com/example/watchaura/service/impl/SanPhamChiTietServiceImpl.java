package com.example.watchaura.service.impl;


import com.example.watchaura.dto.SanPhamChiTietDTO;
import com.example.watchaura.dto.SanPhamChiTietRequest;
import com.example.watchaura.entity.SanPham;
import com.example.watchaura.entity.SanPhamChiTiet;
import com.example.watchaura.repository.*;
import com.example.watchaura.service.SanPhamChiTietService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class SanPhamChiTietServiceImpl implements SanPhamChiTietService {

    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final SanPhamRepository sanPhamRepository;
    private final MauSacRepository mauSacRepository;
    private final KichThuocRepository kichThuocRepository;
    private final ChatLieuDayRepository chatLieuDayRepository;
    private final LoaiMayRepository loaiMayRepository;

    @Override
    public List<SanPhamChiTietDTO> getAllSanPhamChiTiet() {
        return sanPhamChiTietRepository.findAllWithDetails()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SanPhamChiTietDTO getSanPhamChiTietById(Integer id) {
        SanPhamChiTiet spct = sanPhamChiTietRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm chi tiết với ID: " + id));
        return convertToDTO(spct);
    }

    @Override
    @Transactional
    public SanPhamChiTietDTO createSanPhamChiTiet(SanPhamChiTietRequest request) {

        SanPham sanPham = sanPhamRepository.findById(request.getIdSanPham())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + request.getIdSanPham()));

        SanPhamChiTiet spct = new SanPhamChiTiet();
        spct.setSanPham(sanPham);

        if (request.getIdMauSac() != null) {
            spct.setMauSac(
                    mauSacRepository.findById(request.getIdMauSac())
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy màu sắc"))
            );
        }

        if (request.getIdKichThuoc() != null) {
            spct.setKichThuoc(
                    kichThuocRepository.findById(request.getIdKichThuoc())
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy kích thước"))
            );
        }

        if (request.getIdChatLieuDay() != null) {
            spct.setChatLieuDay(
                    chatLieuDayRepository.findById(request.getIdChatLieuDay())
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy chất liệu dây"))
            );
        }

        if (request.getIdLoaiMay() != null) {
            spct.setLoaiMay(
                    loaiMayRepository.findById(request.getIdLoaiMay())
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy loại máy"))
            );
        }

        spct.setSoLuongTon(request.getSoLuongTon());
        spct.setGiaBan(request.getGiaBan());
        spct.setDuongKinh(request.getDuongKinh());
        spct.setDoChiuNuoc(request.getDoChiuNuoc());
        spct.setBeRongDay(request.getBeRongDay());
        spct.setTrongLuong(request.getTrongLuong());
        spct.setTrangThai(request.getTrangThai());

        return convertToDTO(sanPhamChiTietRepository.save(spct));
    }

    @Override
    @Transactional
    public SanPhamChiTietDTO updateSanPhamChiTiet(Integer id, SanPhamChiTietRequest request) {

        SanPhamChiTiet spct = sanPhamChiTietRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm chi tiết"));

        SanPham sanPham = sanPhamRepository.findById(request.getIdSanPham())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        spct.setSanPham(sanPham);

        spct.setMauSac(request.getIdMauSac() == null ? null :
                mauSacRepository.findById(request.getIdMauSac()).orElseThrow());

        spct.setKichThuoc(request.getIdKichThuoc() == null ? null :
                kichThuocRepository.findById(request.getIdKichThuoc()).orElseThrow());

        spct.setChatLieuDay(request.getIdChatLieuDay() == null ? null :
                chatLieuDayRepository.findById(request.getIdChatLieuDay()).orElseThrow());

        spct.setLoaiMay(request.getIdLoaiMay() == null ? null :
                loaiMayRepository.findById(request.getIdLoaiMay()).orElseThrow());

        spct.setSoLuongTon(request.getSoLuongTon());
        spct.setGiaBan(request.getGiaBan());
        spct.setDuongKinh(request.getDuongKinh());
        spct.setDoChiuNuoc(request.getDoChiuNuoc());
        spct.setBeRongDay(request.getBeRongDay());
        spct.setTrongLuong(request.getTrongLuong());
        spct.setTrangThai(request.getTrangThai());

        return convertToDTO(sanPhamChiTietRepository.save(spct));
    }

    @Override
    @Transactional
    public void deleteSanPhamChiTiet(Integer id) {
        if (!sanPhamChiTietRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy sản phẩm chi tiết");
        }
        sanPhamChiTietRepository.deleteById(id);
    }

    private SanPhamChiTietDTO convertToDTO(SanPhamChiTiet spct) {
        SanPhamChiTietDTO dto = new SanPhamChiTietDTO();
        dto.setId(spct.getId());
        dto.setSoLuongTon(spct.getSoLuongTon());
        dto.setGiaBan(spct.getGiaBan());
        dto.setDuongKinh(spct.getDuongKinh());
        dto.setDoChiuNuoc(spct.getDoChiuNuoc());
        dto.setBeRongDay(spct.getBeRongDay());
        dto.setTrongLuong(spct.getTrongLuong());
        dto.setTrangThai(spct.getTrangThai());

        if (spct.getSanPham() != null) {
            dto.setIdSanPham(spct.getSanPham().getId());
            dto.setTenSanPham(spct.getSanPham().getTenSanPham());
            dto.setMaSanPham(spct.getSanPham().getMaSanPham());
        }

        if (spct.getMauSac() != null) {
            dto.setIdMauSac(spct.getMauSac().getId());
            dto.setTenMauSac(spct.getMauSac().getTenMauSac());
        }

        if (spct.getKichThuoc() != null) {
            dto.setIdKichThuoc(spct.getKichThuoc().getId());
            dto.setTenKichThuoc(spct.getKichThuoc().getTenKichThuoc());
        }

        if (spct.getChatLieuDay() != null) {
            dto.setIdChatLieuDay(spct.getChatLieuDay().getId());
            dto.setTenChatLieuDay(spct.getChatLieuDay().getTenChatLieu());
        }

        if (spct.getLoaiMay() != null) {
            dto.setIdLoaiMay(spct.getLoaiMay().getId());
            dto.setTenLoaiMay(spct.getLoaiMay().getTenLoaiMay());
        }

        return dto;
    }
}
