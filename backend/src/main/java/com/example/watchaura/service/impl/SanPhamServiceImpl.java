package com.example.watchaura.service.impl;

import com.example.watchaura.dto.KhuyenMaiPriceResult;
import com.example.watchaura.dto.SanPhamDTO;
import com.example.watchaura.dto.SanPhamRequest;
import com.example.watchaura.entity.DanhMuc;
import com.example.watchaura.entity.LoaiMay;
import com.example.watchaura.entity.SanPham;
import com.example.watchaura.entity.ThuongHieu;
import com.example.watchaura.entity.KhuyenMai;
import com.example.watchaura.entity.SanPhamChiTiet;
import com.example.watchaura.repository.DanhMucRepository;
import com.example.watchaura.repository.LoaiMayRepository;
import com.example.watchaura.repository.SanPhamChiTietRepository;
import com.example.watchaura.repository.SanPhamRepository;
import com.example.watchaura.repository.ThuongHieuRepository;
import com.example.watchaura.service.FileUploadService;
import com.example.watchaura.service.KhuyenMaiService;
import com.example.watchaura.service.SanPhamChiTietKhuyenMaiService;
import com.example.watchaura.service.SanPhamService;
import com.example.watchaura.util.KhuyenMaiPricePick;
import com.example.watchaura.util.SanPhamKhuyenMaiPricing;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SanPhamServiceImpl implements SanPhamService {

    private final SanPhamRepository sanPhamRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final SanPhamChiTietKhuyenMaiService sanPhamChiTietKhuyenMaiService;
    private final KhuyenMaiService khuyenMaiService;
    private final ThuongHieuRepository thuongHieuRepository;
    private final DanhMucRepository danhMucRepository;
    private final LoaiMayRepository loaiMayRepository;
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
        SanPham sanPham = sanPhamRepository.findByIdWithDetails(id)
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
        if (request.getIdLoaiMay() != null) {
            LoaiMay loaiMay = loaiMayRepository.findById(request.getIdLoaiMay())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy loại máy với ID: " + request.getIdLoaiMay()));
            sanPham.setLoaiMay(loaiMay);
        }

        applyKmFieldsFromRequest(sanPham, request);
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
        if (request.getIdLoaiMay() != null) {
            LoaiMay loaiMay = loaiMayRepository.findById(request.getIdLoaiMay())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy loại máy với ID: " + request.getIdLoaiMay()));
            sanPham.setLoaiMay(loaiMay);
        } else {
            sanPham.setLoaiMay(null);
        }

        applyKmFieldsFromRequest(sanPham, request);
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
    public List<SanPhamDTO> getSanPhamDtosForDisplay(List<SanPham> sanPhams) {
        if (sanPhams == null || sanPhams.isEmpty()) {
            return Collections.emptyList();
        }
        return sanPhams.stream().map(this::convertToDTO).collect(Collectors.toList());
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

        if (sanPham.getLoaiMay() != null) {
            dto.setIdLoaiMay(sanPham.getLoaiMay().getId());
            dto.setTenLoaiMay(sanPham.getLoaiMay().getTenLoaiMay());
        }

        // Giá bán: lấy giá thấp nhất trong các biến thể đang bán
        if (sanPham.getId() != null) {
            List<SanPhamChiTiet> variants = sanPhamChiTietRepository.findBySanPhamId(sanPham.getId());
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
            applyPromoPricingToDto(dto, sanPham, variants);
        }

        dto.setLoaiKhuyenMai(sanPham.getLoaiKhuyenMai());
        dto.setGiaTriKhuyenMai(sanPham.getGiaTriKhuyenMai());
        dto.setNgayBatDauKhuyenMai(sanPham.getNgayBatDauKhuyenMai());
        dto.setNgayKetThucKhuyenMai(sanPham.getNgayKetThucKhuyenMai());
        dto.setTrangThaiKhuyenMai(sanPham.getTrangThaiKhuyenMai());

        return dto;
    }

    private static void applyKmFieldsFromRequest(SanPham sanPham, SanPhamRequest request) {
        if (request == null) {
            return;
        }
        sanPham.setLoaiKhuyenMai(request.getLoaiKhuyenMai());
        sanPham.setGiaTriKhuyenMai(request.getGiaTriKhuyenMai());
        sanPham.setNgayBatDauKhuyenMai(request.getNgayBatDauKhuyenMai());
        sanPham.setNgayKetThucKhuyenMai(request.getNgayKetThucKhuyenMai());
        sanPham.setTrangThaiKhuyenMai(request.getTrangThaiKhuyenMai());
    }

    /**
     * Tìm biến thể có giá sau giảm thấp nhất (KM chương trình + KM cấp {@link SanPham}).
     * Trước đây chỉ xét biến thể có giá niêm yết thấp nhất — KM gắn biến thể đắt hơn sẽ không bao giờ hiện.
     */
    private void applyPromoPricingToDto(SanPhamDTO dto, SanPham sanPham, List<SanPhamChiTiet> variants) {
        dto.setCoKhuyenMaiHopLe(false);
        dto.setGiaSauKhuyenMai(null);
        dto.setPhanTramGiamHienThi(null);
        dto.setLoaiGiamHienThi(null);
        dto.setSoTienGiamHienThi(null);
        if (dto.getGiaBan() == null || variants == null || variants.isEmpty()) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        List<KhuyenMai> chuongTrinhDangChay = khuyenMaiService.getActivePromotions(now);
        String tenDm = sanPham.getDanhMuc() != null ? sanPham.getDanhMuc().getTenDanhMuc() : null;
        KhuyenMaiPriceResult bestOverall = null;
        for (SanPhamChiTiet v : variants) {
            if (v.getGiaBan() == null) {
                continue;
            }
            BigDecimal base = v.getGiaBan();
            KhuyenMaiPriceResult fromVariant = sanPhamChiTietKhuyenMaiService.resolveForSanPhamChiTiet(
                    v.getId(), base, chuongTrinhDangChay, tenDm);
            KhuyenMaiPriceResult fromProduct = SanPhamKhuyenMaiPricing.compute(sanPham, base, now);
            KhuyenMaiPriceResult bestV = KhuyenMaiPricePick.pickBetter(fromVariant, fromProduct, base);
            if (bestOverall == null) {
                bestOverall = bestV;
                continue;
            }
            int cmp = bestV.giaSauGiam().compareTo(bestOverall.giaSauGiam());
            if (cmp < 0) {
                bestOverall = bestV;
            } else if (cmp == 0 && bestV.coKhuyenMai() && !bestOverall.coKhuyenMai()) {
                bestOverall = bestV;
            }
        }
        if (bestOverall == null || !bestOverall.coKhuyenMai()) {
            return;
        }
        dto.setCoKhuyenMaiHopLe(true);
        dto.setGiaSauKhuyenMai(bestOverall.giaSauGiam());
        dto.setPhanTramGiamHienThi(bestOverall.phanTramHienThi());
        if (bestOverall.loaiGiamApDung() != KhuyenMaiPriceResult.LoaiGiamApDung.KHONG) {
            dto.setLoaiGiamHienThi(bestOverall.loaiGiamApDung().name());
        }
        dto.setSoTienGiamHienThi(bestOverall.soTienGiam());
        /* Giá gạch ngang = niêm yết của đúng biến thể đang áp KM tốt nhất, không phải min toàn cục nếu khác biến thể. */
        dto.setGiaBan(bestOverall.giaGoc());
        chuanHoaHienThiKhuyenMaiDto(dto);
    }

    /**
     * UI có thể hiện badge % theo {@code phanTramGiamHienThi}; nếu tiền giảm thực tế chỉ vài đồng
     * trong khi % trên giá gạch ngang phải giảm hàng nghìn → đồng bộ lại (lỗi TIEN/PHAN_TRAM cũ hoặc cache).
     */
    private static void chuanHoaHienThiKhuyenMaiDto(SanPhamDTO dto) {
        if (!Boolean.TRUE.equals(dto.getCoKhuyenMaiHopLe()) || dto.getGiaBan() == null
                || dto.getGiaSauKhuyenMai() == null) {
            return;
        }
        BigDecimal pct = dto.getPhanTramGiamHienThi();
        if (pct == null || pct.compareTo(BigDecimal.ZERO) <= 0 || pct.compareTo(BigDecimal.valueOf(100)) > 0) {
            return;
        }
        BigDecimal expectedOff = dto.getGiaBan().multiply(pct)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
                .min(dto.getGiaBan());
        BigDecimal actualOff = dto.getGiaBan().subtract(dto.getGiaSauKhuyenMai()).max(BigDecimal.ZERO);
        if (actualOff.compareTo(BigDecimal.valueOf(500)) >= 0) {
            return;
        }
        if (expectedOff.subtract(actualOff).compareTo(BigDecimal.valueOf(5_000L)) <= 0) {
            return;
        }
        BigDecimal newSau = dto.getGiaBan().subtract(expectedOff).setScale(0, RoundingMode.HALF_UP).max(BigDecimal.ZERO);
        dto.setGiaSauKhuyenMai(newSau);
        dto.setSoTienGiamHienThi(expectedOff);
        dto.setLoaiGiamHienThi("PHAN_TRAM");
    }
}
