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
import java.util.ArrayList;
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
        GioHangChiTiet chiTiet = gioHangChiTietRepository.findByIdWithSanPhamDetails(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chi tiết giỏ hàng với ID: " + id));
        return convertToDTO(chiTiet);
    }

    @Override
    public List<GioHangChiTietDTO> getByGioHangId(Integer gioHangId) {
        return gioHangChiTietRepository.findByGioHangIdWithSanPhamDetails(gioHangId).stream()
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

        // Khả dụng = tồn - đã giữ cho đơn online (soLuongDaDat)
        int khaDung = sanPhamChiTiet.getSoLuongKhaDung() != null ? sanPhamChiTiet.getSoLuongKhaDung() : 0;
        if (khaDung < 1) {
            throw new RuntimeException("Sản phẩm không còn hàng.");
        }
        if (khaDung < request.getSoLuong()) {
            throw new RuntimeException("Thêm sản phẩm thất bại: muốn thêm " + request.getSoLuong() + " nhưng chỉ còn " + khaDung + " có thể bán.");
        }

        // Kiểm tra sản phẩm đã có trong giỏ chưa
        GioHangChiTiet existing = gioHangChiTietRepository
                .findByGioHangIdAndSanPhamChiTietId(request.getGioHangId(), request.getSanPhamChiTietId())
                .orElse(null);

        if (existing != null) {
            // Cộng dồn số lượng
            int soLuongHienTai = existing.getSoLuong();
            int soLuongMoi = soLuongHienTai + request.getSoLuong();

            // Kiểm tra lại tồn kho
            if (khaDung < soLuongMoi) {
                throw new RuntimeException("Thêm sản phẩm thất bại: đã có " + soLuongHienTai + " trong giỏ, thêm " + request.getSoLuong() + " -> tổng " + soLuongMoi + " vượt quá số lượng khả dụng (" + khaDung + ").");
            }

            existing.setSoLuong(soLuongMoi);
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
        int khaDung = sanPhamChiTiet.getSoLuongKhaDung() != null ? sanPhamChiTiet.getSoLuongKhaDung() : 0;
        if (khaDung < request.getSoLuong()) {
            throw new RuntimeException("Số lượng khả dụng không đủ (còn " + khaDung + " sản phẩm).");
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

    @Override
    @Transactional
    public void deleteByGioHangId(Integer gioHangId) {
        gioHangChiTietRepository.deleteByGioHangId(gioHangId);
    }

    private GioHangChiTietDTO convertToDTO(GioHangChiTiet chiTiet) {
        GioHangChiTietDTO dto = new GioHangChiTietDTO();
        dto.setId(chiTiet.getId());
        dto.setGioHangId(chiTiet.getGioHang().getId());

        if (chiTiet.getSanPhamChiTiet() != null) {
            dto.setSanPhamChiTietId(chiTiet.getSanPhamChiTiet().getId());
            dto.setGiaBan(chiTiet.getSanPhamChiTiet().getGiaBan());

            Integer soLuongTon = chiTiet.getSanPhamChiTiet().getSoLuongTon() != null
                    ? chiTiet.getSanPhamChiTiet().getSoLuongTon() : 0;
            Integer soLuongDaDat = chiTiet.getSanPhamChiTiet().getSoLuongDaDat() != null
                    ? chiTiet.getSanPhamChiTiet().getSoLuongDaDat() : 0;
            dto.setSoLuongTon(soLuongTon);
            dto.setSoLuongKhaDung(soLuongTon - soLuongDaDat);

            if (chiTiet.getSanPhamChiTiet().getSanPham() != null) {
                dto.setIdSanPham(chiTiet.getSanPhamChiTiet().getSanPham().getId());
                dto.setMaSanPham(chiTiet.getSanPhamChiTiet().getSanPham().getMaSanPham());
                dto.setTenSanPham(chiTiet.getSanPhamChiTiet().getSanPham().getTenSanPham());
                dto.setHinhAnh(chiTiet.getSanPhamChiTiet().getSanPham().getHinhAnh());
            }
            dto.setMoTaBienThe(buildMoTaBienThe(chiTiet.getSanPhamChiTiet()));
        }

        dto.setSoLuong(chiTiet.getSoLuong());

        if (dto.getGiaBan() != null && dto.getSoLuong() != null) {
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
