package com.example.watchaura.service.impl;

import com.example.watchaura.dto.*;
import com.example.watchaura.entity.*;
import com.example.watchaura.repository.*;
import com.example.watchaura.service.HoaDonService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HoaDonServiceImpl implements HoaDonService {

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final KhachHangRepository khachHangRepository;
    private final VoucherRepository voucherRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final DiaChiGiaoHangRepository diaChiGiaoHangRepository;

    @Override
    public List<HoaDonDTO> getAll() {
        return hoaDonRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public HoaDonDTO getById(Integer id) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + id));
        return convertToDTO(hoaDon);
    }

    @Override
    public HoaDonDTO getByMaDonHang(String maDonHang) {
        HoaDon hoaDon = hoaDonRepository.findByMaDonHang(maDonHang)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với mã: " + maDonHang));
        return convertToDTO(hoaDon);
    }

    @Override
    public List<HoaDonDTO> getByKhachHangId(Integer khachHangId) {
        return hoaDonRepository.findByKhachHangIdOrderByNgayDatDesc(khachHangId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<HoaDonDTO> getByTrangThaiDonHang(String trangThaiDonHang) {
        return hoaDonRepository.findByTrangThaiDonHang(trangThaiDonHang).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public HoaDonDTO create(HoaDonRequest request) {
        // Validate khách hàng
        KhachHang khachHang = khachHangRepository.findById(request.getKhachHangId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        // Validate nhân viên (nếu có)
        KhachHang nhanVien = null;
        if (request.getNhanVienId() != null) {
            nhanVien = khachHangRepository.findById(request.getNhanVienId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));
        }

        // Validate voucher (nếu có)
        Voucher voucher = null;
        if (request.getVoucherId() != null) {
            voucher = voucherRepository.findById(request.getVoucherId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy voucher"));
        }

        // Tính tổng tiền từ các items
        BigDecimal tongTienTamTinh = BigDecimal.ZERO;
        for (HoaDonChiTietRequest itemRequest : request.getItems()) {
            SanPhamChiTiet sanPhamChiTiet = sanPhamChiTietRepository.findById(itemRequest.getSanPhamChiTietId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm chi tiết"));

            // Kiểm tra số lượng tồn kho
            if (sanPhamChiTiet.getSoLuongTon() == null || sanPhamChiTiet.getSoLuongTon() < itemRequest.getSoLuong()) {
                throw new RuntimeException("Số lượng tồn kho không đủ cho sản phẩm: " + sanPhamChiTiet.getSanPham().getTenSanPham());
            }

            if (sanPhamChiTiet.getGiaBan() != null) {
                tongTienTamTinh = tongTienTamTinh.add(
                        sanPhamChiTiet.getGiaBan().multiply(BigDecimal.valueOf(itemRequest.getSoLuong()))
                );
            }
        }

        // Tính tiền giảm từ voucher
        BigDecimal tienGiam = BigDecimal.ZERO;
        if (voucher != null) {
            tienGiam = calculateVoucherDiscount(voucher, tongTienTamTinh);
        }

        BigDecimal tongTienThanhToan = tongTienTamTinh.subtract(tienGiam);
        if (tongTienThanhToan.compareTo(BigDecimal.ZERO) < 0) {
            tongTienThanhToan = BigDecimal.ZERO;
        }

        // Tạo hóa đơn
        HoaDon hoaDon = new HoaDon();
        hoaDon.setMaDonHang(generateMaDonHang());
        hoaDon.setKhachHang(khachHang);
        hoaDon.setNhanVien(nhanVien);
        hoaDon.setVoucher(voucher);
        hoaDon.setTongTienTamTinh(tongTienTamTinh);
        hoaDon.setTienGiam(tienGiam);
        hoaDon.setTongTienThanhToan(tongTienThanhToan);
        hoaDon.setPhuongThucThanhToan(request.getPhuongThucThanhToan());
        hoaDon.setLoaiHoaDon(request.getLoaiHoaDon());
        hoaDon.setTrangThai(true);
        hoaDon.setTrangThaiDonHang("CHO_XAC_NHAN");
        hoaDon.setNgayDat(LocalDateTime.now());
        hoaDon.setDiaChi(request.getDiaChi());
        hoaDon.setTenKhachHang(request.getTenKhachHang());
        hoaDon.setSdtKhachHang(request.getSdtKhachHang());
        hoaDon.setGhiChu(request.getGhiChu());

        HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);

        // Tạo chi tiết hóa đơn và trừ tồn kho
        for (HoaDonChiTietRequest itemRequest : request.getItems()) {
            SanPhamChiTiet sanPhamChiTiet = sanPhamChiTietRepository.findById(itemRequest.getSanPhamChiTietId())
                    .orElseThrow();

            HoaDonChiTiet hoaDonChiTiet = new HoaDonChiTiet();
            hoaDonChiTiet.setHoaDon(savedHoaDon);
            hoaDonChiTiet.setSanPhamChiTiet(sanPhamChiTiet);
            hoaDonChiTiet.setSoLuong(itemRequest.getSoLuong());
            hoaDonChiTiet.setDonGia(sanPhamChiTiet.getGiaBan());
            hoaDonChiTietRepository.save(hoaDonChiTiet);

            // Trừ tồn kho
            sanPhamChiTiet.setSoLuongTon(sanPhamChiTiet.getSoLuongTon() - itemRequest.getSoLuong());
            sanPhamChiTietRepository.save(sanPhamChiTiet);
        }

        // Tạo địa chỉ giao hàng (nếu có)
        if (request.getDiaChiGiaoHang() != null) {
            DiaChiGiaoHang diaChiGiaoHang = new DiaChiGiaoHang();
            diaChiGiaoHang.setHoaDon(savedHoaDon);
            diaChiGiaoHang.setTenNguoiNhan(request.getDiaChiGiaoHang().getTenNguoiNhan());
            diaChiGiaoHang.setSdtNguoiNhan(request.getDiaChiGiaoHang().getSdtNguoiNhan());
            diaChiGiaoHang.setDiaChiCuThe(request.getDiaChiGiaoHang().getDiaChiCuThe());
            diaChiGiaoHang.setPhuongXa(request.getDiaChiGiaoHang().getPhuongXa());
            diaChiGiaoHang.setQuanHuyen(request.getDiaChiGiaoHang().getQuanHuyen());
            diaChiGiaoHang.setTinhThanh(request.getDiaChiGiaoHang().getTinhThanh());
            diaChiGiaoHang.setGhiChu(request.getDiaChiGiaoHang().getGhiChu());
            diaChiGiaoHangRepository.save(diaChiGiaoHang);
        }

        // Cập nhật số lượng đã dùng của voucher
        if (voucher != null && tienGiam.compareTo(BigDecimal.ZERO) > 0) {
            voucher.setSoLuongDaDung(voucher.getSoLuongDaDung() + 1);
            voucherRepository.save(voucher);
        }

        return convertToDTO(savedHoaDon);
    }

    @Override
    @Transactional
    public HoaDonDTO update(Integer id, HoaDonRequest request) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        // Chỉ cho phép cập nhật khi đơn ở trạng thái CHO_XAC_NHAN
        if (!"CHO_XAC_NHAN".equals(hoaDon.getTrangThaiDonHang())) {
            throw new RuntimeException("Chỉ có thể cập nhật đơn hàng ở trạng thái CHỜ XÁC NHẬN");
        }

        // Cập nhật các field
        if (request.getDiaChi() != null) {
            hoaDon.setDiaChi(request.getDiaChi());
        }
        if (request.getTenKhachHang() != null) {
            hoaDon.setTenKhachHang(request.getTenKhachHang());
        }
        if (request.getSdtKhachHang() != null) {
            hoaDon.setSdtKhachHang(request.getSdtKhachHang());
        }
        if (request.getGhiChu() != null) {
            hoaDon.setGhiChu(request.getGhiChu());
        }

        return convertToDTO(hoaDonRepository.save(hoaDon));
    }

    @Override
    @Transactional
    public HoaDonDTO updateTrangThaiDonHang(Integer id, String trangThaiDonHang) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        // Nếu hủy đơn, cộng lại tồn kho
        if ("DA_HUY".equals(trangThaiDonHang) && !"DA_HUY".equals(hoaDon.getTrangThaiDonHang())) {
            List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByHoaDonId(id);
            for (HoaDonChiTiet chiTiet : chiTiets) {
                SanPhamChiTiet sanPhamChiTiet = chiTiet.getSanPhamChiTiet();
                sanPhamChiTiet.setSoLuongTon(sanPhamChiTiet.getSoLuongTon() + chiTiet.getSoLuong());
                sanPhamChiTietRepository.save(sanPhamChiTiet);
            }
        }

        hoaDon.setTrangThaiDonHang(trangThaiDonHang);
        return convertToDTO(hoaDonRepository.save(hoaDon));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        // Soft delete
        hoaDon.setTrangThai(false);
        hoaDonRepository.save(hoaDon);
    }

    @Override
    public String generateMaDonHang() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "HD" + timestamp + uuid;
    }

    private BigDecimal calculateVoucherDiscount(Voucher voucher, BigDecimal tongTien) {
        if (voucher == null || !Boolean.TRUE.equals(voucher.getTrangThai())) {
            return BigDecimal.ZERO;
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(voucher.getNgayBatDau()) || now.isAfter(voucher.getNgayKetThuc())) {
            return BigDecimal.ZERO;
        }

        if (voucher.getDonHangToiThieu() != null && tongTien.compareTo(voucher.getDonHangToiThieu()) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal tienGiam;
        if ("PERCENT".equals(voucher.getLoaiVoucher())) {
            tienGiam = tongTien.multiply(voucher.getGiaTri())
                    .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
            if (voucher.getGiaTriToiDa() != null && tienGiam.compareTo(voucher.getGiaTriToiDa()) > 0) {
                tienGiam = voucher.getGiaTriToiDa();
            }
        } else { // FIXED
            tienGiam = voucher.getGiaTri();
            if (tienGiam.compareTo(tongTien) > 0) {
                tienGiam = tongTien;
            }
        }

        return tienGiam;
    }

    private HoaDonDTO convertToDTO(HoaDon hoaDon) {
        HoaDonDTO dto = new HoaDonDTO();
        dto.setId(hoaDon.getId());
        dto.setMaDonHang(hoaDon.getMaDonHang());
        dto.setKhachHangId(hoaDon.getKhachHang().getId());
        dto.setTenKhachHang(hoaDon.getTenKhachHang());
        dto.setNhanVienId(hoaDon.getNhanVien() != null ? hoaDon.getNhanVien().getId() : null);
        dto.setVoucherId(hoaDon.getVoucher() != null ? hoaDon.getVoucher().getId() : null);
        dto.setMaVoucher(hoaDon.getVoucher() != null ? hoaDon.getVoucher().getMaVoucher() : null);
        dto.setTongTienTamTinh(hoaDon.getTongTienTamTinh());
        dto.setTienGiam(hoaDon.getTienGiam());
        dto.setTongTienThanhToan(hoaDon.getTongTienThanhToan());
        dto.setPhuongThucThanhToan(hoaDon.getPhuongThucThanhToan());
        dto.setLoaiHoaDon(hoaDon.getLoaiHoaDon());
        dto.setTrangThai(hoaDon.getTrangThai());
        dto.setTrangThaiDonHang(hoaDon.getTrangThaiDonHang());
        dto.setNgayDat(hoaDon.getNgayDat());
        dto.setDiaChi(hoaDon.getDiaChi());
        dto.setSdtKhachHang(hoaDon.getSdtKhachHang());
        dto.setGhiChu(hoaDon.getGhiChu());

        // Lấy chi tiết hóa đơn
        List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByHoaDonId(hoaDon.getId());
        List<HoaDonChiTietDTO> chiTietDTOs = chiTiets.stream()
                .map(this::convertChiTietToDTO)
                .collect(Collectors.toList());
        dto.setItems(chiTietDTOs);

        // Lấy địa chỉ giao hàng
        diaChiGiaoHangRepository.findByHoaDonId(hoaDon.getId())
                .ifPresent(diaChi -> {
                    DiaChiGiaoHangDTO diaChiDTO = new DiaChiGiaoHangDTO();
                    diaChiDTO.setId(diaChi.getId());
                    diaChiDTO.setHoaDonId(diaChi.getHoaDon().getId());
                    diaChiDTO.setTenNguoiNhan(diaChi.getTenNguoiNhan());
                    diaChiDTO.setSdtNguoiNhan(diaChi.getSdtNguoiNhan());
                    diaChiDTO.setDiaChiCuThe(diaChi.getDiaChiCuThe());
                    diaChiDTO.setPhuongXa(diaChi.getPhuongXa());
                    diaChiDTO.setQuanHuyen(diaChi.getQuanHuyen());
                    diaChiDTO.setTinhThanh(diaChi.getTinhThanh());
                    diaChiDTO.setGhiChu(diaChi.getGhiChu());
                    dto.setDiaChiGiaoHang(diaChiDTO);
                });

        return dto;
    }

    private HoaDonChiTietDTO convertChiTietToDTO(HoaDonChiTiet chiTiet) {
        HoaDonChiTietDTO dto = new HoaDonChiTietDTO();
        dto.setId(chiTiet.getId());
        dto.setHoaDonId(chiTiet.getHoaDon().getId());
        dto.setSanPhamChiTietId(chiTiet.getSanPhamChiTiet().getId());
        
        if (chiTiet.getSanPhamChiTiet().getSanPham() != null) {
            dto.setTenSanPham(chiTiet.getSanPhamChiTiet().getSanPham().getTenSanPham());
        }
        
        dto.setSoLuong(chiTiet.getSoLuong());
        dto.setDonGia(chiTiet.getDonGia());
        
        if (dto.getDonGia() != null && dto.getSoLuong() != null) {
            dto.setThanhTien(dto.getDonGia().multiply(BigDecimal.valueOf(dto.getSoLuong())));
        } else {
            dto.setThanhTien(BigDecimal.ZERO);
        }

        return dto;
    }
}
