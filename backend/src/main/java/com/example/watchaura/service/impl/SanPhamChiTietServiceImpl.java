package com.example.watchaura.service.impl;


import com.example.watchaura.dto.SanPhamChiTietDTO;
import com.example.watchaura.dto.SanPhamChiTietRequest;
import com.example.watchaura.entity.*;
import com.example.watchaura.repository.*;
import com.example.watchaura.service.SanPhamChiTietService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    private final SerialSanPhamRepository serialSanPhamRepository;

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

        spct.setGiaBan(request.getGiaBan());
        spct.setDuongKinh(request.getDuongKinh());
        spct.setDoChiuNuoc(request.getDoChiuNuoc());
        spct.setBeRongDay(request.getBeRongDay());
        spct.setTrongLuong(request.getTrongLuong());
        spct.setTrangThai(request.getTrangThai());

        List<String> serials = request.getSerials();
        int soLuongSerial = (serials != null) ? serials.size() : 0;
        int soLuongTon = (request.getSoLuongTon() != null) ? request.getSoLuongTon() : 0;
        spct.setSoLuongTon(soLuongSerial > 0 ? soLuongSerial : soLuongTon);

        // Lưu sản phẩm chi tiết trước để có ID
        SanPhamChiTiet savedSpct = sanPhamChiTietRepository.save(spct);

        // Import serial từ Excel nếu có
        if (serials != null && !serials.isEmpty()) {
            LocalDateTime now = LocalDateTime.now();
            for (String serial : serials) {
                if (serial != null && !serial.trim().isEmpty()) {
                    SerialSanPham serialSanPham = new SerialSanPham();
                    serialSanPham.setMaSerial(serial.trim());
                    serialSanPham.setSanPhamChiTiet(savedSpct);
                    serialSanPham.setTrangThai(SerialSanPham.TRANG_THAI_TRONG_KHO);
                    serialSanPham.setNgayTao(now);
                    serialSanPhamRepository.save(serialSanPham);
                }
            }
        }

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
        if (spct.getSerialSanPhams() != null) {
            int soLuongTon = spct.getSoLuongTon() != null ? spct.getSoLuongTon() : 0;
            List<String> serialTrongKho = spct.getSerialSanPhams().stream()
                    .filter(s -> s != null && s.getTrangThai() != null
                            && s.getTrangThai() == SerialSanPham.TRANG_THAI_TRONG_KHO
                            && s.getHoaDonChiTiet() == null)
                    .map(SerialSanPham::getMaSerial)
                    .filter(s -> s != null && !s.isBlank())
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .sorted()
                    .collect(Collectors.toList());
            int soLuongHienThi = Math.min(serialTrongKho.size(), Math.max(soLuongTon, 0));
            dto.setSoLuongSerialTrongKho(soLuongHienThi);
            dto.setSerials(serialTrongKho.stream().limit(soLuongHienThi).collect(Collectors.toList()));
        }

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

    @Override
    public boolean existsByVariant(Integer sanPhamId, Integer mauSacId, Integer kichThuocId, Integer chatLieuDayId, Integer excludeId) {
        return sanPhamChiTietRepository.existsBySanPhamAndVariant(sanPhamId, mauSacId, kichThuocId, chatLieuDayId);
    }

    @Override
    public boolean existsBySanPhamAndVariant(Integer sanPhamId, Integer mauSacId, Integer kichThuocId, Integer chatLieuDayId) {
        return sanPhamChiTietRepository.existsBySanPhamAndVariant(sanPhamId, mauSacId, kichThuocId, chatLieuDayId);
    }

}
