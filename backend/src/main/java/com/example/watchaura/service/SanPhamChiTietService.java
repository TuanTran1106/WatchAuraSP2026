package com.example.watchaura.service;

import com.example.watchaura.dto.SanPhamChiTietDTO;
import com.example.watchaura.dto.SanPhamChiTietRequest;

import com.example.watchaura.entity.*;
import com.example.watchaura.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SanPhamChiTietService {

    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final SanPhamRepository sanPhamRepository;
    private final MauSacRepository mauSacRepository;
    private final KichThuocRepository kichThuocRepository;
    private final ChatLieuDayRepository chatLieuDayRepository;
    private final LoaiMayRepository loaiMayRepository;

    /**
     * Lấy tất cả sản phẩm chi tiết
     */
    public List<SanPhamChiTietDTO> getAllSanPhamChiTiet() {
        return sanPhamChiTietRepository.findAllWithDetails().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy sản phẩm chi tiết theo ID
     */
    public SanPhamChiTietDTO getSanPhamChiTietById(Integer id) {
        SanPhamChiTiet spct = sanPhamChiTietRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm chi tiết với ID: " + id));
        return convertToDTO(spct);
    }

    /**
     * Lấy danh sách sản phẩm chi tiết theo ID sản phẩm
     */
    public List<SanPhamChiTietDTO> getSanPhamChiTietBySanPhamId(Integer sanPhamId) {
        // Kiểm tra sản phẩm tồn tại
        if (!sanPhamRepository.existsById(sanPhamId)) {
            throw new RuntimeException("Không tìm thấy sản phẩm với ID: " + sanPhamId);
        }
        
        return sanPhamChiTietRepository.findBySanPhamId(sanPhamId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Tạo mới sản phẩm chi tiết
     */
    @Transactional
    public SanPhamChiTietDTO createSanPhamChiTiet(SanPhamChiTietRequest request) {
        // Kiểm tra sản phẩm tồn tại
        SanPham sanPham = sanPhamRepository.findById(request.getIdSanPham())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + request.getIdSanPham()));

        // Tạo entity
        SanPhamChiTiet spct = new SanPhamChiTiet();
        spct.setSanPham(sanPham);

        // Set các thuộc tính optional
        if (request.getIdMauSac() != null) {
            MauSac mauSac = mauSacRepository.findById(request.getIdMauSac())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy màu sắc với ID: " + request.getIdMauSac()));
            spct.setMauSac(mauSac);
        }

        if (request.getIdKichThuoc() != null) {
            KichThuoc kichThuoc = kichThuocRepository.findById(request.getIdKichThuoc())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy kích thước với ID: " + request.getIdKichThuoc()));
            spct.setKichThuoc(kichThuoc);
        }

        if (request.getIdChatLieuDay() != null) {
            ChatLieuDay chatLieuDay = chatLieuDayRepository.findById(request.getIdChatLieuDay())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chất liệu dây với ID: " + request.getIdChatLieuDay()));
            spct.setChatLieuDay(chatLieuDay);
        }

        if (request.getIdLoaiMay() != null) {
            LoaiMay loaiMay = loaiMayRepository.findById(request.getIdLoaiMay())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy loại máy với ID: " + request.getIdLoaiMay()));
            spct.setLoaiMay(loaiMay);
        }

        spct.setSoLuongTon(request.getSoLuongTon());
        spct.setGiaBan(request.getGiaBan());
        spct.setDuongKinh(request.getDuongKinh());
        spct.setDoChiuNuoc(request.getDoChiuNuoc());
        spct.setBeRongDay(request.getBeRongDay());
        spct.setTrongLuong(request.getTrongLuong());
        spct.setTrangThai(request.getTrangThai());

        // Lưu vào database
        SanPhamChiTiet savedSpct = sanPhamChiTietRepository.save(spct);
        return convertToDTO(savedSpct);
    }

    /**
     * Cập nhật sản phẩm chi tiết
     */
    @Transactional
    public SanPhamChiTietDTO updateSanPhamChiTiet(Integer id, SanPhamChiTietRequest request) {
        // Tìm sản phẩm chi tiết cần cập nhật
        SanPhamChiTiet spct = sanPhamChiTietRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm chi tiết với ID: " + id));

        // Kiểm tra sản phẩm tồn tại
        SanPham sanPham = sanPhamRepository.findById(request.getIdSanPham())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + request.getIdSanPham()));

        spct.setSanPham(sanPham);

        // Cập nhật các thuộc tính optional
        if (request.getIdMauSac() != null) {
            MauSac mauSac = mauSacRepository.findById(request.getIdMauSac())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy màu sắc với ID: " + request.getIdMauSac()));
            spct.setMauSac(mauSac);
        } else {
            spct.setMauSac(null);
        }

        if (request.getIdKichThuoc() != null) {
            KichThuoc kichThuoc = kichThuocRepository.findById(request.getIdKichThuoc())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy kích thước với ID: " + request.getIdKichThuoc()));
            spct.setKichThuoc(kichThuoc);
        } else {
            spct.setKichThuoc(null);
        }

        if (request.getIdChatLieuDay() != null) {
            ChatLieuDay chatLieuDay = chatLieuDayRepository.findById(request.getIdChatLieuDay())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chất liệu dây với ID: " + request.getIdChatLieuDay()));
            spct.setChatLieuDay(chatLieuDay);
        } else {
            spct.setChatLieuDay(null);
        }

        if (request.getIdLoaiMay() != null) {
            LoaiMay loaiMay = loaiMayRepository.findById(request.getIdLoaiMay())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy loại máy với ID: " + request.getIdLoaiMay()));
            spct.setLoaiMay(loaiMay);
        } else {
            spct.setLoaiMay(null);
        }

        spct.setSoLuongTon(request.getSoLuongTon());
        spct.setGiaBan(request.getGiaBan());
        spct.setDuongKinh(request.getDuongKinh());
        spct.setDoChiuNuoc(request.getDoChiuNuoc());
        spct.setBeRongDay(request.getBeRongDay());
        spct.setTrongLuong(request.getTrongLuong());
        spct.setTrangThai(request.getTrangThai());

        // Lưu vào database
        SanPhamChiTiet updatedSpct = sanPhamChiTietRepository.save(spct);
        return convertToDTO(updatedSpct);
    }

    /**
     * Xóa sản phẩm chi tiết
     */
    @Transactional
    public void deleteSanPhamChiTiet(Integer id) {
        if (!sanPhamChiTietRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy sản phẩm chi tiết với ID: " + id);
        }
        sanPhamChiTietRepository.deleteById(id);
    }

    /**
     * Convert Entity sang DTO
     */
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

import org.springframework.stereotype.Service;

import java.util.List;

public interface SanPhamChiTietService {
    List<SanPhamChiTietDTO> getAllSanPhamChiTiet();

    SanPhamChiTietDTO getSanPhamChiTietById(Integer id);

    SanPhamChiTietDTO createSanPhamChiTiet(SanPhamChiTietRequest request);

    SanPhamChiTietDTO updateSanPhamChiTiet(Integer id, SanPhamChiTietRequest request);

    void deleteSanPhamChiTiet(Integer id);
}

