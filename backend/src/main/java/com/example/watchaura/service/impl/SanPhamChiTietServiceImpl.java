package com.example.watchaura.service.impl;


import com.example.watchaura.dto.SanPhamChiTietDTO;
import com.example.watchaura.dto.SanPhamChiTietRequest;
import com.example.watchaura.entity.*;
import com.example.watchaura.repository.*;
import com.example.watchaura.service.KhuyenMaiService;
import com.example.watchaura.service.SanPhamChiTietService;
import com.example.watchaura.util.KhuyenMaiPricing;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class SanPhamChiTietServiceImpl implements SanPhamChiTietService {
    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final SanPhamRepository sanPhamRepository;
    private final MauSacRepository mauSacRepository;
    private final KichThuocRepository kichThuocRepository;
    private final ChatLieuDayRepository chatLieuDayRepository;
    private final SanPhamChiTietKhuyenMaiRepository sanPhamChiTietKhuyenMaiRepository;
    private final KhuyenMaiService khuyenMaiService;

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
        validateGiaBanKhongXungDotKhuyenMaiTien(null, sanPham, request.getGiaBan());

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
        validateGiaBanKhongXungDotKhuyenMaiTien(id, sanPham, request.getGiaBan());

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
        // FIFO: Số lượng khả dụng = tồn kho (không trừ giữ hàng)
        dto.setSoLuongKhaDung(spct.getSoLuongTon() != null ? spct.getSoLuongTon() : 0);
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

        if (spct.getSanPham() != null && spct.getSanPham().getLoaiMay() != null) {
            dto.setIdLoaiMay(spct.getSanPham().getLoaiMay().getId());
            dto.setTenLoaiMay(spct.getSanPham().getLoaiMay().getTenLoaiMay());
        }

        return dto;
    }

    private void validateGiaBanKhongXungDotKhuyenMaiTien(Integer spctId, SanPham sanPham, BigDecimal giaBanMoi) {
        if (giaBanMoi == null || giaBanMoi.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        Set<Integer> seenPromotionIds = new HashSet<>();
        if (spctId != null) {
            sanPhamChiTietKhuyenMaiRepository.findActiveKhuyenMaiBySpctId(spctId, now).forEach(link -> {
                KhuyenMai km = link.getKhuyenMai();
                validateFixedPromotionAmount(km, giaBanMoi, seenPromotionIds);
            });
        }
        String tenDanhMuc = sanPham != null && sanPham.getDanhMuc() != null
                ? sanPham.getDanhMuc().getTenDanhMuc()
                : null;
        khuyenMaiService.getActivePromotions(now).forEach(km -> {
            if (!appliesToCategory(km, tenDanhMuc)) {
                return;
            }
            validateFixedPromotionAmount(km, giaBanMoi, seenPromotionIds);
        });
    }

    private static boolean appliesToCategory(KhuyenMai km, String tenDanhMuc) {
        if (km == null) {
            return false;
        }
        String danhMucApDung = km.getDanhMucApDung();
        if (danhMucApDung == null || danhMucApDung.isBlank()) {
            return true;
        }
        if (tenDanhMuc == null || tenDanhMuc.isBlank()) {
            return false;
        }
        return danhMucApDung.trim().equalsIgnoreCase(tenDanhMuc.trim());
    }

    private static void validateFixedPromotionAmount(KhuyenMai km, BigDecimal giaBanMoi, Set<Integer> seenPromotionIds) {
        if (km == null || km.getId() == null || !seenPromotionIds.add(km.getId())) {
            return;
        }
        if (KhuyenMaiPricing.phanLoai(km.getLoaiGiam()) != KhuyenMaiPricing.LoaiGiamTinh.TIEN) {
            return;
        }
        if (km.getGiaTriGiam() == null) {
            return;
        }
        if (km.getGiaTriGiam().compareTo(giaBanMoi) >= 0) {
            String ten = km.getTenChuongTrinh() != null && !km.getTenChuongTrinh().isBlank()
                    ? km.getTenChuongTrinh()
                    : ("ID " + km.getId());
            throw new RuntimeException("Giá bán mới phải lớn hơn mức giảm tiền của khuyến mãi '" + ten + "'.");
        }
    }
}
