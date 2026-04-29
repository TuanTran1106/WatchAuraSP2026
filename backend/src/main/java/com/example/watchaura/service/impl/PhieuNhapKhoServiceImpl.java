package com.example.watchaura.service.impl;

import com.example.watchaura.dto.PhieuNhapKhoDTO;
import com.example.watchaura.dto.RefundRequest;
import com.example.watchaura.dto.SerialCheckResponse;
import com.example.watchaura.entity.*;
import com.example.watchaura.repository.*;
import com.example.watchaura.service.PhieuNhapKhoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PhieuNhapKhoServiceImpl implements PhieuNhapKhoService {

    private final PhieuNhapKhoRepository phieuNhapKhoRepository;
    private final ChiTietPhieuNhapKhoRepository chiTietPhieuNhapKhoRepository;
    private final HoanTraRepository hoanTraRepository;
    private final HoanTraChiTietRepository hoanTraChiTietRepository;
    private final KhachHangRepository khachHangRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final SerialSanPhamRepository serialSanPhamRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Override
    @Transactional
    public PhieuNhapKhoDTO createFromHoanTra(RefundRequest request, Integer nhanVienId) {
        HoanTra hoanTra = hoanTraRepository.findByIdWithChiTiet(request.getHoanTraId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hoàn trả với ID: " + request.getHoanTraId()));

        if (!HoanTra.TRANG_THAI_DA_XU_LY.equals(hoanTra.getTrangThai())) {
            throw new RuntimeException("Chỉ có thể hoàn tiền cho hoàn trả đã xử lý");
        }

        if (phieuNhapKhoRepository.existsByHoanTraId(request.getHoanTraId())) {
            throw new RuntimeException("Đã có phiếu nhập kho cho hoàn trả này");
        }

        KhachHang nhanVien = khachHangRepository.findById(nhanVienId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên với ID: " + nhanVienId));

        PhieuNhapKho phieuNhapKho = new PhieuNhapKho();
        phieuNhapKho.setMaPhieu(generateMaPhieu());
        phieuNhapKho.setLoaiPhieu(PhieuNhapKho.LOAI_TU_HoanTra);
        phieuNhapKho.setHoanTra(hoanTra);
        phieuNhapKho.setNhanVien(nhanVien);
        phieuNhapKho.setSoTienHoan(request.getSoTienHoan());
        phieuNhapKho.setPhuongThucHoanTien(request.getPhuongThucHoanTien());
        phieuNhapKho.setGhiChu(request.getGhiChu());
        phieuNhapKho.setNgayTao(LocalDateTime.now());

        hoanTra.setPhuongThucHoanTien(request.getPhuongThucHoanTien());
        hoanTra.setSoTienHoanThucTe(request.getSoTienHoan());
        hoanTra.setGhiChuHoanTien(request.getGhiChu());
        hoanTra.setNgayHoanTien(LocalDateTime.now());

        phieuNhapKho = phieuNhapKhoRepository.save(phieuNhapKho);
        hoanTraRepository.save(hoanTra);

        if (hoanTra.getChiTietList() != null && !hoanTra.getChiTietList().isEmpty()) {
            for (HoanTraChiTiet chiTiet : hoanTra.getChiTietList()) {
                ChiTietPhieuNhapKho chiTietPhieu = new ChiTietPhieuNhapKho();
                chiTietPhieu.setPhieuNhapKho(phieuNhapKho);
                chiTietPhieu.setSanPhamChiTiet(chiTiet.getSanPhamChiTiet());
                chiTietPhieu.setSoLuong(chiTiet.getSoLuongHoanTra());
                chiTietPhieu.setDonGia(chiTiet.getDonGiaTaiThoiDiemMua());
                chiTietPhieu.setGhiChu("Hoàn trả từ phiếu " + hoanTra.getMaHoanTra());

                if (chiTiet.getHoaDonChiTiet() != null 
                        && chiTiet.getHoaDonChiTiet().getSerialSanPhams() != null) {
                    List<SerialSanPham> returnedSerials = chiTiet.getHoaDonChiTiet()
                            .getSerialSanPhams().stream()
                            .filter(s -> s.getTrangThai() == SerialSanPham.TRANG_THAI_DA_TRA_HANG)
                            .collect(Collectors.toList());
                    
                    if (!returnedSerials.isEmpty()) {
                        chiTietPhieu.setSerialSanPham(returnedSerials.get(0));
                    }
                }

                chiTietPhieuNhapKhoRepository.save(chiTietPhieu);
            }
        }

        return convertToDTO(phieuNhapKho);
    }

    @Override
    public PhieuNhapKhoDTO getByHoanTraId(Integer hoanTraId) {
        return phieuNhapKhoRepository.findByHoanTraIdWithChiTiet(hoanTraId)
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Override
    public PhieuNhapKhoDTO getById(Integer id) {
        return phieuNhapKhoRepository.findByIdWithChiTiet(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phiếu nhập kho với ID: " + id));
    }

    @Override
    public List<PhieuNhapKhoDTO> getAll() {
        return phieuNhapKhoRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SerialCheckResponse checkSerial(String maSerial) {
        SerialSanPham serial = serialSanPhamRepository.findByMaSerial(maSerial.trim()).orElse(null);

        SerialCheckResponse.SerialCheckResponseBuilder builder = SerialCheckResponse.builder()
                .maSerial(maSerial);

        if (serial == null) {
            return builder.valid(false)
                    .errorMessage("Serial không tồn tại trong hệ thống")
                    .build();
        }

        builder.serialId(serial.getId())
                .trangThai(String.valueOf(serial.getTrangThai()))
                .trangThaiHienThi(getTrangThaiText(serial.getTrangThai()))
                .trangThaiCode(serial.getTrangThai());

        if (serial.getTrangThai() == SerialSanPham.TRANG_THAI_DA_TRA_HANG) {
            return builder.valid(false)
                    .errorMessage("Serial này đã được trả hàng trước đó")
                    .build();
        }

        if (serial.getTrangThai() == SerialSanPham.TRANG_THAI_TRONG_KHO) {
            return builder.valid(false)
                    .errorMessage("Serial này chưa bán, đang ở trong kho")
                    .build();
        }

        if (serial.getTrangThai() == SerialSanPham.TRANG_THAI_BAO_HANH) {
            return builder.valid(false)
                    .errorMessage("Serial đang trong trạng thái bảo hành")
                    .build();
        }

        if (serial.getTrangThai() != SerialSanPham.TRANG_THAI_DA_BAN) {
            return builder.valid(false)
                    .errorMessage("Serial không ở trạng thái hợp lệ để hoàn trả")
                    .build();
        }

        if (serial.getHoaDonChiTiet() == null) {
            return builder.valid(false)
                    .errorMessage("Serial chưa được gán với hóa đơn nào")
                    .build();
        }

        HoaDonChiTiet hoaDonChiTiet = serial.getHoaDonChiTiet();
        HoaDon hoaDon = hoaDonChiTiet.getHoaDon();

        String tt = hoaDon.getTrangThaiDonHang();
        boolean coTheHoanTra = "DA_GIAO".equals(tt) || "HOAN_THANH".equals(tt)
                || "DA_THANH_TOAN".equals(tt) || "DA THANH TOAN".equals(tt)
                || "DA_XAC_NHAN".equals(tt);

        if (!coTheHoanTra) {
            return builder.valid(false)
                    .errorMessage("Đơn hàng '" + hoaDon.getMaDonHang() + "' có trạng thái '" + tt + "' không thể hoàn trả")
                    .build();
        }

        Optional<HoanTra> existingHoanTra = hoanTraRepository.findFirstByHoaDonIdWithChiTiet(hoaDon.getId());
        if (existingHoanTra.isPresent() && HoanTra.TRANG_THAI_DA_XU_LY.equals(existingHoanTra.get().getTrangThai())) {
            boolean serialDaHoan = hoaDonChiTiet.getSerialSanPhams() != null
                    && hoaDonChiTiet.getSerialSanPhams().stream()
                            .anyMatch(s -> s.getId().equals(serial.getId()) 
                                    && s.getTrangThai() == SerialSanPham.TRANG_THAI_DA_TRA_HANG);
            if (serialDaHoan) {
                return builder.valid(false)
                        .errorMessage("Serial này đã được hoàn trả rồi")
                        .build();
            }
        }

        SanPhamChiTiet spct = hoaDonChiTiet.getSanPhamChiTiet();
        builder.valid(true)
                .hoaDonId(hoaDon.getId())
                .maDonHang(hoaDon.getMaDonHang())
                .tenSanPham(spct.getSanPham() != null ? spct.getSanPham().getTenSanPham() : null)
                .maSanPham(spct.getSanPham() != null ? spct.getSanPham().getMaSanPham() : null)
                .tenBienThe(buildTenBienThe(spct))
                .donGia(hoaDonChiTiet.getDonGia())
                .sanPhamChiTietId(spct.getId())
                .hoaDonChiTietId(hoaDonChiTiet.getId());

        if (existingHoanTra.isPresent()) {
            builder.hoanTraId(existingHoanTra.get().getId())
                    .maHoanTra(existingHoanTra.get().getMaHoanTra());
        }

        return builder.build();
    }

    @Override
    public BigDecimal calculateRefundAmount(Integer hoanTraId) {
        List<HoanTraChiTiet> chiTietList = hoanTraChiTietRepository.findByHoanTraId(hoanTraId);
        if (chiTietList == null || chiTietList.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return chiTietList.stream()
                .map(HoanTraChiTiet::getSoTienHoan)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String generateMaPhieu() {
        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String random = String.format("%04d", new Random().nextInt(10000));
        return "PNK" + timestamp + random;
    }

    private String getTrangThaiText(Integer trangThai) {
        if (trangThai == null) return "Không xác định";
        switch (trangThai) {
            case 0: return "Trong kho";
            case 1: return "Đã bán";
            case 2: return "Bảo hành";
            case 3: return "Đã hoàn trả";
            default: return "Không xác định";
        }
    }

    private String buildTenBienThe(SanPhamChiTiet spct) {
        List<String> parts = new ArrayList<>();
        if (spct.getMauSac() != null && spct.getMauSac().getTenMauSac() != null) {
            parts.add(spct.getMauSac().getTenMauSac());
        }
        if (spct.getKichThuoc() != null && spct.getKichThuoc().getTenKichThuoc() != null) {
            parts.add(spct.getKichThuoc().getTenKichThuoc());
        }
        if (spct.getChatLieuDay() != null && spct.getChatLieuDay().getTenChatLieu() != null) {
            parts.add(spct.getChatLieuDay().getTenChatLieu());
        }
        return String.join(", ", parts);
    }

    private PhieuNhapKhoDTO convertToDTO(PhieuNhapKho phieu) {
        PhieuNhapKhoDTO.PhieuNhapKhoDTOBuilder builder = PhieuNhapKhoDTO.builder()
                .id(phieu.getId())
                .maPhieu(phieu.getMaPhieu())
                .loaiPhieu(phieu.getLoaiPhieu())
                .loaiPhieuHienThi(getLoaiPhieuHienThi(phieu.getLoaiPhieu()))
                .idHoanTra(phieu.getHoanTra() != null ? phieu.getHoanTra().getId() : null)
                .maHoanTra(phieu.getHoanTra() != null ? phieu.getHoanTra().getMaHoanTra() : null)
                .idNhanVien(phieu.getNhanVien() != null ? phieu.getNhanVien().getId() : null)
                .tenNhanVien(phieu.getNhanVien() != null ? phieu.getNhanVien().getTenNguoiDung() : null)
                .soTienHoan(phieu.getSoTienHoan())
                .soTienHoanFormatted(phieu.getSoTienHoan() != null 
                        ? phieu.getSoTienHoan().toPlainString() : "0")
                .phuongThucHoanTien(phieu.getPhuongThucHoanTien())
                .phuongThucHoanTienHienThi(getPhuongThucHienThi(phieu.getPhuongThucHoanTien()))
                .ghiChu(phieu.getGhiChu())
                .ngayTao(phieu.getNgayTao())
                .ngayTaoFormatted(phieu.getNgayTao() != null 
                        ? phieu.getNgayTao().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : null);

        if (phieu.getChiTietList() != null && !phieu.getChiTietList().isEmpty()) {
            List<PhieuNhapKhoDTO.ChiTietPhieuNhapKhoDTO> chiTietDTOs = phieu.getChiTietList().stream()
                    .map(this::convertChiTietToDTO)
                    .collect(Collectors.toList());
            builder.chiTietList(chiTietDTOs);
        }

        return builder.build();
    }

    private PhieuNhapKhoDTO.ChiTietPhieuNhapKhoDTO convertChiTietToDTO(ChiTietPhieuNhapKho chiTiet) {
        SanPhamChiTiet spct = chiTiet.getSanPhamChiTiet();
        
        return PhieuNhapKhoDTO.ChiTietPhieuNhapKhoDTO.builder()
                .id(chiTiet.getId())
                .idPhieuNhapKho(chiTiet.getPhieuNhapKho() != null ? chiTiet.getPhieuNhapKho().getId() : null)
                .idSanPhamChiTiet(spct != null ? spct.getId() : null)
                .tenSanPham(spct != null && spct.getSanPham() != null ? spct.getSanPham().getTenSanPham() : null)
                .maSanPham(spct != null && spct.getSanPham() != null ? spct.getSanPham().getMaSanPham() : null)
                .tenBienThe(buildTenBienThe(spct))
                .mauSac(spct != null && spct.getMauSac() != null ? spct.getMauSac().getTenMauSac() : null)
                .kichThuoc(spct != null && spct.getKichThuoc() != null ? spct.getKichThuoc().getTenKichThuoc() : null)
                .chatLieuDay(spct != null && spct.getChatLieuDay() != null ? spct.getChatLieuDay().getTenChatLieu() : null)
                .idSerial(chiTiet.getSerialSanPham() != null ? chiTiet.getSerialSanPham().getId() : null)
                .maSerial(chiTiet.getSerialSanPham() != null ? chiTiet.getSerialSanPham().getMaSerial() : null)
                .soLuong(chiTiet.getSoLuong())
                .donGia(chiTiet.getDonGia())
                .ghiChu(chiTiet.getGhiChu())
                .build();
    }

    private String getLoaiPhieuHienThi(String loaiPhieu) {
        if (loaiPhieu == null) return "Không xác định";
        switch (loaiPhieu) {
            case PhieuNhapKho.LOAI_TU_HoanTra: return "Hoàn trả";
            case PhieuNhapKho.LOAI_NHAP_KHO_BINH_THUONG: return "Nhập kho thường";
            default: return loaiPhieu;
        }
    }

    private String getPhuongThucHienThi(String phuongThuc) {
        if (phuongThuc == null) return "Không xác định";
        switch (phuongThuc) {
            case HoanTra.PHUONG_THUC_TIEN_MAT: return "Tiền mặt";
            case HoanTra.PHUONG_THUC_CHUYEN_KHOAN: return "Chuyển khoản";
            case HoanTra.PHUONG_THUC_VI_DIEN_TU: return "Ví điện tử";
            default: return phuongThuc;
        }
    }
}
