package com.example.watchaura.service.impl;

import com.example.watchaura.dto.GioHangDTO;
import com.example.watchaura.dto.GioHangChiTietDTO;
import com.example.watchaura.dto.GioHangRequest;
import com.example.watchaura.entity.GioHang;
import com.example.watchaura.entity.GioHangChiTiet;
import com.example.watchaura.entity.KhachHang;
import com.example.watchaura.repository.GioHangChiTietRepository;
import com.example.watchaura.repository.GioHangRepository;
import com.example.watchaura.repository.KhachHangRepository;
import com.example.watchaura.service.GioHangService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GioHangServiceImpl implements GioHangService {

    private final GioHangRepository gioHangRepository;
    private final GioHangChiTietRepository gioHangChiTietRepository;
    private final KhachHangRepository khachHangRepository;

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

    private GioHangDTO convertToDTO(GioHang gioHang) {
        GioHangDTO dto = new GioHangDTO();
        dto.setId(gioHang.getId());
        dto.setKhachHangId(gioHang.getKhachHang().getId());
        dto.setTenKhachHang(gioHang.getKhachHang().getTenNguoiDung());
        dto.setTrangThai(gioHang.getTrangThai());
        dto.setNgayTao(gioHang.getNgayTao());

        // Lấy danh sách items
        List<GioHangChiTiet> items = gioHangChiTietRepository.findByGioHangId(gioHang.getId());
        List<GioHangChiTietDTO> itemDTOs = items.stream()
                .map(this::convertChiTietToDTO)
                .collect(Collectors.toList());
        dto.setItems(itemDTOs);

        // Tính tổng tiền
        BigDecimal tongTien = items.stream()
                .map(item -> {
                    if (item.getSanPhamChiTiet() != null && item.getSanPhamChiTiet().getGiaBan() != null && item.getSoLuong() != null) {
                        return item.getSanPhamChiTiet().getGiaBan()
                                .multiply(BigDecimal.valueOf(item.getSoLuong()));
                    }
                    return BigDecimal.ZERO;
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        dto.setTongTien(tongTien);

        return dto;
    }

    private GioHangChiTietDTO convertChiTietToDTO(GioHangChiTiet chiTiet) {
        GioHangChiTietDTO dto = new GioHangChiTietDTO();
        dto.setId(chiTiet.getId());
        dto.setGioHangId(chiTiet.getGioHang().getId());
        
        if (chiTiet.getSanPhamChiTiet() != null) {
            dto.setSanPhamChiTietId(chiTiet.getSanPhamChiTiet().getId());
            dto.setGiaBan(chiTiet.getSanPhamChiTiet().getGiaBan());
            
            if (chiTiet.getSanPhamChiTiet().getSanPham() != null) {
                dto.setTenSanPham(chiTiet.getSanPhamChiTiet().getSanPham().getTenSanPham());
                dto.setHinhAnh(chiTiet.getSanPhamChiTiet().getSanPham().getHinhAnh());
            }
        }
        
        dto.setSoLuong(chiTiet.getSoLuong());
        
        if (dto.getGiaBan() != null && dto.getSoLuong() != null) {
            dto.setThanhTien(dto.getGiaBan().multiply(BigDecimal.valueOf(dto.getSoLuong())));
        } else {
            dto.setThanhTien(BigDecimal.ZERO);
        }

        return dto;
    }
}
