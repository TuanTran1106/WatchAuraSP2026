package com.example.watchaura.service.impl;

import com.example.watchaura.dto.GioHangDTO;
import com.example.watchaura.dto.GioHangChiTietDTO;
import com.example.watchaura.dto.GioHangRequest;
import com.example.watchaura.entity.GioHang;
import com.example.watchaura.entity.GioHangChiTiet;
import com.example.watchaura.entity.KhachHang;
import com.example.watchaura.entity.SanPhamChiTiet;
import com.example.watchaura.repository.GioHangChiTietRepository;
import com.example.watchaura.repository.GioHangRepository;
import com.example.watchaura.repository.KhachHangRepository;
import com.example.watchaura.entity.KhuyenMai;
import com.example.watchaura.service.GioHangService;
import com.example.watchaura.service.KhuyenMaiService;
import com.example.watchaura.service.SanPhamChiTietKhuyenMaiService;
import com.example.watchaura.util.GioHangChiTietPromoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GioHangServiceImpl implements GioHangService {

    private final GioHangRepository gioHangRepository;
    private final GioHangChiTietRepository gioHangChiTietRepository;
    private final KhachHangRepository khachHangRepository;
    private final SanPhamChiTietKhuyenMaiService sanPhamChiTietKhuyenMaiService;
    private final KhuyenMaiService khuyenMaiService;

    @Override
    public List<GioHangDTO> getAll() {
        return gioHangRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public GioHangDTO getById(Integer id) {
        GioHang gioHang = gioHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hàng với ID: " + id));
        return convertToDTO(gioHang);
    }

    @Override
    public GioHangDTO getByKhachHangId(Integer khachHangId) {
        GioHang gioHang = gioHangRepository.findByKhachHangIdAndTrangThai(khachHangId, true)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hàng của khách hàng"));
        return convertToDTO(gioHang);
    }

    @Override
    @Transactional
    public GioHangDTO create(GioHangRequest request) {
        KhachHang khachHang = khachHangRepository.findById(request.getKhachHangId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        // Kiểm tra đã có giỏ hàng active chưa
        if (gioHangRepository.findByKhachHangIdAndTrangThai(request.getKhachHangId(), true).isPresent()) {
            throw new RuntimeException("Khách hàng đã có giỏ hàng đang hoạt động");
        }

        GioHang gioHang = new GioHang();
        gioHang.setKhachHang(khachHang);
        gioHang.setTrangThai(request.getTrangThai() != null ? request.getTrangThai() : true);
        gioHang.setNgayTao(LocalDateTime.now());

        return convertToDTO(gioHangRepository.save(gioHang));
    }

    @Override
    @Transactional
    public GioHangDTO update(Integer id, GioHangRequest request) {
        GioHang gioHang = gioHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hàng"));

        if (request.getTrangThai() != null) {
            gioHang.setTrangThai(request.getTrangThai());
        }

        return convertToDTO(gioHangRepository.save(gioHang));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (!gioHangRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy giỏ hàng");
        }
        
        // Xóa tất cả chi tiết giỏ hàng
        gioHangChiTietRepository.deleteByGioHangId(id);
        
        gioHangRepository.deleteById(id);
    }

    @Override
    @Transactional
    public GioHangDTO getOrCreateCart(Integer khachHangId) {
        return gioHangRepository.findByKhachHangIdAndTrangThai(khachHangId, true)
                .map(this::convertToDTO)
                .orElseGet(() -> {
                    GioHangRequest request = new GioHangRequest();
                    request.setKhachHangId(khachHangId);
                    request.setTrangThai(true);
                    return create(request);
                });
    }

    @Override
    public int getSoLuongGioHang(Integer khachHangId) {
        if (khachHangId == null) return 0;
        return gioHangRepository.findByKhachHangIdAndTrangThai(khachHangId, true)
                .map(gh -> gioHangChiTietRepository.findByGioHangId(gh.getId()).stream()
                        .mapToInt(ct -> ct.getSoLuong() != null ? ct.getSoLuong() : 0)
                        .sum())
                .orElse(0);
    }

    private GioHangDTO convertToDTO(GioHang gioHang) {
        GioHangDTO dto = new GioHangDTO();
        dto.setId(gioHang.getId());
        dto.setKhachHangId(gioHang.getKhachHang().getId());
        dto.setTenKhachHang(gioHang.getKhachHang().getTenNguoiDung());
        dto.setTrangThai(gioHang.getTrangThai());
        dto.setNgayTao(gioHang.getNgayTao());

        List<GioHangChiTiet> items = gioHangChiTietRepository.findByGioHangIdWithSanPhamDetails(gioHang.getId());
        LocalDateTime promoNow = LocalDateTime.now();
        List<KhuyenMai> activeKhuyenMai = khuyenMaiService.getActivePromotions(promoNow);
        List<GioHangChiTietDTO> itemDTOs = items.stream()
                .map(line -> convertChiTietToDTO(line, promoNow, activeKhuyenMai))
                .collect(Collectors.toList());
        dto.setItems(itemDTOs);

        BigDecimal tongTien = itemDTOs.stream()
                .map(i -> i.getThanhTien() != null ? i.getThanhTien() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTongTien(tongTien);

        return dto;
    }

    private GioHangChiTietDTO convertChiTietToDTO(
            GioHangChiTiet chiTiet,
            LocalDateTime promoNow,
            List<KhuyenMai> chuongTrinhDangChay) {
        GioHangChiTietDTO dto = new GioHangChiTietDTO();
        dto.setId(chiTiet.getId());
        dto.setGioHangId(chiTiet.getGioHang().getId());

        SanPhamChiTiet spct = chiTiet.getSanPhamChiTiet();
        if (spct != null) {
            dto.setSanPhamChiTietId(spct.getId());
            dto.setGiaBan(spct.getGiaBan());
            dto.setSoLuongTon(spct.getSoLuongTon());
            dto.setSoLuongKhaDung(spct.getSoLuongKhaDung());
            if (spct.getSanPham() != null) {
                dto.setIdSanPham(spct.getSanPham().getId());
                dto.setMaSanPham(spct.getSanPham().getMaSanPham());
                dto.setTenSanPham(spct.getSanPham().getTenSanPham());
                dto.setHinhAnh(spct.getSanPham().getHinhAnh());
                if (spct.getSanPham().getDanhMuc() != null) {
                    dto.setTenDanhMuc(spct.getSanPham().getDanhMuc().getTenDanhMuc());
                }
            }
            dto.setMoTaBienThe(buildMoTaBienThe(spct));
        }

        dto.setSoLuong(chiTiet.getSoLuong());

        LocalDateTime now = promoNow != null ? promoNow : LocalDateTime.now();
        List<KhuyenMai> active = chuongTrinhDangChay != null
                ? chuongTrinhDangChay
                : khuyenMaiService.getActivePromotions(now);
        if (spct != null) {
            GioHangChiTietPromoUtil.applySanPhamKhuyenMai(
                    sanPhamChiTietKhuyenMaiService, dto, spct, now, active);
        } else if (dto.getGiaBan() != null && dto.getSoLuong() != null) {
            dto.setThanhTien(dto.getGiaBan().multiply(BigDecimal.valueOf(dto.getSoLuong())));
        } else {
            dto.setThanhTien(BigDecimal.ZERO);
        }

        return dto;
    }

    private static String buildMoTaBienThe(SanPhamChiTiet spct) {
        if (spct == null) {
            return null;
        }
        List<String> parts = new ArrayList<>();
        if (spct.getMauSac() != null && spct.getMauSac().getTenMauSac() != null
                && !spct.getMauSac().getTenMauSac().isBlank()) {
            parts.add("Màu: " + spct.getMauSac().getTenMauSac().trim());
        }
        if (spct.getKichThuoc() != null && spct.getKichThuoc().getTenKichThuoc() != null
                && !spct.getKichThuoc().getTenKichThuoc().isBlank()) {
            parts.add("Kích thước: " + spct.getKichThuoc().getTenKichThuoc().trim());
        }
        if (spct.getChatLieuDay() != null && spct.getChatLieuDay().getTenChatLieu() != null
                && !spct.getChatLieuDay().getTenChatLieu().isBlank()) {
            parts.add("Dây: " + spct.getChatLieuDay().getTenChatLieu().trim());
        }
        var lm = spct.getSanPham() != null ? spct.getSanPham().getLoaiMay() : null;
        if (lm != null && lm.getTenLoaiMay() != null && !lm.getTenLoaiMay().isBlank()) {
            parts.add("Loại máy: " + lm.getTenLoaiMay().trim());
        }
        return parts.isEmpty() ? null : String.join(" · ", parts);
    }
}
