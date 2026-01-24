package com.example.watchaura.service.impl;

import com.example.watchaura.dto.SanPhamDTO;
import com.example.watchaura.dto.SanPhamRequest;
import com.example.watchaura.entity.DanhMuc;
import com.example.watchaura.entity.SanPham;
import com.example.watchaura.entity.ThuongHieu;
import com.example.watchaura.repository.DanhMucRepository;
import com.example.watchaura.repository.SanPhamRepository;
import com.example.watchaura.repository.ThuongHieuRepository;
import com.example.watchaura.service.SanPhamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SanPhamServiceImpl implements SanPhamService {

    private final SanPhamRepository sanPhamRepository;
    private final ThuongHieuRepository thuongHieuRepository;
    private final DanhMucRepository danhMucRepository;

    @Override
    public List<SanPhamDTO> getAllSanPham() {
        return sanPhamRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SanPhamDTO getSanPhamById(Integer id) {
        SanPham sanPham = sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));
        return convertToDTO(sanPham);
    }

    @Override
    @Transactional
    public SanPhamDTO createSanPham(SanPhamRequest request) {

        if (sanPhamRepository.existsByMaSanPham(request.getMaSanPham())) {
            throw new RuntimeException("Mã sản phẩm đã tồn tại: " + request.getMaSanPham());
        }

        ThuongHieu thuongHieu = thuongHieuRepository.findById(request.getIdThuongHieu())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu"));

        DanhMuc danhMuc = danhMucRepository.findById(request.getIdDanhMuc())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));

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

        return convertToDTO(sanPhamRepository.save(sanPham));
    }

    @Override
    @Transactional
    public SanPhamDTO updateSanPham(Integer id, SanPhamRequest request) {

        SanPham sanPham = sanPhamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        if (sanPhamRepository.existsByMaSanPhamAndIdNot(request.getMaSanPham(), id)) {
            throw new RuntimeException("Mã sản phẩm đã tồn tại: " + request.getMaSanPham());
        }

        ThuongHieu thuongHieu = thuongHieuRepository.findById(request.getIdThuongHieu())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu"));

        DanhMuc danhMuc = danhMucRepository.findById(request.getIdDanhMuc())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));

        sanPham.setMaSanPham(request.getMaSanPham());
        sanPham.setTenSanPham(request.getTenSanPham());
        sanPham.setMoTa(request.getMoTa());
        sanPham.setHinhAnh(request.getHinhAnh());
        sanPham.setThuongHieu(thuongHieu);
        sanPham.setDanhMuc(danhMuc);
        sanPham.setPhongCach(request.getPhongCach());
        sanPham.setTrangThai(request.getTrangThai());

        return convertToDTO(sanPhamRepository.save(sanPham));
    }

    @Override
    @Transactional
    public void deleteSanPham(Integer id) {
        if (!sanPhamRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy sản phẩm với ID: " + id);
        }
        sanPhamRepository.deleteById(id);
    }

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
