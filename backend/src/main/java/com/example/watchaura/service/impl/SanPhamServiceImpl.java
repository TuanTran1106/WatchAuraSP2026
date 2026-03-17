package com.example.watchaura.service.impl;

import com.example.watchaura.dto.SanPhamDTO;
import com.example.watchaura.dto.SanPhamRequest;
import com.example.watchaura.entity.DanhMuc;
import com.example.watchaura.entity.SanPham;
import com.example.watchaura.entity.ThuongHieu;
import com.example.watchaura.entity.SanPhamChiTiet;
import com.example.watchaura.repository.DanhMucRepository;
import com.example.watchaura.repository.SanPhamChiTietRepository;
import com.example.watchaura.repository.SanPhamRepository;
import com.example.watchaura.repository.ThuongHieuRepository;
import com.example.watchaura.service.FileUploadService;
import com.example.watchaura.service.SanPhamService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SanPhamServiceImpl implements SanPhamService {

    private final SanPhamRepository sanPhamRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final ThuongHieuRepository thuongHieuRepository;
    private final DanhMucRepository danhMucRepository;
    private final FileUploadService fileUploadService;

    @Override
    public List<SanPhamDTO> getAllSanPham() {
        return sanPhamRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
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
        // Nếu không có mã hoặc mã trống thì tự sinh mã
        String ma = request.getMaSanPham();
        if (ma == null || ma.trim().isEmpty()) {
            ma = generateMaSanPham();
        } else {
            ma = ma.trim();
        }

        // Kiểm tra mã đã tồn tại chưa
        if (sanPhamRepository.existsByMaSanPham(ma)) {
            throw new RuntimeException("Mã sản phẩm đã tồn tại: " + ma);
        }

        ThuongHieu thuongHieu = thuongHieuRepository.findById(request.getIdThuongHieu())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu với ID: " + request.getIdThuongHieu()));

        DanhMuc danhMuc = danhMucRepository.findById(request.getIdDanhMuc())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + request.getIdDanhMuc()));

        SanPham sanPham = new SanPham();
        sanPham.setMaSanPham(ma);
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
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));

        // Không cho phép sửa mã sản phẩm - giữ nguyên mã hiện tại
        ThuongHieu thuongHieu = thuongHieuRepository.findById(request.getIdThuongHieu())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu với ID: " + request.getIdThuongHieu()));

        DanhMuc danhMuc = danhMucRepository.findById(request.getIdDanhMuc())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + request.getIdDanhMuc()));

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

    @Override
    public boolean existsByMaSanPham(String maSanPham) {
        return maSanPham != null && sanPhamRepository.existsByMaSanPham(maSanPham.trim());
    }

    @Override
    public List<SanPhamDTO> getSanPhamTrangChu(int limit) {
        return sanPhamRepository.findByTrangThaiOrderByNgayTaoDesc(true, PageRequest.of(0, limit))
                .getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<SanPhamDTO> searchPage(String keyword, Boolean trangThai, Pageable pageable) {
        return sanPhamRepository.searchByKeywordAndTrangThai(keyword, trangThai, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public String generateMaSanPham() {
        // Tìm mã lớn nhất hiện có có dạng SP + số
        List<SanPham> sanPhams = sanPhamRepository.findAll();
        int maxNumber = 0;

        for (SanPham sp : sanPhams) {
            String ma = sp.getMaSanPham();
            if (ma != null && ma.startsWith("SP")) {
                try {
                    int num = Integer.parseInt(ma.substring(2));
                    if (num > maxNumber) {
                        maxNumber = num;
                    }
                } catch (NumberFormatException e) {
                    // Bỏ qua nếu không phải định dạng SP + số
                }
            }
        }

        // Trả về mã mới: SP + số tiếp theo (có padding 4 chữ số)
        return String.format("SP%04d", maxNumber + 1);
    }

    @Override
    @Transactional
    public SanPhamDTO updateSanPhamImage(Integer id, String newFilePath) {
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

        // Giá bán: lấy giá thấp nhất trong các biến thể đang bán
        if (sanPham.getId() != null) {
            List<SanPhamChiTiet> variants = sanPhamChiTietRepository.findBySanPham_Id(sanPham.getId());
            BigDecimal minGia = variants.stream()
                    .map(SanPhamChiTiet::getGiaBan)
                    .filter(Objects::nonNull)
                    .min(BigDecimal::compareTo)
                    .orElse(null);
            dto.setGiaBan(minGia);
            int totalTon = variants.stream()
                    .map(SanPhamChiTiet::getSoLuongTon)
                    .filter(Objects::nonNull)
                    .mapToInt(Integer::intValue)
                    .sum();
            dto.setSoLuongTon(totalTon);
        }

        return dto;
    }
}
