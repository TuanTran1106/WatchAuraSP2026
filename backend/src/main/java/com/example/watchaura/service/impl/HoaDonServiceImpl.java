package com.example.watchaura.service.impl;

import com.example.watchaura.dto.DiaChiGiaoHangDTO;
import com.example.watchaura.dto.HoaDonChiTietDTO;
import com.example.watchaura.dto.HoaDonDTO;
import com.example.watchaura.dto.HoaDonRequest;
import com.example.watchaura.dto.HoaDonChiTietRequest;
import com.example.watchaura.entity.DiaChiGiaoHang;
import com.example.watchaura.entity.HoaDon;
import com.example.watchaura.entity.HoaDonChiTiet;
import com.example.watchaura.entity.KhachHang;
import com.example.watchaura.entity.SanPhamChiTiet;
import com.example.watchaura.entity.Voucher;
import com.example.watchaura.entity.VoucherUser;
import com.example.watchaura.repository.DiaChiGiaoHangRepository;
import com.example.watchaura.repository.HoaDonChiTietRepository;
import com.example.watchaura.repository.HoaDonRepository;
import com.example.watchaura.repository.KhachHangRepository;
import com.example.watchaura.repository.SanPhamChiTietRepository;
import com.example.watchaura.repository.VoucherRepository;
import com.example.watchaura.repository.VoucherUserRepository;
import com.example.watchaura.service.HoaDonService;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.example.watchaura.service.HoaDonService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.io.ByteArrayOutputStream;
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
    private final VoucherUserRepository voucherUserRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final DiaChiGiaoHangRepository diaChiGiaoHangRepository;

    @Override
    @Transactional(readOnly = true)
    public List<HoaDonDTO> getAll() {
        return hoaDonRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public HoaDonDTO getById(Integer id) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + id));
        return convertToDTO(hoaDon);
    }

    @Override
    @Transactional(readOnly = true)
    public HoaDonDTO getByMaDonHang(String maDonHang) {
        HoaDon hoaDon = hoaDonRepository.findByMaDonHang(maDonHang)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với mã: " + maDonHang));
        return convertToDTO(hoaDon);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HoaDonDTO> getByKhachHangId(Integer khachHangId) {
        return hoaDonRepository.findByKhachHangIdOrderByNgayDatDesc(khachHangId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HoaDonDTO> getByTrangThaiDonHang(String trangThaiDonHang) {
        return hoaDonRepository.findByTrangThaiDonHang(trangThaiDonHang).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<HoaDonDTO> search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return getAll();
        }
        String q = keyword.trim();
        return hoaDonRepository
                .findByMaDonHangContainingIgnoreCaseOrTenKhachHangContainingIgnoreCase(q, q)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HoaDonDTO> searchPage(String keyword, String trangThai, Pageable pageable) {
        String q = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        String status = (trangThai == null || trangThai.isBlank()) ? null : trangThai.trim();

        Page<HoaDon> page;
        if (status != null) {
            if (q != null) {
                page = hoaDonRepository.findByTrangThaiAndKeyword(status, q, pageable);
            } else {
                page = hoaDonRepository.findByTrangThaiDonHang(status, pageable);
            }
        } else {
            if (q != null) {
                page = hoaDonRepository
                        .findByMaDonHangContainingIgnoreCaseOrTenKhachHangContainingIgnoreCase(q, q, pageable);
            } else {
                page = hoaDonRepository.findAll(pageable);
            }
        }
        return page.map(this::convertToDTO);
    }

    @Override
    @Transactional
    public HoaDonDTO create(HoaDonRequest request) {
        // Validate khách hàng
        KhachHang khachHang = khachHangRepository.findById(request.getKhachHangId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        // Validate nhân viên
        KhachHang nhanVien = null;
        if (request.getNhanVienId() != null) {
            nhanVien = khachHangRepository.findById(request.getNhanVienId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));
        }

        // Validate voucher
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
            tienGiam = validateAndCalculateVoucherForUser(voucher, tongTienTamTinh, khachHang.getId());
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
            Integer used = voucher.getSoLuongDaDung() != null ? voucher.getSoLuongDaDung() : 0;
            voucher.setSoLuongDaDung(used + 1);
            voucherRepository.save(voucher);

            if (Boolean.TRUE.equals(voucher.getGioiHanMoiUser())) {
                VoucherUser voucherUser = voucherUserRepository
                        .findByVoucherIdAndKhachHangId(voucher.getId(), khachHang.getId())
                        .orElse(null);
                if (voucherUser == null) {
                    voucherUser = new VoucherUser();
                    voucherUser.setVoucher(voucher);
                    voucherUser.setKhachHang(khachHang);
                    voucherUser.setSoLanDaDung(0);
                }
                Integer usedByUser = voucherUser.getSoLanDaDung() != null ? voucherUser.getSoLanDaDung() : 0;
                voucherUser.setSoLanDaDung(usedByUser + 1);
                voucherUser.setLanCuoiDung(LocalDateTime.now());
                voucherUserRepository.save(voucherUser);
            }
        }

        return convertToDTO(savedHoaDon);
    }

    @Override
    @Transactional
    public HoaDonDTO update(Integer id, HoaDonRequest request) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        if (!"CHO_XAC_NHAN".equals(hoaDon.getTrangThaiDonHang())) {
            throw new RuntimeException("Chỉ có thể cập nhật đơn hàng ở trạng thái CHỜ XÁC NHẬN");
        }

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

        hoaDon.setTrangThai(false);
        hoaDonRepository.save(hoaDon);
    }

    @Override
    public String generateMaDonHang() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "HD" + timestamp + uuid;
    }

    @Override
    public byte[] exportPdf(HoaDonDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("HoaDonDTO không được null");
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 10);

            Paragraph title = new Paragraph("HÓA ĐƠN BÁN HÀNG", fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            document.add(new Paragraph("Mã đơn: " + safe(dto.getMaDonHang()), fontNormal));
            if (dto.getNgayDat() != null) {
                String formatted = dto.getNgayDat().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                document.add(new Paragraph("Ngày đặt: " + formatted, fontNormal));
            }
            document.add(new Paragraph("Khách hàng: " + safe(dto.getTenKhachHang()), fontNormal));
            document.add(new Paragraph("SĐT: " + safe(dto.getSdtKhachHang()), fontNormal));
            document.add(new Paragraph("Địa chỉ: " + safe(dto.getDiaChi()), fontNormal));

            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{4f, 1.2f, 2f, 2f});

            table.addCell(createCell("Sản phẩm", fontHeader, Element.ALIGN_LEFT));
            table.addCell(createCell("SL", fontHeader, Element.ALIGN_CENTER));
            table.addCell(createCell("Đơn giá", fontHeader, Element.ALIGN_RIGHT));
            table.addCell(createCell("Thành tiền", fontHeader, Element.ALIGN_RIGHT));

            if (dto.getItems() != null) {
                for (HoaDonChiTietDTO ct : dto.getItems()) {
                    table.addCell(createCell(safe(ct.getTenSanPham()), fontNormal, Element.ALIGN_LEFT));
                    table.addCell(createCell(String.valueOf(ct.getSoLuong()), fontNormal, Element.ALIGN_CENTER));
                    table.addCell(createCell(formatMoney(ct.getDonGia()), fontNormal, Element.ALIGN_RIGHT));
                    table.addCell(createCell(formatMoney(ct.getThanhTien()), fontNormal, Element.ALIGN_RIGHT));
                }
            }
            document.add(table);

            document.add(new Paragraph(" "));
            BigDecimal tongTamTinh = dto.getTongTienTamTinh() != null ? dto.getTongTienTamTinh() : BigDecimal.ZERO;
            BigDecimal giam = dto.getTienGiam() != null ? dto.getTienGiam() : BigDecimal.ZERO;
            BigDecimal thanhToan = dto.getTongTienThanhToan() != null ? dto.getTongTienThanhToan() : BigDecimal.ZERO;

            document.add(new Paragraph("Tạm tính: " + formatMoney(tongTamTinh), fontNormal));
            document.add(new Paragraph("Giảm giá: " + formatMoney(giam), fontNormal));
            document.add(new Paragraph("Tổng thanh toán: " + formatMoney(thanhToan), fontHeader));
            document.add(new Paragraph("Phương thức thanh toán: " + safe(dto.getPhuongThucThanhToan()), fontNormal));

            document.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Lỗi khi xuất PDF: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi không xác định khi xuất PDF.", e);
        }
    }

    private static PdfPCell createCell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        return cell;
    }

    private static String safe(String s) {
        return s != null ? s : "";
    }

    private static String formatMoney(BigDecimal value) {
        if (value == null) return "0";
        return value.toPlainString();
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
        } else {
            tienGiam = voucher.getGiaTri();
            if (tienGiam.compareTo(tongTien) > 0) {
                tienGiam = tongTien;
            }
        }

        return tienGiam;
    }

    private BigDecimal validateAndCalculateVoucherForUser(Voucher voucher, BigDecimal tongTien, Integer khachHangId) {
        if (voucher == null) {
            return BigDecimal.ZERO;
        }

        if (!Boolean.TRUE.equals(voucher.getTrangThai())) {
            throw new RuntimeException("Voucher không hợp lệ hoặc đã bị khóa.");
        }

        LocalDateTime now = LocalDateTime.now();
        if (voucher.getNgayBatDau() != null && now.isBefore(voucher.getNgayBatDau())) {
            throw new RuntimeException("Voucher chưa đến thời gian sử dụng.");
        }
        if (voucher.getNgayKetThuc() != null && now.isAfter(voucher.getNgayKetThuc())) {
            throw new RuntimeException("Voucher đã hết hạn.");
        }

        Integer tong = voucher.getSoLuongTong();
        Integer daDung = voucher.getSoLuongDaDung() != null ? voucher.getSoLuongDaDung() : 0;
        if (tong != null && tong - daDung <= 0) {
            throw new RuntimeException("Voucher đã hết lượt sử dụng.");
        }

        if (voucher.getDonHangToiThieu() != null
                && tongTien.compareTo(voucher.getDonHangToiThieu()) < 0) {
            throw new RuntimeException("Đơn hàng chưa đạt giá trị tối thiểu để áp dụng voucher.");
        }

        if (Boolean.TRUE.equals(voucher.getGioiHanMoiUser())) {
            if (khachHangId == null) {
                throw new RuntimeException("Không xác định được khách hàng để áp dụng voucher.");
            }
            VoucherUser existing = voucherUserRepository
                    .findByVoucherIdAndKhachHangId(voucher.getId(), khachHangId)
                    .orElse(null);
            if (existing != null) {
                Integer usedByUser = existing.getSoLanDaDung() != null ? existing.getSoLanDaDung() : 0;
                if (usedByUser >= 1) {
                    throw new RuntimeException("Bạn đã sử dụng voucher này.");
                }
            }
        }

        return calculateVoucherDiscount(voucher, tongTien);
    }

    private HoaDonDTO convertToDTO(HoaDon hoaDon) {
        HoaDonDTO dto = new HoaDonDTO();
        dto.setId(hoaDon.getId());
        dto.setMaDonHang(hoaDon.getMaDonHang());

        if (hoaDon.getKhachHang() != null) {
            dto.setKhachHangId(hoaDon.getKhachHang().getId());
        }

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

        try {
            List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByHoaDonId(hoaDon.getId());
            List<HoaDonChiTietDTO> chiTietDTOs = chiTiets.stream()
                    .map(this::convertChiTietToDTO)
                    .collect(Collectors.toList());
            dto.setItems(chiTietDTOs);
        } catch (Exception ignored) {
        }

        try {
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
        } catch (Exception ignored) {
        }

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
