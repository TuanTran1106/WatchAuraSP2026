package com.example.watchaura.service.impl;

import com.example.watchaura.dto.SanPhamDTO;
import com.example.watchaura.dto.SanPhamRequest;
import com.example.watchaura.entity.ChatLieuDay;
import com.example.watchaura.entity.DanhMuc;
import com.example.watchaura.entity.KichThuoc;
import com.example.watchaura.entity.LoaiMay;
import com.example.watchaura.entity.MauSac;
import com.example.watchaura.entity.SanPham;
import com.example.watchaura.entity.SanPhamChiTiet;
import com.example.watchaura.entity.ThuongHieu;
import com.example.watchaura.repository.ChatLieuDayRepository;
import com.example.watchaura.repository.DanhMucRepository;
import com.example.watchaura.repository.KichThuocRepository;
import com.example.watchaura.repository.LoaiMayRepository;
import com.example.watchaura.repository.MauSacRepository;
import com.example.watchaura.repository.SanPhamRepository;
import com.example.watchaura.repository.ThuongHieuRepository;
import com.example.watchaura.service.FileUploadService;
import com.example.watchaura.service.SanPhamService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SanPhamServiceImpl implements SanPhamService {
    private final SanPhamRepository sanPhamRepository;
    private final ThuongHieuRepository thuongHieuRepository;
    private final DanhMucRepository danhMucRepository;
    private final MauSacRepository mauSacRepository;
    private final KichThuocRepository kichThuocRepository;
    private final ChatLieuDayRepository chatLieuDayRepository;
    private final LoaiMayRepository loaiMayRepository;
    private final FileUploadService fileUploadService;
    private final com.example.watchaura.repository.SanPhamChiTietRepository sanPhamChiTietRepository;

    /**
     * Lấy tất cả sản phẩm
     */
    public List<SanPhamDTO> getAllSanPham() {
        return sanPhamRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<SanPhamDTO> getPage(Pageable pageable) {
        Page<SanPham> entityPage = sanPhamRepository.findAll(pageable);
        List<SanPhamDTO> content = entityPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(content, pageable, entityPage.getTotalElements());
    }

    @Override
    public Page<SanPhamDTO> searchPage(String keyword, Boolean trangThai, Pageable pageable) {
        String q = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        Page<SanPham> entityPage;
        if (q != null || trangThai != null) {
            entityPage = sanPhamRepository.searchByKeywordAndTrangThai(q, trangThai, pageable);
        } else {
            entityPage = sanPhamRepository.findAll(pageable);
        }
        List<SanPhamDTO> content = entityPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(content, pageable, entityPage.getTotalElements());
    }

    /**
     * Lấy sản phẩm theo ID
     */
    public SanPhamDTO getSanPhamById(Integer id) {
        SanPham sanPham = sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));
        return convertToDTO(sanPham);
    }

    /**
     * Tạo mới sản phẩm
     */
    @Transactional
    public SanPhamDTO createSanPham(SanPhamRequest request) {
        // Kiểm tra mã sản phẩm đã tồn tại
        if (sanPhamRepository.existsByMaSanPham(request.getMaSanPham())) {
            throw new RuntimeException("Mã sản phẩm đã tồn tại: " + request.getMaSanPham());
        }

        // Kiểm tra thương hiệu tồn tại
        ThuongHieu thuongHieu = thuongHieuRepository.findById(request.getIdThuongHieu())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu với ID: " + request.getIdThuongHieu()));

        // Kiểm tra danh mục tồn tại
        DanhMuc danhMuc = danhMucRepository.findById(request.getIdDanhMuc())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + request.getIdDanhMuc()));

        // Tạo entity
        SanPham sanPham = new SanPham();
        sanPham.setMaSanPham(request.getMaSanPham());
        sanPham.setTenSanPham(request.getTenSanPham());
        sanPham.setMoTa(request.getMoTa());
        sanPham.setHinhAnh(request.getHinhAnh());
        sanPham.setThuongHieu(thuongHieu);
        sanPham.setDanhMuc(danhMuc);
        sanPham.setPhongCach(request.getPhongCach());
        sanPham.setTrangThai(request.getTrangThai());
        sanPham.setNgayTao(LocalDateTime.now());

        // Lưu vào database
        SanPham savedSanPham = sanPhamRepository.save(sanPham);

        // Nếu có giá bán hoặc số lượng tồn (hoặc các thuộc tính biến thể) thì tạo một biến thể mặc định (SanPhamChiTiet)
        if (request.getGiaBan() != null || (request.getSoLuongTon() != null && request.getSoLuongTon() > 0)
                || request.getIdMauSac() != null || request.getIdKichThuoc() != null
                || request.getIdChatLieuDay() != null || request.getIdLoaiMay() != null
                || request.getDuongKinh() != null || request.getDoChiuNuoc() != null
                || request.getBeRongDay() != null || request.getTrongLuong() != null) {
            SanPhamChiTiet chiTiet = new SanPhamChiTiet();
            chiTiet.setSanPham(savedSanPham);
            chiTiet.setGiaBan(request.getGiaBan());
            chiTiet.setSoLuongTon(request.getSoLuongTon() != null ? request.getSoLuongTon() : 0);
            chiTiet.setTrangThai(Boolean.TRUE);
            chiTiet.setDuongKinh(request.getDuongKinh());
            chiTiet.setDoChiuNuoc(request.getDoChiuNuoc());
            chiTiet.setBeRongDay(request.getBeRongDay());
            chiTiet.setTrongLuong(request.getTrongLuong());
            if (request.getIdMauSac() != null) {
                chiTiet.setMauSac(mauSacRepository.findById(request.getIdMauSac()).orElse(null));
            }
            if (request.getIdKichThuoc() != null) {
                chiTiet.setKichThuoc(kichThuocRepository.findById(request.getIdKichThuoc()).orElse(null));
            }
            if (request.getIdChatLieuDay() != null) {
                chiTiet.setChatLieuDay(chatLieuDayRepository.findById(request.getIdChatLieuDay()).orElse(null));
            }
            if (request.getIdLoaiMay() != null) {
                chiTiet.setLoaiMay(loaiMayRepository.findById(request.getIdLoaiMay()).orElse(null));
            }
            sanPhamChiTietRepository.save(chiTiet);
        }

        return convertToDTO(savedSanPham);
    }

    /**
     * Cập nhật sản phẩm
     */
    @Transactional
    public SanPhamDTO updateSanPham(Integer id, SanPhamRequest request) {
        // Tìm sản phẩm cần cập nhật
        SanPham sanPham = sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));

        // Kiểm tra mã sản phẩm trùng (ngoại trừ chính nó)
        if (sanPhamRepository.existsByMaSanPhamAndIdNot(request.getMaSanPham(), id)) {
            throw new RuntimeException("Mã sản phẩm đã tồn tại: " + request.getMaSanPham());
        }

        // Kiểm tra thương hiệu tồn tại
        ThuongHieu thuongHieu = thuongHieuRepository.findById(request.getIdThuongHieu())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu với ID: " + request.getIdThuongHieu()));

        // Kiểm tra danh mục tồn tại
        DanhMuc danhMuc = danhMucRepository.findById(request.getIdDanhMuc())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + request.getIdDanhMuc()));

        // Cập nhật thông tin
        sanPham.setMaSanPham(request.getMaSanPham());
        sanPham.setTenSanPham(request.getTenSanPham());
        sanPham.setMoTa(request.getMoTa());
        sanPham.setHinhAnh(request.getHinhAnh());
        sanPham.setThuongHieu(thuongHieu);
        sanPham.setDanhMuc(danhMuc);
        sanPham.setPhongCach(request.getPhongCach());
        sanPham.setTrangThai(request.getTrangThai());

        // Lưu vào database
        SanPham updatedSanPham = sanPhamRepository.save(sanPham);

        // Cập nhật giá bán / số lượng tồn / màu sắc / kích thước / chất liệu dây / loại máy của biến thể (chi tiết)
        List<SanPhamChiTiet> chiTiets = sanPhamChiTietRepository.findBySanPham_Id(id);
        if (!chiTiets.isEmpty()) {
            SanPhamChiTiet first = chiTiets.get(0);
            if (request.getGiaBan() != null) first.setGiaBan(request.getGiaBan());
            if (request.getSoLuongTon() != null) first.setSoLuongTon(request.getSoLuongTon());
            first.setDuongKinh(request.getDuongKinh());
            first.setDoChiuNuoc(request.getDoChiuNuoc());
            first.setBeRongDay(request.getBeRongDay());
            first.setTrongLuong(request.getTrongLuong());
            if (request.getIdMauSac() != null) first.setMauSac(mauSacRepository.findById(request.getIdMauSac()).orElse(null));
            else first.setMauSac(null);
            if (request.getIdKichThuoc() != null) first.setKichThuoc(kichThuocRepository.findById(request.getIdKichThuoc()).orElse(null));
            else first.setKichThuoc(null);
            if (request.getIdChatLieuDay() != null) first.setChatLieuDay(chatLieuDayRepository.findById(request.getIdChatLieuDay()).orElse(null));
            else first.setChatLieuDay(null);
            if (request.getIdLoaiMay() != null) first.setLoaiMay(loaiMayRepository.findById(request.getIdLoaiMay()).orElse(null));
            else first.setLoaiMay(null);
            sanPhamChiTietRepository.save(first);
        } else if (request.getGiaBan() != null || (request.getSoLuongTon() != null && request.getSoLuongTon() > 0)
                || request.getIdMauSac() != null || request.getIdKichThuoc() != null
                || request.getIdChatLieuDay() != null || request.getIdLoaiMay() != null
                || request.getDuongKinh() != null || request.getDoChiuNuoc() != null
                || request.getBeRongDay() != null || request.getTrongLuong() != null) {
            SanPhamChiTiet chiTiet = new SanPhamChiTiet();
            chiTiet.setSanPham(updatedSanPham);
            chiTiet.setGiaBan(request.getGiaBan());
            chiTiet.setSoLuongTon(request.getSoLuongTon() != null ? request.getSoLuongTon() : 0);
            chiTiet.setTrangThai(Boolean.TRUE);
            chiTiet.setDuongKinh(request.getDuongKinh());
            chiTiet.setDoChiuNuoc(request.getDoChiuNuoc());
            chiTiet.setBeRongDay(request.getBeRongDay());
            chiTiet.setTrongLuong(request.getTrongLuong());
            if (request.getIdMauSac() != null) chiTiet.setMauSac(mauSacRepository.findById(request.getIdMauSac()).orElse(null));
            if (request.getIdKichThuoc() != null) chiTiet.setKichThuoc(kichThuocRepository.findById(request.getIdKichThuoc()).orElse(null));
            if (request.getIdChatLieuDay() != null) chiTiet.setChatLieuDay(chatLieuDayRepository.findById(request.getIdChatLieuDay()).orElse(null));
            if (request.getIdLoaiMay() != null) chiTiet.setLoaiMay(loaiMayRepository.findById(request.getIdLoaiMay()).orElse(null));
            sanPhamChiTietRepository.save(chiTiet);
        }

        return convertToDTO(updatedSanPham);
    }

    /**
     * Xóa sản phẩm
     */
    @Transactional
    public void deleteSanPham(Integer id) {
        if (!sanPhamRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy sản phẩm với ID: " + id);
        }
        // Xóa tất cả chi tiết sản phẩm liên quan trước
        sanPhamChiTietRepository.deleteAll(sanPhamChiTietRepository.findBySanPhamId(id));
        sanPhamRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void toggleTrangThai(Integer id) {
        SanPham sanPham = sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));
        sanPham.setTrangThai(sanPham.getTrangThai() == null || !sanPham.getTrangThai());
        sanPhamRepository.save(sanPham);
    }

    /**
     * Cập nhật ảnh sản phẩm
     */
    @Transactional
    public SanPhamDTO updateSanPhamImage(Integer id, String newFilePath) {
        // Tìm sản phẩm cần cập nhật
        SanPham sanPham = sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));

        // Xóa ảnh cũ nếu có
        if (sanPham.getHinhAnh() != null && !sanPham.getHinhAnh().isEmpty()) {
            fileUploadService.deleteFile(sanPham.getHinhAnh());
        }

        // Cập nhật ảnh mới
        sanPham.setHinhAnh(newFilePath);

        // Lưu vào database
        SanPham updatedSanPham = sanPhamRepository.save(sanPham);
        return convertToDTO(updatedSanPham);
    }

    /**
     * Convert Entity sang DTO
     */
    private SanPhamDTO convertToDTO(SanPham sanPham) {
        SanPhamDTO dto = new SanPhamDTO();
        dto.setId(sanPham.getId());
        dto.setMaSanPham(sanPham.getMaSanPham());
        dto.setTenSanPham(sanPham.getTenSanPham());
        dto.setMoTa(sanPham.getMoTa());
        dto.setHinhAnh(sanPham.getHinhAnh());
        dto.setPhongCach(sanPham.getPhongCach());
        dto.setTrangThai(sanPham.getTrangThai());

        if (sanPham.getThuongHieu() != null) {
            dto.setIdThuongHieu(sanPham.getThuongHieu().getId());
            dto.setTenThuongHieu(sanPham.getThuongHieu().getTenThuongHieu());
        }

        if (sanPham.getDanhMuc() != null) {
            dto.setIdDanhMuc(sanPham.getDanhMuc().getId());
            dto.setTenDanhMuc(sanPham.getDanhMuc().getTenDanhMuc());
        }

        List<SanPhamChiTiet> chiTiets = sanPhamChiTietRepository.findBySanPham_Id(sanPham.getId());
        if (!chiTiets.isEmpty()) {
            SanPhamChiTiet first = chiTiets.get(0);
            int tongTon = chiTiets.stream()
                    .mapToInt(ct -> ct.getSoLuongTon() != null ? ct.getSoLuongTon() : 0)
                    .sum();
            dto.setSoLuongTon(tongTon);
            BigDecimal minGia = chiTiets.stream()
                    .map(SanPhamChiTiet::getGiaBan)
                    .filter(g -> g != null)
                    .min(BigDecimal::compareTo)
                    .orElse(null);
            dto.setGiaBan(minGia);
            if (first.getMauSac() != null) dto.setIdMauSac(first.getMauSac().getId());
            if (first.getKichThuoc() != null) dto.setIdKichThuoc(first.getKichThuoc().getId());
            if (first.getChatLieuDay() != null) dto.setIdChatLieuDay(first.getChatLieuDay().getId());
            if (first.getLoaiMay() != null) dto.setIdLoaiMay(first.getLoaiMay().getId());
            dto.setDuongKinh(first.getDuongKinh());
            dto.setDoChiuNuoc(first.getDoChiuNuoc());
            dto.setBeRongDay(first.getBeRongDay());
            dto.setTrongLuong(first.getTrongLuong());
        }

        return dto;
    }

}
