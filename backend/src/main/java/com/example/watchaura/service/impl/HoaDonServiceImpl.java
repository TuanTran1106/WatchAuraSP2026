package com.example.watchaura.service.impl;

import com.example.watchaura.dto.CheckoutStockResponse;
import com.example.watchaura.dto.StockWarningItem;
import com.example.watchaura.dto.DiaChiGiaoHangDTO;
import com.example.watchaura.dto.HoaDonChiTietDTO;
import com.example.watchaura.dto.HoaDonDTO;
import com.example.watchaura.dto.HoaDonRequest;
import com.example.watchaura.dto.HoaDonChiTietRequest;
import com.example.watchaura.dto.KhuyenMaiPriceResult;
import com.example.watchaura.entity.DiaChiGiaoHang;
import com.example.watchaura.entity.GioHangChiTiet;
import com.example.watchaura.entity.HoaDon;
import com.example.watchaura.entity.HoaDonChiTiet;
import com.example.watchaura.entity.KhuyenMai;
import com.example.watchaura.entity.KhachHang;
import com.example.watchaura.entity.SanPhamChiTiet;
import com.example.watchaura.entity.Voucher;
import com.example.watchaura.entity.VoucherUser;
import com.example.watchaura.repository.DiaChiGiaoHangRepository;
import com.example.watchaura.repository.GioHangChiTietRepository;
import com.example.watchaura.repository.HoaDonChiTietRepository;
import com.example.watchaura.repository.HoaDonRepository;
import com.example.watchaura.repository.KhachHangRepository;
import com.example.watchaura.repository.SanPhamChiTietRepository;
import com.example.watchaura.repository.VoucherRepository;
import com.example.watchaura.repository.VoucherUserRepository;
import com.example.watchaura.service.HoaDonService;
import com.example.watchaura.service.KhuyenMaiService;
import com.example.watchaura.service.SanPhamChiTietKhuyenMaiService;
import com.example.watchaura.service.ghn.ShippingService;
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
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HoaDonServiceImpl implements HoaDonService {
    private static final List<String> STOCK_RESERVED_STATUSES = List.of("CHO_XAC_NHAN", "CHO_THANH_TOAN");

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final KhachHangRepository khachHangRepository;
    private final VoucherRepository voucherRepository;
    private final VoucherUserRepository voucherUserRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final DiaChiGiaoHangRepository diaChiGiaoHangRepository;
    private final GioHangChiTietRepository gioHangChiTietRepository;
    private final SanPhamChiTietKhuyenMaiService sanPhamChiTietKhuyenMaiService;
    private final ShippingService shippingService;
    private final KhuyenMaiService khuyenMaiService;

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
            // UI dùng DA_THANH_TOAN; DB bán tại quầy / sau cập nhật có thể lưu "DA THANH TOAN"
            if ("DA_THANH_TOAN".equals(status)) {
                List<String> paidCodes = List.of("DA_THANH_TOAN", "DA THANH TOAN");
                if (q != null) {
                    page = hoaDonRepository.findByTrangThaiInAndKeyword(paidCodes, q, pageable);
                } else {
                    page = hoaDonRepository.findByTrangThaiDonHangInAndTrangThai(paidCodes, pageable);
                }
            } else if (q != null) {
                page = hoaDonRepository.findByTrangThaiAndKeyword(status, q, pageable);
            } else {
                page = hoaDonRepository.findByTrangThaiDonHangAndTrangThai(status, pageable);
            }
        } else {
            if (q != null) {
                page = hoaDonRepository
                        .findByMaDonHangContainingIgnoreCaseOrTenKhachHangContainingIgnoreCaseAndTrangThai(q, q, pageable);
            } else {
                page = hoaDonRepository.findActiveOrders(pageable);
            }
        }
        return page.map(this::convertToDTO);
    }

    @Override
    @Transactional
    public HoaDonDTO create(HoaDonRequest request) {
        KhachHang khachHang = khachHangRepository.findById(request.getKhachHangId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));

        KhachHang nhanVien = null;
        if (request.getNhanVienId() != null) {
            nhanVien = khachHangRepository.findById(request.getNhanVienId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên"));
        }

        Voucher voucher = null;
        if (request.getVoucherId() != null) {
            voucher = voucherRepository.findById(request.getVoucherId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy voucher"));
        }

        LocalDateTime thoiDiemDat = LocalDateTime.now();
        List<KhuyenMai> khuyenMaiDangChay = khuyenMaiService.getActivePromotions(thoiDiemDat);

        // Tính tổng tiền + chặn oversell:
        // kiểm tra theo tồn thực tế trừ đi lượng đang "giữ chỗ" ở các đơn chờ.
        BigDecimal tongTienTamTinh = BigDecimal.ZERO;
        for (HoaDonChiTietRequest itemRequest : request.getItems()) {
            SanPhamChiTiet sanPhamChiTiet = sanPhamChiTietRepository.findByIdWithLock(itemRequest.getSanPhamChiTietId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm chi tiết"));

            Integer soLuongTon = sanPhamChiTiet.getSoLuongTon() != null ? sanPhamChiTiet.getSoLuongTon() : 0;
            Integer dangGiu = hoaDonChiTietRepository.sumReservedQtyBySanPhamChiTietId(
                    itemRequest.getSanPhamChiTietId(),
                    STOCK_RESERVED_STATUSES
            );
            int soLuongDangGiu = dangGiu != null ? dangGiu : 0;
            int soLuongConCoTheDat = Math.max(0, soLuongTon - soLuongDangGiu);

            if (soLuongConCoTheDat < itemRequest.getSoLuong()) {
                throw new RuntimeException("Số lượng tồn kho không đủ cho sản phẩm: "
                        + (sanPhamChiTiet.getSanPham() != null ? sanPhamChiTiet.getSanPham().getTenSanPham() : "ID " + itemRequest.getSanPhamChiTietId())
                        + ". Khả dụng để đặt lúc này: " + soLuongConCoTheDat
                        + " (tồn: " + soLuongTon + ", đang giữ: " + soLuongDangGiu + ").");
            }

            SanPhamChiTiet forPrice = sanPhamChiTietRepository.findByIdWithDetails(itemRequest.getSanPhamChiTietId())
                    .orElse(sanPhamChiTiet);
            KhuyenMaiPriceResult dongGiaKm = sanPhamChiTietKhuyenMaiService.resolveBestForCartOrOrderLine(
                    forPrice, thoiDiemDat, khuyenMaiDangChay);
            tongTienTamTinh = tongTienTamTinh.add(
                    dongGiaKm.giaSauGiam().multiply(BigDecimal.valueOf(itemRequest.getSoLuong())));
        }

        BigDecimal tienGiam = BigDecimal.ZERO;
        if (voucher != null) {
            tienGiam = validateAndCalculateVoucherForUser(voucher, tongTienTamTinh, khachHang.getId());
        }

        BigDecimal tienHangSauGiam = tongTienTamTinh.subtract(tienGiam);
        if (tienHangSauGiam.compareTo(BigDecimal.ZERO) < 0) {
            tienHangSauGiam = BigDecimal.ZERO;
        }
        BigDecimal phiVanChuyen;
        try {
            phiVanChuyen = shippingService.calculateShippingFee(tienHangSauGiam, request.getToDistrictId(), request.getToWardCode());
        } catch (Exception e) {
            throw new RuntimeException("Không tính được phí giao hàng (GHN). Vui lòng chọn lại quận/huyện, phường/xã.");
        }
        BigDecimal tongTienThanhToan = tienHangSauGiam.add(phiVanChuyen);

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
        boolean isVnPay = request.getPhuongThucThanhToan() != null
                && request.getPhuongThucThanhToan().toUpperCase().contains("VNPAY");
        hoaDon.setTrangThaiDonHang(isVnPay ? "CHO_THANH_TOAN" : "CHO_XAC_NHAN");
        hoaDon.setNgayDat(thoiDiemDat);
        hoaDon.setDiaChi(request.getDiaChi());
        hoaDon.setTenKhachHang(request.getTenKhachHang());
        hoaDon.setSdtKhachHang(request.getSdtKhachHang());
        hoaDon.setGhiChu(request.getGhiChu());

        HoaDon savedHoaDon = hoaDonRepository.save(hoaDon);

        // Tạo chi tiết hóa đơn (giữ chỗ logic thông qua trạng thái đơn chờ)
        for (HoaDonChiTietRequest itemRequest : request.getItems()) {
            SanPhamChiTiet sanPhamChiTiet = sanPhamChiTietRepository.findById(itemRequest.getSanPhamChiTietId())
                    .orElseThrow();

            SanPhamChiTiet forPrice = sanPhamChiTietRepository.findByIdWithDetails(itemRequest.getSanPhamChiTietId())
                    .orElse(sanPhamChiTiet);
            KhuyenMaiPriceResult dongGiaKm = sanPhamChiTietKhuyenMaiService.resolveBestForCartOrOrderLine(
                    forPrice, thoiDiemDat, khuyenMaiDangChay);

            HoaDonChiTiet hoaDonChiTiet = new HoaDonChiTiet();
            hoaDonChiTiet.setHoaDon(savedHoaDon);
            hoaDonChiTiet.setSanPhamChiTiet(sanPhamChiTiet);
            hoaDonChiTiet.setSoLuong(itemRequest.getSoLuong());
            hoaDonChiTiet.setDonGia(dongGiaKm.giaSauGiam());
            hoaDonChiTietRepository.save(hoaDonChiTiet);
        }

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

    /** Chuẩn hóa mã đã thanh toán (DB/UI có thể dùng dấu cách hoặc gạch dưới). */
    private static String normalizeDaThanhToanCode(String s) {
        if (s == null) return null;
        return "DA_THANH_TOAN".equals(s) ? "DA THANH TOAN" : s;
    }

    private static boolean isDaThanhToanCode(String s) {
        return "DA THANH TOAN".equals(s) || "DA_THANH_TOAN".equals(s);
    }

    @Override
    @Transactional
    public HoaDonDTO updateTrangThaiDonHang(Integer id, String trangThaiDonHang) {
        HoaDon hoaDon = hoaDonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        String newStatus = normalizeDaThanhToanCode(trangThaiDonHang);
        String currentStatus = hoaDon.getTrangThaiDonHang();
        String effectiveCurrent = normalizeDaThanhToanCode(currentStatus);

        if ("DA_GIAO".equals(effectiveCurrent) && !"DA_GIAO".equals(newStatus) && !"HOAN_THANH".equals(newStatus)) {
            throw new RuntimeException("Đơn đã giao, không thể đổi sang trạng thái khác.");
        }

        if ("DA_HUY".equals(effectiveCurrent) && !"DA_HUY".equals(newStatus)) {
            throw new RuntimeException("Đơn đã hủy, không thể đổi sang trạng thái khác.");
        }

        if (isDaThanhToanCode(effectiveCurrent) && !isDaThanhToanCode(newStatus)) {
            throw new RuntimeException("Đơn đã thanh toán, không thể đổi sang trạng thái khác.");
        }

        // Không cho chuyển từ "Đang giao" sang "Đã hủy"
        if ("DANG_GIAO".equals(effectiveCurrent) && "DA_HUY".equals(newStatus)) {
            throw new RuntimeException("Đơn đang giao, không thể hủy. Vui lòng chờ đơn được giao thành công hoặc liên hệ khách hàng.");
        }

        // Xử lý khi xác nhận đơn (DA_XAC_NHAN) - trừ tồn kho ngay khi xác nhận (FIFO)
        if ("DA_XAC_NHAN".equals(newStatus) && !"DA_XAC_NHAN".equals(effectiveCurrent)) {
            List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByHoaDonId(id);
            StringBuilder loiTonKho = new StringBuilder();

            // Kiểm tra tất cả sản phẩm trước
            for (HoaDonChiTiet chiTiet : chiTiets) {
                SanPhamChiTiet sp = sanPhamChiTietRepository.findByIdWithLock(chiTiet.getSanPhamChiTiet().getId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
                Integer soLuongTon = sp.getSoLuongTon() != null ? sp.getSoLuongTon() : 0;
                if (soLuongTon < chiTiet.getSoLuong()) {
                    String tenSp = sp.getSanPham() != null ? sp.getSanPham().getTenSanPham() : "ID " + chiTiet.getSanPhamChiTiet().getId();
                    loiTonKho.append(String.format("%s: cần %d, chỉ còn %d; ",
                            tenSp, chiTiet.getSoLuong(), soLuongTon));
                }
            }

            // Nếu có lỗi tồn kho -> KHÔNG xác nhận được, CHUYỂN sang "Cần xử lý"
            if (loiTonKho.length() > 0) {
                String lyDoHetHang = loiTonKho.toString();
                hoaDon.setGhiChu("Không đủ tồn kho: " + lyDoHetHang);
                hoaDon.setTrangThaiDonHang("CAN_XU_LY");
                return convertToDTO(hoaDonRepository.save(hoaDon));
            }

            // Đủ tồn kho -> trừ kho và xác nhận
            for (HoaDonChiTiet chiTiet : chiTiets) {
                sanPhamChiTietRepository.deductStock(chiTiet.getSanPhamChiTiet().getId(), chiTiet.getSoLuong());
            }
        }

        // Xử lý khi chuyển sang "Đã thanh toán" - trừ tồn kho nếu chưa trừ
        if (isDaThanhToanCode(newStatus) && !isDaThanhToanCode(effectiveCurrent)) {
            // Nếu đơn chưa xác nhận thì trừ kho ở đây
            if (!"DA_XAC_NHAN".equals(effectiveCurrent)) {
                List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByHoaDonId(id);
                StringBuilder loiTonKho = new StringBuilder();

                for (HoaDonChiTiet chiTiet : chiTiets) {
                    SanPhamChiTiet sp = sanPhamChiTietRepository.findByIdWithLock(chiTiet.getSanPhamChiTiet().getId())
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
                    Integer soLuongTon = sp.getSoLuongTon() != null ? sp.getSoLuongTon() : 0;
                    if (soLuongTon < chiTiet.getSoLuong()) {
                        String tenSp = sp.getSanPham() != null ? sp.getSanPham().getTenSanPham() : "ID " + chiTiet.getSanPhamChiTiet().getId();
                        loiTonKho.append(String.format("%s: cần %d, chỉ còn %d; ",
                                tenSp, chiTiet.getSoLuong(), soLuongTon));
                    }
                }

                // Nếu có lỗi tồn kho -> KHÔNG thanh toán được, CHUYỂN sang "Cần xử lý"
                if (loiTonKho.length() > 0) {
                    String lyDoHetHang = loiTonKho.toString();
                    hoaDon.setGhiChu("Không đủ tồn kho: " + lyDoHetHang);
                    hoaDon.setTrangThaiDonHang("CAN_XU_LY");
                    return convertToDTO(hoaDonRepository.save(hoaDon));
                }

                // Đủ tồn kho -> trừ kho
                for (HoaDonChiTiet chiTiet : chiTiets) {
                    sanPhamChiTietRepository.deductStock(chiTiet.getSanPhamChiTiet().getId(), chiTiet.getSoLuong());
                }
            }
        }

        // Hoàn tồn kho khi hủy đơn (nếu đơn đã xác nhận/thanhtoán); KHÔNG cho hủy khi đã thanh toán
        if ("DA_HUY".equals(newStatus) && !"DA_HUY".equals(effectiveCurrent)) {
            if (isDaThanhToanCode(effectiveCurrent)) {
                throw new RuntimeException("Không thể hủy đơn đã thanh toán.");
            }
            if ("DA_XAC_NHAN".equals(effectiveCurrent)) {
                List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByHoaDonId(id);
                for (HoaDonChiTiet chiTiet : chiTiets) {
                    sanPhamChiTietRepository.findByIdWithLock(chiTiet.getSanPhamChiTiet().getId())
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
                    sanPhamChiTietRepository.restoreStock(chiTiet.getSanPhamChiTiet().getId(), chiTiet.getSoLuong());
                }
            }
        }

        hoaDon.setTrangThaiDonHang(newStatus);
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

    @Override
    public byte[] exportRevenueReportPdf(List<HoaDonDTO> orders, LocalDate fromDate, LocalDate toDate, String statusFilter) {
        List<HoaDonDTO> safeOrders = orders != null ? orders : Collections.emptyList();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            Font fontTitle = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font fontHeader = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
            Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 10);

            Paragraph title = new Paragraph("BAO CAO DOANH THU", fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Tu ngay: " + (fromDate != null ? fromDate : "Tat ca"), fontNormal));
            document.add(new Paragraph("Den ngay: " + (toDate != null ? toDate : "Tat ca"), fontNormal));
            document.add(new Paragraph("Trang thai loc: " + safe(statusFilter), fontNormal));
            document.add(new Paragraph("So don: " + safeOrders.size(), fontNormal));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2f, 2f, 3f, 2f, 2f});

            table.addCell(createCell("Ma don", fontHeader, Element.ALIGN_LEFT));
            table.addCell(createCell("Ngay dat", fontHeader, Element.ALIGN_LEFT));
            table.addCell(createCell("Khach hang", fontHeader, Element.ALIGN_LEFT));
            table.addCell(createCell("Trang thai", fontHeader, Element.ALIGN_LEFT));
            table.addCell(createCell("Tong tien", fontHeader, Element.ALIGN_RIGHT));

            BigDecimal total = BigDecimal.ZERO;
            for (HoaDonDTO order : safeOrders) {
                table.addCell(createCell(safe(order.getMaDonHang()), fontNormal, Element.ALIGN_LEFT));
                table.addCell(createCell(order.getNgayDat() != null
                        ? order.getNgayDat().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                        : "", fontNormal, Element.ALIGN_LEFT));
                table.addCell(createCell(safe(order.getTenKhachHang()), fontNormal, Element.ALIGN_LEFT));
                table.addCell(createCell(safe(order.getTrangThaiDonHang()), fontNormal, Element.ALIGN_LEFT));
                BigDecimal paid = order.getTongTienThanhToan() != null ? order.getTongTienThanhToan() : BigDecimal.ZERO;
                total = total.add(paid);
                table.addCell(createCell(formatMoney(paid), fontNormal, Element.ALIGN_RIGHT));
            }
            document.add(table);
            document.add(new Paragraph(" "));
            document.add(new Paragraph("Tong doanh thu: " + formatMoney(total), fontHeader));

            document.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            throw new RuntimeException("Loi khi xuat bao cao PDF: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Loi khong xac dinh khi xuat bao cao doanh thu PDF.", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal tinhTienGiamVoucher(Integer khachHangId, Integer voucherId, BigDecimal tongTien) {
        if (voucherId == null || tongTien == null) {
            return BigDecimal.ZERO;
        }
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy voucher"));
        return validateAndCalculateVoucherForUser(voucher, tongTien, khachHangId);
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
        String loai = voucher.getLoaiVoucher() != null ? voucher.getLoaiVoucher().trim().toUpperCase() : "";
        boolean isPercent = loai.equals("PERCENT") || loai.equals("PERCENTAGE") || loai.equals("PHAN_TRAM") || loai.equals("PHẦN_TRĂM") || loai.equals("%") || loai.equals("PT");
        if (isPercent) {
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
            throw new RuntimeException("�ơn hàng chưa đạt giá trị tối thiểu để áp dụng voucher.");
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

        String emailOnOrder = hoaDon.getEmail();
        if (emailOnOrder != null && !emailOnOrder.isBlank()) {
            dto.setEmail(emailOnOrder.trim());
        }
        if (hoaDon.getKhachHang() != null) {
            dto.setKhachHangId(hoaDon.getKhachHang().getId());
            if (dto.getEmail() == null || dto.getEmail().isBlank()) {
                dto.setEmail(hoaDon.getKhachHang().getEmail());
            }
            dto.setGioiTinh(hoaDon.getKhachHang().getGioiTinh());
            dto.setNgaySinh(hoaDon.getKhachHang().getNgaySinh());
        }

        dto.setTenKhachHang(hoaDon.getTenKhachHang());
        dto.setNhanVienId(hoaDon.getNhanVien() != null ? hoaDon.getNhanVien().getId() : null);
        dto.setVoucherId(hoaDon.getVoucher() != null ? hoaDon.getVoucher().getId() : null);
        dto.setMaVoucher(hoaDon.getVoucher() != null ? hoaDon.getVoucher().getMaVoucher() : null);
        dto.setTongTienTamTinh(hoaDon.getTongTienTamTinh());
        // Tổng tiền gốc (chưa trừ khuyến mãi) = tạm tính + tienGiam
        BigDecimal tienGiamVal = hoaDon.getTienGiam() != null ? hoaDon.getTienGiam() : BigDecimal.ZERO;
        BigDecimal tamTinh = hoaDon.getTongTienTamTinh() != null ? hoaDon.getTongTienTamTinh() : BigDecimal.ZERO;
        dto.setTongTienChuaGiam(tamTinh.add(tienGiamVal));
        dto.setTienGiam(hoaDon.getTienGiam());
        dto.setTongTienThanhToan(hoaDon.getTongTienThanhToan());
        dto.setPhiVanChuyen(derivePhiVanChuyen(
                hoaDon.getTongTienTamTinh(),
                hoaDon.getTienGiam(),
                hoaDon.getTongTienThanhToan()));
        dto.setPhuongThucThanhToan(hoaDon.getPhuongThucThanhToan());
        dto.setLoaiHoaDon(hoaDon.getLoaiHoaDon());
        dto.setTrangThai(hoaDon.getTrangThai());
        dto.setTrangThaiDonHang(hoaDon.getTrangThaiDonHang());
        dto.setNgayDat(hoaDon.getNgayDat());
        dto.setDiaChi(hoaDon.getDiaChi());
        dto.setSdtKhachHang(hoaDon.getSdtKhachHang());
        dto.setGhiChu(hoaDon.getGhiChu());

        try {
            List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByHoaDonIdWithDetails(hoaDon.getId());
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

    /**
     * Phí vận chuyển không lưu cột riêng: suy ra từ tổng thanh toán = (tạm tính − giảm giá) + phí ship.
     */
    private static BigDecimal derivePhiVanChuyen(BigDecimal tongTienTamTinh, BigDecimal tienGiam, BigDecimal tongTienThanhToan) {
        BigDecimal tam = tongTienTamTinh != null ? tongTienTamTinh : BigDecimal.ZERO;
        BigDecimal giam = tienGiam != null ? tienGiam : BigDecimal.ZERO;
        BigDecimal tt = tongTienThanhToan != null ? tongTienThanhToan : BigDecimal.ZERO;
        BigDecimal phi = tt.subtract(tam).add(giam);
        if (phi.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return phi;
    }

    private HoaDonChiTietDTO convertChiTietToDTO(HoaDonChiTiet chiTiet) {
        HoaDonChiTietDTO dto = new HoaDonChiTietDTO();
        dto.setId(chiTiet.getId());
        dto.setHoaDonId(chiTiet.getHoaDon().getId());
        dto.setSanPhamChiTietId(chiTiet.getSanPhamChiTiet().getId());

        if (chiTiet.getSanPhamChiTiet().getSanPham() != null) {
            dto.setTenSanPham(chiTiet.getSanPhamChiTiet().getSanPham().getTenSanPham());
            dto.setHinhAnh(chiTiet.getSanPhamChiTiet().getSanPham().getHinhAnh());
        }

        dto.setTenBienThe(buildTenBienTheForChiTiet(chiTiet.getSanPhamChiTiet()));

        dto.setSoLuong(chiTiet.getSoLuong());
        dto.setDonGia(chiTiet.getDonGia());

        // Lấy tồn kho khả dụng hiện tại của sản phẩm chi tiết
        SanPhamChiTiet spct = chiTiet.getSanPhamChiTiet();
        dto.setSoLuongKhaDung(spct.getSoLuongKhaDung());
        dto.setGiaGoc(spct.getGiaBan() != null ? spct.getGiaBan() : chiTiet.getDonGia());

        // Thành tiền dựa trên giá đã giảm (đúng như đã lưu)
        if (dto.getDonGia() != null && dto.getSoLuong() != null) {
            dto.setThanhTien(dto.getDonGia().multiply(BigDecimal.valueOf(dto.getSoLuong())));
        } else {
            dto.setThanhTien(BigDecimal.ZERO);
        }

        return dto;
    }

    private static String buildTenBienTheForChiTiet(SanPhamChiTiet spct) {
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

    @Override
    @Transactional(readOnly = true)
    public Page<HoaDonDTO> filterDonHang(
            Integer userId,
            String trangThai,
            String thanhToan,
            LocalDate ngay,
            String maDon,
            Pageable pageable) {

        LocalDateTime ngayStart = (ngay != null) ? ngay.atStartOfDay() : null;
        LocalDateTime ngayEnd = (ngay != null) ? ngay.plusDays(1).atStartOfDay() : null;

        Page<HoaDon> page = hoaDonRepository.filterDonHang(
                userId,
                trangThai,
                thanhToan,
                ngayStart,
                ngayEnd,
                maDon,
                pageable
        );

        return page.map(this::convertToDTO);
    }

    @Override
    @Transactional
    public CheckoutStockResponse checkAndAdjustStockBeforeCheckout(Integer gioHangId, Integer khachHangId) {
        List<GioHangChiTiet> cartItems = gioHangChiTietRepository.findByGioHangId(gioHangId);
        List<StockWarningItem> warnings = new ArrayList<>();

        for (GioHangChiTiet item : cartItems) {
            SanPhamChiTiet spct = sanPhamChiTietRepository.findByIdWithLock(item.getSanPhamChiTiet().getId())
                    .orElse(null);
            if (spct == null) continue;

            // FIFO: Chỉ kiểm tra soLuongTon (không trừ giữ hàng)
            Integer tonKho = spct.getSoLuongTon() != null ? spct.getSoLuongTon() : 0;
            Integer khaDung = tonKho;

            Integer soLuongTrongGio = item.getSoLuong() != null ? item.getSoLuong() : 0;

            if (soLuongTrongGio > khaDung) {
                String tenSanPham = spct.getSanPham() != null ? spct.getSanPham().getTenSanPham() : "Sản phẩm";
                StringBuilder bienThe = new StringBuilder();
                if (spct.getMauSac() != null && !spct.getMauSac().getTenMauSac().isEmpty()) {
                    bienThe.append("Màu: ").append(spct.getMauSac().getTenMauSac());
                }
                if (spct.getKichThuoc() != null && !spct.getKichThuoc().getTenKichThuoc().isEmpty()) {
                    if (bienThe.length() > 0) bienThe.append(" / ");
                    bienThe.append("Size: ").append(spct.getKichThuoc().getTenKichThuoc());
                }
                if (spct.getChatLieuDay() != null && !spct.getChatLieuDay().getTenChatLieu().isEmpty()) {
                    if (bienThe.length() > 0) bienThe.append(" / ");
                    bienThe.append("Dây: ").append(spct.getChatLieuDay().getTenChatLieu());
                }

                Integer conLaiSauKhiDat = Math.max(0, khaDung - 1);
                Integer soLuongDuocDat = khaDung;

                if (khaDung > 0) {
                    item.setSoLuong(khaDung);
                    gioHangChiTietRepository.save(item);
                } else {
                    gioHangChiTietRepository.delete(item);
                    soLuongDuocDat = 0;
                }

                StockWarningItem warning = StockWarningItem.builder()
                        .sanPhamChiTietId(spct.getId())
                        .tenSanPham(tenSanPham)
                        .bienThe(bienThe.length() > 0 ? bienThe.toString() : null)
                        .soLuongYeuCau(soLuongTrongGio)
                        .soLuongKhaDung(khaDung)
                        .soLuongConLaiSauKhiDat(conLaiSauKhiDat)
                        .giaBan(spct.getGiaBan())
                        .soLuongDuocDat(soLuongDuocDat)
                        .build();
                warnings.add(warning);
            }
        }

        if (warnings.isEmpty()) {
            return CheckoutStockResponse.ok();
        }

        StringBuilder message = new StringBuilder();
        message.append("Một số sản phẩm trong giỏ hàng đã được cập nhật số lượng do tồn kho thay đổi. Chi tiết:\n");
        for (int i = 0; i < warnings.size(); i++) {
            StockWarningItem w = warnings.get(i);
            if (w.getSoLuongDuocDat() == null || w.getSoLuongDuocDat() == 0) {
                message.append(String.format("- %s: Yêu cầu %d, chỉ còn %d (đã xóa khỏi giỏ hàng)",
                        w.getTenSanPham(), w.getSoLuongYeuCau(), w.getSoLuongKhaDung()));
            } else {
                message.append(String.format("- %s: Yêu cầu %d, chỉ còn %d (đã cập nhật còn %d)",
                        w.getTenSanPham(), w.getSoLuongYeuCau(), w.getSoLuongKhaDung(), w.getSoLuongDuocDat()));
            }
            if (i < warnings.size() - 1) message.append("\n");
        }

        return CheckoutStockResponse.withWarnings(warnings, message.toString());
    }

    @Override
    @Transactional
    public HoaDonDTO editOrderItems(Integer hoaDonId, Map<Integer, Integer> itemsData) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn với ID: " + hoaDonId));


        if (!"CAN_XU_LY".equals(hoaDon.getTrangThaiDonHang())) {
            throw new RuntimeException("Chỉ có thể chỉnh sửa đơn hàng ở trạng thái Cần xử lý.");
        }


        List<HoaDonChiTiet> currentItems = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);
        if (currentItems == null || currentItems.isEmpty()) {
            throw new RuntimeException("Đơn hàng không có sản phẩm.");
        }

        // Bước 1 — chỉ kiểm tra tồn kho: nếu số lượng yêu cầu vượt tồn thì ném lỗi, KHÔNG sửa DB (transaction rollback).
        List<String> loiTonKho = new ArrayList<>();
        for (HoaDonChiTiet chiTiet : currentItems) {
            Integer spctId = chiTiet.getSanPhamChiTiet().getId();
            if (!itemsData.containsKey(spctId)) {
                continue;
            }
            Integer soLuongMoi = itemsData.get(spctId);
            if (soLuongMoi == null || soLuongMoi <= 0) {
                continue;
            }
            SanPhamChiTiet spct = sanPhamChiTietRepository.findByIdWithLock(spctId)
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại: " + spctId));
            int tonKho = spct.getSoLuongTon() != null ? spct.getSoLuongTon() : 0;
            if (soLuongMoi > tonKho) {
                String tenSanPham = spct.getSanPham() != null ? spct.getSanPham().getTenSanPham() : "Sản phẩm";
                loiTonKho.add(tenSanPham + ": cần " + soLuongMoi + ", chỉ còn " + tonKho);
            }
        }
        if (!loiTonKho.isEmpty()) {
            throw new RuntimeException(
                    "Một số sản phẩm không đủ hàng: Không đủ tồn kho: " + String.join("; ", loiTonKho));
        }

        LocalDateTime thoiDiemSua = LocalDateTime.now();
        List<KhuyenMai> khuyenMaiDangChay = khuyenMaiService.getActivePromotions(thoiDiemSua);

        BigDecimal tongTienTamTinh = BigDecimal.ZERO;

        for (HoaDonChiTiet chiTiet : currentItems) {
            Integer spctId = chiTiet.getSanPhamChiTiet().getId();

            if (!itemsData.containsKey(spctId)) {
                hoaDonChiTietRepository.delete(chiTiet);
                continue;
            }

            Integer soLuongMoi = itemsData.get(spctId);

            if (soLuongMoi == null || soLuongMoi <= 0) {
                hoaDonChiTietRepository.delete(chiTiet);
                continue;
            }

            SanPhamChiTiet spct = sanPhamChiTietRepository.findByIdWithLock(spctId)
                    .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại: " + spctId));

            chiTiet.setSoLuong(soLuongMoi);

            SanPhamChiTiet forPrice = sanPhamChiTietRepository.findByIdWithDetails(spctId).orElse(spct);
            KhuyenMaiPriceResult dongGiaKm = sanPhamChiTietKhuyenMaiService.resolveBestForCartOrOrderLine(
                    forPrice, thoiDiemSua, khuyenMaiDangChay);
            chiTiet.setDonGia(dongGiaKm.giaSauGiam());
            hoaDonChiTietRepository.save(chiTiet);

            // Tính thành tiền
            if (chiTiet.getDonGia() != null && chiTiet.getSoLuong() != null) {
                tongTienTamTinh = tongTienTamTinh.add(
                        chiTiet.getDonGia().multiply(BigDecimal.valueOf(chiTiet.getSoLuong()))
                );
            }
        }

        // Kiểm tra lại: sau khi xóa các sản phẩm hết hàng hoặc không có trong itemsData,
        // lấy lại danh sách chi tiết hóa đơn để xem còn sản phẩm nào không
        List<HoaDonChiTiet> remainingItems = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);

        // Nếu không còn sản phẩm nào -> hủy đơn luôn, không cần admin xử lý
        if (remainingItems == null || remainingItems.isEmpty()) {
            hoaDon.setTrangThaiDonHang("DA_HUY");
            hoaDon.setGhiChu("Đã hủy do không còn sản phẩm nào sau khi chỉnh sửa.");
            hoaDon.setTongTienTamTinh(BigDecimal.ZERO);
            hoaDon.setTienGiam(BigDecimal.ZERO);
            hoaDon.setTongTienThanhToan(BigDecimal.ZERO);
            hoaDonRepository.save(hoaDon);
            return convertToDTO(hoaDon);
        }

        // Tính lại tổng tiền từ các sản phẩm còn lại
        tongTienTamTinh = BigDecimal.ZERO;
        for (HoaDonChiTiet chiTiet : remainingItems) {
            if (chiTiet.getDonGia() != null && chiTiet.getSoLuong() != null) {
                tongTienTamTinh = tongTienTamTinh.add(
                        chiTiet.getDonGia().multiply(BigDecimal.valueOf(chiTiet.getSoLuong()))
                );
            }
        }

        // Cập nhật tổng tiền hóa đơn
        hoaDon.setTongTienTamTinh(tongTienTamTinh);
        // Giữ lại voucher nếu có (hoặc tính lại)
        if (hoaDon.getVoucher() != null && hoaDon.getTongTienTamTinh().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal tienGiam = tinhTienGiamVoucher(
                    hoaDon.getKhachHang() != null ? hoaDon.getKhachHang().getId() : null,
                    hoaDon.getVoucher().getId(),
                    tongTienTamTinh
            );
            hoaDon.setTienGiam(tienGiam);
            hoaDon.setTongTienThanhToan(tongTienTamTinh.subtract(tienGiam));
        } else {
            hoaDon.setTienGiam(BigDecimal.ZERO);
            hoaDon.setTongTienThanhToan(tongTienTamTinh);
        }

        hoaDon.setGhiChu("Đã chỉnh sửa số lượng.");
        
        hoaDon.setTrangThaiDonHang("CHO_XAC_NHAN");
        hoaDonRepository.save(hoaDon);

        return convertToDTO(hoaDon);
    }
}
