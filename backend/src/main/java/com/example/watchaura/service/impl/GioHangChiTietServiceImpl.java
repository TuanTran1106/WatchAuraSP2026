package com.example.watchaura.service.impl;

import com.example.watchaura.dto.GioHangChiTietDTO;
import com.example.watchaura.dto.GioHangChiTietRequest;
import com.example.watchaura.entity.GioHang;
import com.example.watchaura.entity.GioHangChiTiet;
import com.example.watchaura.entity.SanPhamChiTiet;
import com.example.watchaura.repository.GioHangChiTietRepository;
import com.example.watchaura.repository.GioHangRepository;
import com.example.watchaura.repository.SanPhamChiTietRepository;
import com.example.watchaura.service.GioHangChiTietService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GioHangChiTietServiceImpl implements GioHangChiTietService {

    private final GioHangChiTietRepository gioHangChiTietRepository;
    private final GioHangRepository gioHangRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;

    @Override
    public List<GioHangChiTietDTO> getAll() {
        return gioHangChiTietRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public GioHangChiTietDTO getById(Integer id) {
        GioHangChiTiet chiTiet = gioHangChiTietRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết giỏ hàng với ID: " + id));
        return convertToDTO(chiTiet);
    }

    @Override
    public List<GioHangChiTietDTO> getByGioHangId(Integer gioHangId) {
        return gioHangChiTietRepository.findByGioHangId(gioHangId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public GioHangChiTietDTO create(GioHangChiTietRequest request) {
        GioHang gioHang = gioHangRepository.findById(request.getGioHangId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giỏ hàng"));

        SanPhamChiTiet sanPhamChiTiet = sanPhamChiTietRepository.findById(request.getSanPhamChiTietId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm chi tiết"));

        // Kiểm tra số lượng tồn kho
        if (sanPhamChiTiet.getSoLuongTon() == null || sanPhamChiTiet.getSoLuongTon() < request.getSoLuong()) {
            throw new RuntimeException("Số lượng tồn kho không đủ");
        }

        // Kiểm tra sản phẩm đã có trong giỏ chưa
        GioHangChiTiet existing = gioHangChiTietRepository
                .findByGioHangIdAndSanPhamChiTietId(request.getGioHangId(), request.getSanPhamChiTietId())
                .orElse(null);

        if (existing != null) {
            // Cộng dồn số lượng
            existing.setSoLuong(existing.getSoLuong() + request.getSoLuong());
            
            // Kiểm tra lại tồn kho
            if (sanPhamChiTiet.getSoLuongTon() < existing.getSoLuong()) {
                throw new RuntimeException("Số lượng tồn kho không đủ");
            }
            
            return convertToDTO(gioHangChiTietRepository.save(existing));
        } else {
            // Tạo mới
            GioHangChiTiet chiTiet = new GioHangChiTiet();
            chiTiet.setGioHang(gioHang);
            chiTiet.setSanPhamChiTiet(sanPhamChiTiet);
            chiTiet.setSoLuong(request.getSoLuong());
            
            return convertToDTO(gioHangChiTietRepository.save(chiTiet));
        }
    }

    @Override
    @Transactional
    public GioHangChiTietDTO update(Integer id, GioHangChiTietRequest request) {
        GioHangChiTiet chiTiet = gioHangChiTietRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết giỏ hàng"));

        SanPhamChiTiet sanPhamChiTiet = chiTiet.getSanPhamChiTiet();
        
        // Kiểm tra số lượng tồn kho
        if (sanPhamChiTiet.getSoLuongTon() == null || sanPhamChiTiet.getSoLuongTon() < request.getSoLuong()) {
            throw new RuntimeException("Số lượng tồn kho không đủ");
        }

        chiTiet.setSoLuong(request.getSoLuong());
        
        return convertToDTO(gioHangChiTietRepository.save(chiTiet));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (!gioHangChiTietRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy chi tiết giỏ hàng với ID: " + id);
        }
        gioHangChiTietRepository.deleteById(id);
    }

    private GioHangChiTietDTO convertToDTO(GioHangChiTiet chiTiet) {
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
