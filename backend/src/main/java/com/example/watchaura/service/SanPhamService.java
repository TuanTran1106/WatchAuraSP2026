package com.example.watchaura.service;

import com.example.watchaura.dto.SanPhamDTO;
import com.example.watchaura.dto.SanPhamRequest;
import com.example.watchaura.entity.SanPham;
import com.example.watchaura.entity.ThuongHieu;
import com.example.watchaura.entity.DanhMuc;
import com.example.watchaura.repository.SanPhamRepository;
import com.example.watchaura.repository.ThuongHieuRepository;
import com.example.watchaura.repository.DanhMucRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SanPhamService {

    private final SanPhamRepository sanPhamRepository;
    private final ThuongHieuRepository thuongHieuRepository;
    private final DanhMucRepository danhMucRepository;

    /**
     * Lấy tất cả sản phẩm
     */
    public List<SanPhamDTO> getAllSanPham() {
        return sanPhamRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
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
        sanPhamRepository.deleteById(id);
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

        return dto;
    }
}