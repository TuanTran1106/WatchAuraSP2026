package com.example.watchaura.controller;

import com.example.watchaura.annotation.RequiresRole;
import com.example.watchaura.dto.CartResponse;
import com.example.watchaura.dto.HoaDonChiTietDTO;
import com.example.watchaura.dto.HoaDonDTO;
import com.example.watchaura.dto.KhuyenMaiPriceResult;
import com.example.watchaura.dto.PosSerialSelectionDTO;
import com.example.watchaura.entity.*;
import com.example.watchaura.repository.*;
import com.example.watchaura.service.HoaDonService;
import com.example.watchaura.service.KhuyenMaiService;
import com.example.watchaura.service.SanPhamChiTietKhuyenMaiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/ban-hang")
@RequiresRole({"Admin", "Quản lý", "Nhân viên"})
public class BanHangTaiQuayController {

    /** Giỏ POS đang soạn — chưa chốt hóa đơn, chưa trừ kho */
    public static final String TRANG_THAI_GIO_QUAY = "DRAFT_OFFLINE";

    @Autowired
    private SanPhamChiTietRepository sanPhamChiTietRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private HoaDonChiTietRepository hoaDonChiTietRepository;
    @Autowired
    private VoucherRepository voucherRepository;
    @Autowired
    private GioHangChiTietRepository gioHangChiTietRepository;

    @Autowired
    private SanPhamChiTietKhuyenMaiService sanPhamChiTietKhuyenMaiService;

    @Autowired
    private KhuyenMaiService khuyenMaiService;

    @Autowired
    private SerialSanPhamRepository serialSanPhamRepository;

    private static String normalizeVoucherType(String raw) {
        if (raw == null) return null;
        String s = raw.trim().toUpperCase();
        if (s.isBlank()) return null;
        if (s.equals("%")) return "PERCENT";
        if (s.equals("PERCENT") || s.equals("PERCENTAGE") || s.equals("PHAN_TRAM") || s.equals("PHẦN_TRĂM") || s.equals("PT")) {
            return "PERCENT";
        }
        if (s.equals("FIXED") || s.equals("TIEN") || s.equals("TIỀN") || s.equals("MONEY") || s.equals("VND")) {
            return "FIXED";
        }
        return null;
    }



    private void addTenNhanVienToModel(HttpSession session, Model model) {
        Object userId = session.getAttribute(AuthController.SESSION_CURRENT_USER_ID);
        if (userId instanceof Integer) {
            Integer id = (Integer) userId;
            khachHangRepository.findById(id).ifPresent(kh -> {
                String ten = kh.getTenNguoiDung();
                model.addAttribute("tenNhanVien", (ten != null && !ten.isBlank()) ? ten : "Nhân viên");
            });
        }
        if (!model.containsAttribute("tenNhanVien")) {
            model.addAttribute("tenNhanVien", "Nhân viên");
        }
    }

    @GetMapping
    public String banHang(Model model, HttpSession session){

        // Bắt buộc đăng nhập
        if (session.getAttribute(AuthController.SESSION_CURRENT_USER_ID) == null) {
            String msg = URLEncoder.encode("Vui lòng đăng nhập để sử dụng chức năng bán hàng.", StandardCharsets.UTF_8);
            return "redirect:/admin/login?error=" + msg;
        }

        List<SanPhamChiTiet> sanPhamList =
                sanPhamChiTietRepository.findActiveForSaleWithDetails();

        model.addAttribute("sanPhamList", sanPhamList);
        LocalDateTime now = LocalDateTime.now();
        List<KhuyenMai> khuyenMaiDangChay = khuyenMaiService.getActivePromotions(now);
        Map<Integer, KhuyenMaiPriceResult> kmGiaMap = new HashMap<>();
        for (SanPhamChiTiet spct : sanPhamList) {
            if (spct == null || spct.getId() == null) continue;
            kmGiaMap.put(spct.getId(), resolveGiaKhuyenMai(spct, now, khuyenMaiDangChay));
        }
        model.addAttribute("kmGiaMap", kmGiaMap);

        // Luôn bắt đầu với giỏ hàng trống - không tự động load đơn cũ
        model.addAttribute("hoaDonId", 0);

        addTenNhanVienToModel(session, model);
        model.addAttribute("title", "Bán hàng tại quầy");
        model.addAttribute("content", "admin/banhang/ban-hang");
        return "layout/admin-layout";
    }




    @PostMapping("/them-san-pham")
    @ResponseBody
    @Transactional
    public String themSanPham(Integer hoaDonId, Integer sanPhamChiTietId, HttpSession session){

        boolean taoMoi = (hoaDonId == null || hoaDonId == 0);
        HoaDon hoaDon;
        if (taoMoi) {
            hoaDon = new HoaDon();
            hoaDon.setMaDonHang("HD" + System.currentTimeMillis());
            hoaDon.setTenKhachHang("Khách vãng lai");
            hoaDon.setSdtKhachHang("0000000000");
            hoaDon.setDiaChi("Mua tại cửa hàng");
            hoaDon.setLoaiHoaDon("OFFLINE");
            hoaDon.setTrangThaiDonHang(TRANG_THAI_GIO_QUAY);
            hoaDon.setPhuongThucThanhToan("TIEN_MAT");
            hoaDon.setTongTienTamTinh(BigDecimal.ZERO);
            hoaDon.setTienGiam(BigDecimal.ZERO);
            hoaDon.setTongTienThanhToan(BigDecimal.ZERO);
            hoaDon.setTrangThai(true);
            hoaDon.setNgayDat(LocalDateTime.now());


            Object userId = session.getAttribute(AuthController.SESSION_CURRENT_USER_ID);
            if (userId instanceof Integer) {
                khachHangRepository.findById((Integer) userId).ifPresent(hoaDon::setNhanVien);
            }

            hoaDon = hoaDonRepository.save(hoaDon);
            hoaDonId = hoaDon.getId();
        } else {
            hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
            // Cho phép thêm sản phẩm vào đơn đang chờ thanh toán (chưa trừ kho)
            if (!TRANG_THAI_GIO_QUAY.equals(hoaDon.getTrangThaiDonHang())
                && !"CHO_THANH_TOAN".equals(hoaDon.getTrangThaiDonHang())) {
                return "Hóa đơn đã thanh toán hoặc đã hủy. Vui lòng tạo đơn mới.";
            }
        }

        // DÙNG PESSIMISTIC LOCK - Khóa dòng sản phẩm để tránh race condition
        SanPhamChiTiet sp = sanPhamChiTietRepository.findByIdWithLock(sanPhamChiTietId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        SanPham spParent = sp.getSanPham();
        if (spParent == null || !Boolean.TRUE.equals(spParent.getTrangThai())) {
            return "Sản phẩm này đã tắt hoạt động, không thể bán tại quầy.";
        }
        if (!Boolean.TRUE.equals(sp.getTrangThai())) {
            return "Biến thể này đã tắt hoạt động, không thể bán tại quầy.";
        }

        List<HoaDonChiTiet> list = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);

        HoaDonChiTiet existing = null;

        for(HoaDonChiTiet ct:list){
            if(ct.getSanPhamChiTiet().getId().equals(sanPhamChiTietId)){
                existing = ct;
                break;
            }
        }

        // FIFO: Chỉ kiểm tra soLuongTon (ai thanh toán trước được mua)
        Integer soLuongTon = sp.getSoLuongTon() != null ? sp.getSoLuongTon() : 0;

        if(existing != null){
            // Tăng số lượng - kiểm tra tồn kho
            int newQty = existing.getSoLuong() + 1;
            if (soLuongTon < newQty) {
                return "Sản phẩm '" + sp.getSanPham().getTenSanPham() + "' chỉ còn " + soLuongTon + " sản phẩm.";
            }

            existing.setSoLuong(newQty);

            hoaDonChiTietRepository.save(existing);

        }else{
            // Thêm mới - kiểm tra tồn kho
            if (soLuongTon < 1) {
                return "Sản phẩm '" + sp.getSanPham().getTenSanPham() + "' đã hết hàng.";
            }

            HoaDonChiTiet ct = new HoaDonChiTiet();

            ct.setHoaDon(hoaDon);
            ct.setSanPhamChiTiet(sp);
            ct.setSoLuong(1);
            KhuyenMaiPriceResult giaKm = resolveGiaKhuyenMai(sp, LocalDateTime.now(), null);
            ct.setDonGia(giaKm.giaSauGiam());

            hoaDonChiTietRepository.save(ct);

        }

        updateTongTien(hoaDonId);

        // Trả về id hóa đơn mới nếu vừa tạo (để frontend cập nhật hoaDonId)
        if (taoMoi) {
            return "NEW:" + hoaDon.getId();
        }
        return "OK";
    }

    /**
     * Chốt hóa đơn (nút "Tạo hóa đơn" — tiền mặt): DRAFT_OFFLINE → CHO_THANH_TOAN, không trừ kho.
     * Trừ kho khi thanh toán QR hoặc khi đổi trạng thái sang Đã thanh toán ở Quản lý đơn hàng.
     */
    @PostMapping("/chot-hoa-don")
    @ResponseBody
    @Transactional
    public String chotHoaDon(Integer hoaDonId) {
        if (hoaDonId == null || hoaDonId == 0) {
            return "Giỏ hàng trống! Vui lòng thêm sản phẩm trước khi tạo hóa đơn.";
        }

        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        if (!"OFFLINE".equals(hoaDon.getLoaiHoaDon())) {
            return "Không phải hóa đơn bán tại quầy.";
        }

        String ts = hoaDon.getTrangThaiDonHang();
        if ("CHO_THANH_TOAN".equals(ts)) {
            return "OK";
        }
        if (!TRANG_THAI_GIO_QUAY.equals(ts)) {
            return "Không thể tạo hóa đơn ở trạng thái hiện tại.";
        }

        List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);
        if (chiTiets == null || chiTiets.isEmpty()) {
            return "Giỏ hàng trống! Vui lòng thêm sản phẩm trước khi tạo hóa đơn.";
        }

        StringBuilder loiTonKho = new StringBuilder();
        for (HoaDonChiTiet ct : chiTiets) {
            if (ct.getSanPhamChiTiet() != null) {
                SanPhamChiTiet sp = sanPhamChiTietRepository.findByIdWithLock(ct.getSanPhamChiTiet().getId())
                        .orElse(null);
                if (sp != null) {
                    Integer soLuongTon = sp.getSoLuongTon() != null ? sp.getSoLuongTon() : 0;
                    if (soLuongTon < ct.getSoLuong()) {
                        String tenSp = sp.getSanPham() != null ? sp.getSanPham().getTenSanPham() : "ID " + ct.getSanPhamChiTiet().getId();
                        loiTonKho.append(String.format("%s: cần %d, chỉ còn %d; ",
                                tenSp, ct.getSoLuong(), soLuongTon));
                    }
                }
            }
        }
        if (loiTonKho.length() > 0) {
            return "KHONG_DU_TON_KHO:" + loiTonKho;
        }

        hoaDon.setTrangThaiDonHang("CHO_THANH_TOAN");
        hoaDon.setNgayDat(LocalDateTime.now());
        hoaDonRepository.save(hoaDon);
        return "OK";
    }


    // =============================
    // LOAD GIỎ HÀNG
    // =============================

    @GetMapping("/cart/{hoaDonId}")
    @ResponseBody
    public CartResponse getCart(@PathVariable Integer hoaDonId){

        // Chưa có hóa đơn (id = 0) → trả về giỏ trống, không gọi DB
        if (hoaDonId == null || hoaDonId == 0) {
            return CartResponse.builder()
                    .items(List.of())
                    .tamTinh(BigDecimal.ZERO)
                    .giamGia(BigDecimal.ZERO)
                    .tongThanhToan(BigDecimal.ZERO)
                    .tenKhachHang("")
                    .sdtKhachHang("")
                    .build();
        }

        Optional<HoaDon> hoaDonOpt = hoaDonRepository.findById(hoaDonId);
        if (hoaDonOpt.isEmpty()) {
            return CartResponse.builder()
                    .items(List.of())
                    .tamTinh(BigDecimal.ZERO)
                    .giamGia(BigDecimal.ZERO)
                    .tongThanhToan(BigDecimal.ZERO)
                    .tenKhachHang("")
                    .sdtKhachHang("")
                    .build();
        }
        HoaDon hoaDon = hoaDonOpt.get();
        // Đơn đã thanh toán / không còn ở trạng thái giỏ POS → không coi là giỏ hàng
        String ts = hoaDon.getTrangThaiDonHang();
        if (!TRANG_THAI_GIO_QUAY.equals(ts) && !"CHO_THANH_TOAN".equals(ts)) {
            return CartResponse.builder()
                    .items(List.of())
                    .tamTinh(BigDecimal.ZERO)
                    .giamGia(BigDecimal.ZERO)
                    .tongThanhToan(BigDecimal.ZERO)
                    .tenKhachHang("")
                    .sdtKhachHang("")
                    .build();
        }

        List<HoaDonChiTiet> list = hoaDonChiTietRepository.findByHoaDonIdWithDetails(hoaDonId);

        // Convert sang DTO để tránh circular reference
        List<CartResponse.CartItemResponse> itemDTOs = list.stream()
                .map(ct -> {
                    CartResponse.MauSacInfo mauSac = null;
                    CartResponse.KichThuocInfo kichThuoc = null;

                    if (ct.getSanPhamChiTiet() != null) {
                        if (ct.getSanPhamChiTiet().getMauSac() != null) {
                            mauSac = CartResponse.MauSacInfo.builder()
                                    .id(ct.getSanPhamChiTiet().getMauSac().getId())
                                    .tenMauSac(ct.getSanPhamChiTiet().getMauSac().getTenMauSac())
                                    .build();
                        }
                        if (ct.getSanPhamChiTiet().getKichThuoc() != null) {
                            kichThuoc = CartResponse.KichThuocInfo.builder()
                                    .id(ct.getSanPhamChiTiet().getKichThuoc().getId())
                                    .tenKichThuoc(ct.getSanPhamChiTiet().getKichThuoc().getTenKichThuoc())
                                    .build();
                        }
                    }

                    return CartResponse.CartItemResponse.builder()
                            .id(ct.getId())
                            .soLuong(ct.getSoLuong())
                            .donGia(ct.getDonGia())
                            .tenSanPham(ct.getSanPhamChiTiet() != null && ct.getSanPhamChiTiet().getSanPham() != null
                                    ? ct.getSanPhamChiTiet().getSanPham().getTenSanPham() : null)
                            .hinhAnh(ct.getSanPhamChiTiet() != null && ct.getSanPhamChiTiet().getSanPham() != null
                                    ? ct.getSanPhamChiTiet().getSanPham().getHinhAnh() : null)
                            .mauSac(mauSac)
                            .kichThuoc(kichThuoc)
                            .build();
                })
                .collect(Collectors.toList());

        // Lấy serial đã gán cho từng hóa đơn chi tiết
        List<CartResponse.SerialInfo> serialInfos = new ArrayList<>();
        for (HoaDonChiTiet ct : list) {
            List<SerialSanPham> serials = serialSanPhamRepository.findByHoaDonChiTietIdOrderByIdAsc(ct.getId());
            if (!serials.isEmpty()) {
                List<String> maSerials = serials.stream()
                        .map(SerialSanPham::getMaSerial)
                        .collect(Collectors.toList());
                serialInfos.add(CartResponse.SerialInfo.builder()
                        .hoaDonChiTietId(ct.getId())
                        .maSerials(maSerials)
                        .build());
            }
        }

        return CartResponse.builder()
                .items(itemDTOs)
                .tamTinh(hoaDon.getTongTienTamTinh())
                .giamGia(hoaDon.getTienGiam())
                .tongThanhToan(hoaDon.getTongTienThanhToan())
                .tenKhachHang(hoaDon.getTenKhachHang() != null ? hoaDon.getTenKhachHang() : "")
                .sdtKhachHang(hoaDon.getSdtKhachHang() != null ? hoaDon.getSdtKhachHang() : "")
                .emailKhachHang(hoaDon.getEmail() != null ? hoaDon.getEmail() : "")
                .serials(serialInfos)
                .build();
    }

    @PostMapping("/cap-nhat-khach")
    @ResponseBody
    @Transactional
    public String capNhatKhach(Integer hoaDonId, String tenKhachHang, String sdtKhachHang, String emailKhachHang) {
        if (hoaDonId == null || hoaDonId <= 0) {
            return "Chưa có hóa đơn — hãy thêm sản phẩm trước.";
        }
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElse(null);
        if (hoaDon == null) {
            return "Không tìm thấy hóa đơn.";
        }
        if (!"OFFLINE".equals(hoaDon.getLoaiHoaDon())) {
            return "Không phải hóa đơn tại quầy.";
        }
        String ts = hoaDon.getTrangThaiDonHang();
        if (!TRANG_THAI_GIO_QUAY.equals(ts) && !"CHO_THANH_TOAN".equals(ts)) {
            return "Không thể cập nhật khách ở trạng thái hiện tại.";
        }
        if (tenKhachHang != null && !tenKhachHang.isBlank()) {
            hoaDon.setTenKhachHang(tenKhachHang.trim());
        }
        if (sdtKhachHang != null && !sdtKhachHang.isBlank()) {
            hoaDon.setSdtKhachHang(sdtKhachHang.trim().replaceAll("\\s+", ""));
        }
        if (emailKhachHang != null && !emailKhachHang.isBlank()) {
            hoaDon.setEmail(emailKhachHang.trim().toLowerCase());
        } else {
            hoaDon.setEmail(null);
        }
        hoaDonRepository.save(hoaDon);
        return "OK";
    }


    // =============================
    // CẬP NHẬT TỔNG TIỀN
    // =============================

    public void updateTongTien(Integer hoaDonId){

        List<HoaDonChiTiet> list =
                hoaDonChiTietRepository.findByHoaDonId(hoaDonId);

        BigDecimal tong = BigDecimal.ZERO;
        LocalDateTime now = LocalDateTime.now();
        List<KhuyenMai> khuyenMaiDangChay = khuyenMaiService.getActivePromotions(now);

        for(HoaDonChiTiet ct:list){
            SanPhamChiTiet spct = ct.getSanPhamChiTiet();
            if (spct != null) {
                KhuyenMaiPriceResult giaKm = resolveGiaKhuyenMai(spct, now, khuyenMaiDangChay);
                ct.setDonGia(giaKm.giaSauGiam());
                hoaDonChiTietRepository.save(ct);
            }

            BigDecimal thanhTien =
                    ct.getDonGia().multiply(
                            BigDecimal.valueOf(ct.getSoLuong())
                    );

            tong = tong.add(thanhTien);

        }

        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).get();

        hoaDon.setTongTienTamTinh(tong);
        hoaDon.setTongTienThanhToan(tong);

        hoaDonRepository.save(hoaDon);
    }

    @PostMapping("/tang-so-luong")
    @ResponseBody
    @Transactional
    public String tangSoLuong(Integer id){

        HoaDonChiTiet ct = hoaDonChiTietRepository.findById(id).get();
        String trangThai = ct.getHoaDon().getTrangThaiDonHang();
        // Cho phép tăng số lượng với cả DRAFT_OFFLINE và CHO_THANH_TOAN
        if (!TRANG_THAI_GIO_QUAY.equals(trangThai) && !"CHO_THANH_TOAN".equals(trangThai)) {
            return "HOA_DON_DA_CHOT";
        }

        // DÙNG PESSIMISTIC LOCK - Khóa dòng sản phẩm để tránh race condition
        SanPhamChiTiet sp = sanPhamChiTietRepository.findByIdWithLock(ct.getSanPhamChiTiet().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        // FIFO: Chỉ kiểm tra soLuongTon
        Integer soLuongTon = sp.getSoLuongTon() != null ? sp.getSoLuongTon() : 0;

        int newQty = (ct.getSoLuong() != null ? ct.getSoLuong() : 0) + 1;
        if (soLuongTon < newQty) {
            return "Sản phẩm '" + sp.getSanPham().getTenSanPham() + "' chỉ còn " + soLuongTon + " sản phẩm.";
        }

        ct.setSoLuong(newQty);

        hoaDonChiTietRepository.save(ct);

        updateTongTien(ct.getHoaDon().getId());

        return "OK";

    }

    @PostMapping("/giam-so-luong")
    @ResponseBody
    @Transactional
    public String giamSoLuong(Integer id){

        HoaDonChiTiet ct = hoaDonChiTietRepository.findById(id).orElse(null);
        if (ct == null) {
            return "NOT_FOUND";
        }
        String trangThai = ct.getHoaDon().getTrangThaiDonHang();
        boolean laChoThanhToan = "CHO_THANH_TOAN".equals(trangThai);
        // Cho phép giảm số lượng với cả DRAFT_OFFLINE và CHO_THANH_TOAN
        if (!TRANG_THAI_GIO_QUAY.equals(trangThai) && !laChoThanhToan) {
            return "HOA_DON_DA_CHOT";
        }

        HoaDon hoaDon = ct.getHoaDon();
        Integer hoaDonId = hoaDon.getId();
        boolean laOffline = "OFFLINE".equals(hoaDon.getLoaiHoaDon());

        if(ct.getSoLuong() > 1){

            // Nếu là hóa đơn offline và có serial đã gán, giải phóng 1 serial
            if (laOffline) {
                List<SerialSanPham> serials = serialSanPhamRepository.findByHoaDonChiTietIdOrderByIdAsc(id);
                if (!serials.isEmpty()) {
                    SerialSanPham serialToRelease = serials.get(0);
                    serialToRelease.setHoaDonChiTiet(null);
                    serialToRelease.setTrangThai(SerialSanPham.TRANG_THAI_TRONG_KHO);
                    serialToRelease.setNgayXuatKho(null);
                    serialToRelease.setNgayHetBaoHanh(null);
                    serialSanPhamRepository.save(serialToRelease);
                }
            }

            ct.setSoLuong(ct.getSoLuong() - 1);
            hoaDonChiTietRepository.save(ct);
            updateTongTien(hoaDonId);
            return "OK";

        } else {

            // Xóa hết - hoàn trả tất cả serial
            if (laOffline) {
                List<SerialSanPham> serials = serialSanPhamRepository.findByHoaDonChiTietIdOrderByIdAsc(id);
                for (SerialSanPham serial : serials) {
                    serial.setHoaDonChiTiet(null);
                    serial.setTrangThai(SerialSanPham.TRANG_THAI_TRONG_KHO);
                    serial.setNgayXuatKho(null);
                    serial.setNgayHetBaoHanh(null);
                    serialSanPhamRepository.save(serial);
                }
            }

            hoaDonChiTietRepository.delete(ct);
            List<HoaDonChiTiet> conLai = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);
            // Không xóa hóa đơn - giữ lại để có thể thêm sản phẩm lại
            updateTongTien(hoaDonId);

            if (conLai == null || conLai.isEmpty()) {
                return "CART_EMPTY";
            }

            return "OK";

        }

    }

    @PostMapping("/xoa-san-pham")
    @ResponseBody
    @Transactional
    public String xoaSanPham(Integer id){

        HoaDonChiTiet ct = hoaDonChiTietRepository.findById(id).orElse(null);
        if (ct == null) {
            return "NOT_FOUND";
        }
        String trangThai = ct.getHoaDon().getTrangThaiDonHang();
        // Cho phép xóa sản phẩm với cả DRAFT_OFFLINE và CHO_THANH_TOAN
        if (!TRANG_THAI_GIO_QUAY.equals(trangThai) && !"CHO_THANH_TOAN".equals(trangThai)) {
            return "HOA_DON_DA_CHOT";
        }

        HoaDon hoaDon = ct.getHoaDon();
        Integer hoaDonId = hoaDon.getId();

        // Hoàn trả serial vào kho (chỉ cho hóa đơn offline)
        if ("OFFLINE".equals(hoaDon.getLoaiHoaDon())) {
            List<SerialSanPham> serials = serialSanPhamRepository.findByHoaDonChiTietIdOrderByIdAsc(id);
            for (SerialSanPham serial : serials) {
                serial.setHoaDonChiTiet(null);
                serial.setTrangThai(SerialSanPham.TRANG_THAI_TRONG_KHO);
                serial.setNgayXuatKho(null);
                serial.setNgayHetBaoHanh(null);
                serialSanPhamRepository.save(serial);
            }
        }

        hoaDonChiTietRepository.delete(ct);

        List<HoaDonChiTiet> conLai = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);
        if (conLai == null || conLai.isEmpty()) {
            // Không xóa hóa đơn - giữ lại để có thể thêm sản phẩm lại
            updateTongTien(hoaDonId);
            return "CART_EMPTY";
        }

        updateTongTien(hoaDonId);

        return "OK";

    }

    @PostMapping("/xoa-tat-ca-san-pham")
    @ResponseBody
    @Transactional
    public String xoaTatCaSanPham(Integer hoaDonId) {
        if (hoaDonId == null || hoaDonId == 0) {
            return "INVALID";
        }

        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).orElse(null);
        if (hoaDon == null) {
            return "NOT_FOUND";
        }

        String trangThai = hoaDon.getTrangThaiDonHang();
        if (!TRANG_THAI_GIO_QUAY.equals(trangThai) && !"CHO_THANH_TOAN".equals(trangThai)) {
            return "HOA_DON_DA_CHOT";
        }

        hoaDonChiTietRepository.deleteByHoaDonId(hoaDonId);
        updateTongTien(hoaDonId);

        return "OK";

    }

    @PostMapping("/thanh-toan")
    @ResponseBody
    @Transactional
    public String thanhToan(Integer hoaDonId, String phuongThuc){

        if (hoaDonId == null || hoaDonId == 0) {
            return "Giỏ hàng trống! Vui lòng thêm sản phẩm vào giỏ trước khi tạo hóa đơn.";
        }

        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);
        if (chiTiets == null || chiTiets.isEmpty()) {
            return "Giỏ hàng trống! Vui lòng thêm sản phẩm vào giỏ trước khi tạo hóa đơn.";
        }

        // Bán tại quầy: thanh toán xong = đã thanh toán + trừ kho (giống khi chọn "Thanh toán" ở quản lý đơn)
        if (isPaidOfflineStatus(hoaDon.getTrangThaiDonHang())) {
            return "Đơn hàng đã được thanh toán trước đó.";
        }

        String trangThaiHt = hoaDon.getTrangThaiDonHang();
        if (!TRANG_THAI_GIO_QUAY.equals(trangThaiHt) && !"CHO_THANH_TOAN".equals(trangThaiHt)) {
            return "Hóa đơn không ở trạng thái có thể thanh toán tại quầy.";
        }

        StringBuilder loiTonKho = new StringBuilder();
        for (HoaDonChiTiet ct : chiTiets) {
            if (ct.getSanPhamChiTiet() != null) {
                SanPhamChiTiet sp = sanPhamChiTietRepository.findByIdWithLock(ct.getSanPhamChiTiet().getId())
                        .orElse(null);
                if (sp != null) {
                    Integer soLuongTon = sp.getSoLuongTon() != null ? sp.getSoLuongTon() : 0;
                    if (soLuongTon < ct.getSoLuong()) {
                        String tenSp = sp.getSanPham() != null ? sp.getSanPham().getTenSanPham() : "ID " + ct.getSanPhamChiTiet().getId();
                        loiTonKho.append(String.format("%s: cần %d, chỉ còn %d; ",
                                tenSp, ct.getSoLuong(), soLuongTon));
                    }
                }
            }
        }
        if (loiTonKho.length() > 0) {
            return "KHONG_DU_TON_KHO:" + loiTonKho;
        }
        for (HoaDonChiTiet ct : chiTiets) {
            if (ct.getSanPhamChiTiet() != null) {
                sanPhamChiTietRepository.deductStock(ct.getSanPhamChiTiet().getId(), ct.getSoLuong());
            }
        }

        // Trạng thái thanh toán là Đã thanh toán, trạng thái hóa đơn là Chờ xác nhận để admin chuyển trạng thái
        hoaDon.setTrangThaiDonHang("CHO_XAC_NHAN");
        hoaDon.setTrangThaiThanhToan("DA_THANH_TOAN");
        hoaDon.setPhuongThucThanhToan(phuongThuc != null ? phuongThuc : "TIEN_MAT");
        // Cập nhật thời điểm hoàn tất để danh sách "Quản lý đơn hàng" luôn xếp đơn mới nhất lên đầu
        hoaDon.setNgayDat(LocalDateTime.now());
        hoaDonRepository.save(hoaDon);

        return "OK";
    }

    @PostMapping("/ap-dung-voucher")
    @ResponseBody
    public String apDungVoucher(String maVoucher, Integer hoaDonId){

        if (maVoucher == null || maVoucher.isBlank()) {
            return "Vui lòng nhập mã voucher";
        }
        String code = maVoucher.trim();

        Voucher voucher = voucherRepository
                .findByMaVoucherIgnoreCase(code)
                .orElse(null);

        if(voucher == null){
            return "Voucher không tồn tại";
        }

        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).get();
        if (!TRANG_THAI_GIO_QUAY.equals(hoaDon.getTrangThaiDonHang())) {
            return "Không thể áp dụng voucher: hóa đơn đã chốt.";
        }

        // Validate voucher: trạng thái / thời gian / số lượng
        if (!Boolean.TRUE.equals(voucher.getTrangThai())) {
            return "Voucher không hợp lệ hoặc đã bị khóa";
        }
        LocalDateTime now = LocalDateTime.now();
        if (voucher.getNgayBatDau() != null && now.isBefore(voucher.getNgayBatDau())) {
            return "Voucher chưa đến thời gian sử dụng";
        }
        if (voucher.getNgayKetThuc() != null && now.isAfter(voucher.getNgayKetThuc())) {
            return "Voucher đã hết hạn";
        }
        Integer tongSo = voucher.getSoLuongTong();
        Integer daDung = voucher.getSoLuongDaDung() != null ? voucher.getSoLuongDaDung() : 0;
        if (tongSo != null && daDung >= tongSo) {
            return "Voucher đã hết lượt sử dụng";
        }

        BigDecimal tongTien = hoaDon.getTongTienTamTinh() != null ? hoaDon.getTongTienTamTinh() : BigDecimal.ZERO;

        // kiểm tra đơn tối thiểu
        if(voucher.getDonHangToiThieu() != null &&
                tongTien.compareTo(voucher.getDonHangToiThieu()) < 0){

            return "Đơn chưa đạt giá trị tối thiểu";
        }

        BigDecimal giam = BigDecimal.ZERO;

        String loai = normalizeVoucherType(voucher.getLoaiVoucher());

        // giảm theo %
        if ("PERCENT".equals(loai)) {

            giam = tongTien
                    .multiply(voucher.getGiaTri())
                    .divide(BigDecimal.valueOf(100));

            if(voucher.getGiaTriToiDa()!=null &&
                    giam.compareTo(voucher.getGiaTriToiDa()) > 0){

                giam = voucher.getGiaTriToiDa();
            }
        }

        // giảm tiền trực tiếp
        else if ("FIXED".equals(loai)) {

            giam = voucher.getGiaTri();

        }
        else {
            return "Loại voucher không hợp lệ (" + (voucher.getLoaiVoucher() != null ? voucher.getLoaiVoucher() : "null") + ")";
        }

        if (giam == null || giam.compareTo(BigDecimal.ZERO) < 0) {
            giam = BigDecimal.ZERO;
        }
        if (giam.compareTo(tongTien) > 0) {
            giam = tongTien;
        }


        hoaDon.setTienGiam(giam);
        hoaDon.setVoucher(voucher);

        hoaDon.setTongTienThanhToan(
                tongTien.subtract(giam)
        );

        hoaDonRepository.save(hoaDon);

        return "Áp dụng voucher thành công";
    }

    @GetMapping("/api/voucher-kha-dung")
    @ResponseBody
    public List<Map<String, Object>> getVoucherKhaDung() {
        // Danh sách voucher hợp lệ (trạng thái, số lượng, thời gian) — dành cho POS.
        return voucherRepository.findAllValidVouchersForPos().stream()
                .map(v -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", v.getId());
                    m.put("maVoucher", v.getMaVoucher());
                    m.put("tenVoucher", v.getTenVoucher());
                    m.put("loaiVoucher", v.getLoaiVoucher());
                    m.put("giaTri", v.getGiaTri());
                    m.put("giaTriToiDa", v.getGiaTriToiDa());
                    m.put("donHangToiThieu", v.getDonHangToiThieu());
                    m.put("ngayKetThuc", v.getNgayKetThuc());
                    return m;
                })
                .collect(Collectors.toList());
    }

    // =============================
    // TABS - LẤY DANH SÁCH ĐƠN ĐANG MỞ
    // =============================

    /** Tồn kho hiện tại các biến thể đang bán tại quầy — dùng làm mới UI sau khi trừ kho */
    @GetMapping("/api/ton-kho-pos")
    @ResponseBody
    public List<Map<String, Object>> getTonKhoPos() {
        return sanPhamChiTietRepository.findActiveForSaleWithDetails().stream()
                .filter(sp -> sp != null && sp.getId() != null)
                .map(sp -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", sp.getId());
                    m.put("soLuongTon", sp.getSoLuongTon() != null ? sp.getSoLuongTon() : 0);
                    return m;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/api/don-dang-mo")
    @ResponseBody
    public List<Map<String, Object>> getDonDangMo(HttpSession session) {
        // Bắt buộc đăng nhập
        if (session.getAttribute(AuthController.SESSION_CURRENT_USER_ID) == null) {
            return List.of();
        }

        List<HoaDon> hoaDonList = hoaDonRepository.findByLoaiHoaDon("OFFLINE");

        // Lọc các đơn đang mở: DRAFT_OFFLINE hoặc CHO_THANH_TOAN (bao gồm cả đơn trống)
        List<HoaDon> dangMo = hoaDonList.stream()
                .filter(hd -> TRANG_THAI_GIO_QUAY.equals(hd.getTrangThaiDonHang())
                           || "CHO_THANH_TOAN".equals(hd.getTrangThaiDonHang()))
                .sorted(Comparator
                        .comparing(HoaDon::getNgayDat, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(HoaDon::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        return dangMo.stream().map(hd -> {
            Map<String, Object> map = new java.util.HashMap<>();
            map.put("id", hd.getId());
            map.put("maDonHang", hd.getMaDonHang());
            map.put("trangThai", hd.getTrangThaiDonHang());
            map.put("tenTrangThai", TRANG_THAI_GIO_QUAY.equals(hd.getTrangThaiDonHang()) ? "Đang soạn" : "Chờ thanh toán");
            map.put("tongTien", hd.getTongTienThanhToan());
            map.put("soSanPham", hd.getChiTietList() != null ? hd.getChiTietList().size() : 0);
            map.put("ngayDat", hd.getNgayDat() != null ? hd.getNgayDat().toString() : "");
            return map;
        }).toList();
    }

    @GetMapping("/api/hoa-don/{id}")
    @ResponseBody
    public Map<String, Object> getHoaDonById(@PathVariable("id") Integer id, HttpSession session) {
        Map<String, Object> result = new java.util.HashMap<>();

        if (session.getAttribute(AuthController.SESSION_CURRENT_USER_ID) == null) {
            result.put("error", "Vui lòng đăng nhập");
            return result;
        }

        Optional<HoaDon> opt = hoaDonRepository.findById(id);
        if (opt.isEmpty()) {
            result.put("error", "Không tìm thấy hóa đơn");
            return result;
        }

        HoaDon hd = opt.get();
        result.put("id", hd.getId());
        result.put("maHoaDon", hd.getMaDonHang());
        result.put("ngayTao", hd.getNgayDat() != null ? hd.getNgayDat().toString() : "");
        result.put("tongTien", hd.getTongTienTamTinh() != null ? hd.getTongTienTamTinh() : BigDecimal.ZERO);
        result.put("giamGia", hd.getTienGiam() != null ? hd.getTienGiam() : BigDecimal.ZERO);
        result.put("thanhTien", hd.getTongTienThanhToan() != null ? hd.getTongTienThanhToan() : BigDecimal.ZERO);
        result.put("phuongThucThanhToan", hd.getPhuongThucThanhToan() != null ? hd.getPhuongThucThanhToan() : "");
        result.put("tenKhachHang", hd.getTenKhachHang() != null ? hd.getTenKhachHang() : "Khách vãng lai");
        result.put("sdtKhachHang", hd.getSdtKhachHang() != null ? hd.getSdtKhachHang() : "");
        result.put("emailKhachHang", hd.getEmail() != null ? hd.getEmail() : "");
        result.put("diaChiKhachHang", hd.getDiaChi() != null ? hd.getDiaChi() : "");

        if (hd.getVoucher() != null) {
            Map<String, Object> vc = new java.util.HashMap<>();
            vc.put("maVoucher", hd.getVoucher().getMaVoucher());
            vc.put("tenVoucher", hd.getVoucher().getTenVoucher());
            vc.put("loaiVoucher", hd.getVoucher().getLoaiVoucher());
            vc.put("giaTri", hd.getVoucher().getGiaTri() != null ? hd.getVoucher().getGiaTri() : BigDecimal.ZERO);
            vc.put("giaTriToiDa", hd.getVoucher().getGiaTriToiDa() != null ? hd.getVoucher().getGiaTriToiDa() : BigDecimal.ZERO);
            // Tính số tiền giảm thực tế
            BigDecimal tienGiam = BigDecimal.ZERO;
            if (hd.getTongTienTamTinh() != null && hd.getVoucher().getGiaTri() != null) {
                if ("PERCENT".equals(hd.getVoucher().getLoaiVoucher())) {
                    tienGiam = hd.getTongTienTamTinh().multiply(hd.getVoucher().getGiaTri()).divide(BigDecimal.valueOf(100));
                    BigDecimal max = hd.getVoucher().getGiaTriToiDa() != null ? hd.getVoucher().getGiaTriToiDa() : BigDecimal.valueOf(999999999);
                    if (tienGiam.compareTo(max) > 0) tienGiam = max;
                } else {
                    tienGiam = hd.getVoucher().getGiaTri();
                }
            }
            vc.put("soTienGiam", tienGiam);
            result.put("voucher", vc);
        }

        if (hd.getKhachHang() != null) {
            Map<String, Object> kh = new java.util.HashMap<>();
            kh.put("tenKhachHang", hd.getKhachHang().getTenNguoiDung());
            kh.put("soDienThoai", hd.getKhachHang().getSdt());
            kh.put("email", hd.getKhachHang().getEmail());
            result.put("khachHang", kh);
        }

        // Thông tin nhân viên bán hàng
        if (hd.getNhanVien() != null) {
            Map<String, Object> nv = new java.util.HashMap<>();
            nv.put("hoTen", hd.getNhanVien().getTenNguoiDung());
            nv.put("tenDangNhap", hd.getNhanVien().getTenNguoiDung());
            result.put("nhanVien", nv);
        }

        List<Map<String, Object>> chiTiet = new java.util.ArrayList<>();
        if (hd.getChiTietList() != null) {
            for (HoaDonChiTiet ct : hd.getChiTietList()) {
                Map<String, Object> item = new java.util.HashMap<>();
                item.put("id", ct.getId());
                item.put("soLuong", ct.getSoLuong());
                item.put("donGia", ct.getDonGia() != null ? ct.getDonGia() : BigDecimal.ZERO);

                Map<String, Object> spInfo = new java.util.HashMap<>();
                if (ct.getSanPhamChiTiet() != null) {
                    if (ct.getSanPhamChiTiet().getSanPham() != null) {
                        spInfo.put("tenSanPham", ct.getSanPhamChiTiet().getSanPham().getTenSanPham());
                        spInfo.put("maSanPham", ct.getSanPhamChiTiet().getSanPham().getMaSanPham());
                    }
                    if (ct.getSanPhamChiTiet().getMauSac() != null) {
                        spInfo.put("mauSac", ct.getSanPhamChiTiet().getMauSac().getTenMauSac());
                    }
                    if (ct.getSanPhamChiTiet().getKichThuoc() != null) {
                        spInfo.put("kichThuoc", ct.getSanPhamChiTiet().getKichThuoc().getTenKichThuoc());
                    }
                }
                item.put("sanPhamChiTiet", spInfo);
                chiTiet.add(item);
            }
        }
        result.put("chiTietHoaDonList", chiTiet);

        return result;
    }

    @PostMapping("/api/tao-don-moi")
    @ResponseBody
    public Map<String, Object> taoDonMoi(HttpSession session) {
        Map<String, Object> result = new java.util.HashMap<>();

        // Bắt buộc đăng nhập
        if (session.getAttribute(AuthController.SESSION_CURRENT_USER_ID) == null) {
            result.put("success", false);
            result.put("message", "Vui lòng đăng nhập");
            return result;
        }

        HoaDon hoaDon = new HoaDon();
        hoaDon.setMaDonHang("HD" + System.currentTimeMillis());
        hoaDon.setTenKhachHang("Khách vãng lai");
        hoaDon.setSdtKhachHang("0000000000");
        hoaDon.setDiaChi("Mua tại cửa hàng");
        hoaDon.setLoaiHoaDon("OFFLINE");
        hoaDon.setTrangThaiDonHang(TRANG_THAI_GIO_QUAY);
        hoaDon.setPhuongThucThanhToan("TIEN_MAT");
        hoaDon.setTongTienTamTinh(BigDecimal.ZERO);
        hoaDon.setTienGiam(BigDecimal.ZERO);
        hoaDon.setTongTienThanhToan(BigDecimal.ZERO);
        hoaDon.setTrangThai(true);
        hoaDon.setNgayDat(LocalDateTime.now());

        // Gán nhân viên đang đăng nhập vào hóa đơn
        Object userId = session.getAttribute(AuthController.SESSION_CURRENT_USER_ID);
        if (userId instanceof Integer) {
            khachHangRepository.findById((Integer) userId).ifPresent(hoaDon::setNhanVien);
        }

        hoaDon = hoaDonRepository.save(hoaDon);

        result.put("success", true);
        result.put("id", hoaDon.getId());
        result.put("maDonHang", hoaDon.getMaDonHang());
        result.put("trangThai", hoaDon.getTrangThaiDonHang());
        result.put("tenTrangThai", "Đang soạn");
        result.put("tongTien", hoaDon.getTongTienThanhToan());
        result.put("soSanPham", 0);
        result.put("ngayDat", hoaDon.getNgayDat().toString());
        return result;
    }

    // =============================
    // QUẢN LÝ ĐƠN HÀNG CHO NHÂN VIÊN - ĐÃ TẮT
    // =============================

    // @GetMapping("/don-hang")
    // public String quanLyDonHang(Model model, ...) { ... }

    // @PostMapping("/don-hang/cap-nhat-trang-thai")
    // @ResponseBody ... { ... }

    // @GetMapping("/don-hang/{id}")
    // @ResponseBody ... { ... }

    /** DB có thể lưu "DA THANH TOAN" hoặc "DA_THANH_TOAN" - Vẫn dùng trong thanhToan */
    private static boolean isPaidOfflineStatus(String s) {
        if (s == null || s.isBlank()) return false;
        return "DA THANH TOAN".equals(s) || "DA_THANH_TOAN".equals(s);
    }

    /** Frontend gửi DA_THANH_TOAN; lưu thống nhất "DA THANH TOAN" - Vẫn dùng trong thanhToan */
    private static String normalizeOfflineStatusParam(String trangThai) {
        if (trangThai == null) return null;
        if ("DA_THANH_TOAN".equals(trangThai)) return "DA THANH TOAN";
        return trangThai;
    }

    /**
     * Dọn dẹp các hóa đơn trống (không có sản phẩm, tổng tiền = 0)
     * Được gọi định kỳ hoặc khi load trang bán hàng
     */
    @PostMapping("/api/cleanup-empty")
    @ResponseBody
    @Transactional
    public String cleanupEmptyOrders() {
        List<HoaDon> allOffline = hoaDonRepository.findByLoaiHoaDon("OFFLINE");

        int deletedCount = 0;
        for (HoaDon hd : allOffline) {
            // Chỉ xóa đơn đang soạn, không có sản phẩm, tổng tiền = 0
            if (TRANG_THAI_GIO_QUAY.equals(hd.getTrangThaiDonHang())) {
                List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByHoaDonId(hd.getId());
                if ((chiTiets == null || chiTiets.isEmpty())
                        && (hd.getTongTienThanhToan() == null || hd.getTongTienThanhToan().compareTo(BigDecimal.ZERO) == 0)) {
                    hoaDonRepository.delete(hd);
                    deletedCount++;
                }
            }
        }

        return "Đã xóa " + deletedCount + " hóa đơn trống";
    }

    // =============================
    // SERIAL SELECTION CHO POS
    // =============================

    /**
     * API lấy dữ liệu serial cho trang bán hàng POS
     */
    @GetMapping("/api/serial/{hoaDonId}")
    @ResponseBody
    public PosSerialSelectionDTO getSerialDataForPos(@PathVariable Integer hoaDonId) {
        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));

        List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByHoaDonIdWithDetails(hoaDonId);

        List<PosSerialSelectionDTO.SerialGroupItem> groups = new ArrayList<>();

        for (HoaDonChiTiet chiTiet : chiTiets) {
            SanPhamChiTiet spct = chiTiet.getSanPhamChiTiet();
            String tenSanPham = spct.getSanPham() != null ? spct.getSanPham().getTenSanPham() : "Sản phẩm";
            String tenBienThe = buildTenBienThe(spct);
            Integer spctId = spct.getId();
            int soLuongMua = chiTiet.getSoLuong() != null ? chiTiet.getSoLuong() : 0;

            // Lấy serial trong kho (chưa bán)
            List<SerialSanPham> availableSerials = serialSanPhamRepository
                    .findBySanPhamChiTietIdAndTrangThai(spctId, SerialSanPham.TRANG_THAI_TRONG_KHO);

            // Lấy serial đã gán cho hóa đơn chi tiết này
            List<SerialSanPham> assignedSerials = serialSanPhamRepository
                    .findByHoaDonChiTietIdOrderByIdAsc(chiTiet.getId());

            Set<Integer> assignedIds = assignedSerials.stream()
                    .map(SerialSanPham::getId)
                    .collect(Collectors.toSet());

            List<PosSerialSelectionDTO.SerialItem> serialItems = new ArrayList<>();

            // Thêm serial đã gán trước
            for (SerialSanPham serial : assignedSerials) {
                serialItems.add(PosSerialSelectionDTO.SerialItem.builder()
                        .id(serial.getId())
                        .maSerial(serial.getMaSerial())
                        .daChon(true)
                        .ngayTao(serial.getNgayTao())
                        .build());
            }

            // Thêm serial trong kho
            for (SerialSanPham serial : availableSerials) {
                if (!assignedIds.contains(serial.getId())) {
                    serialItems.add(PosSerialSelectionDTO.SerialItem.builder()
                            .id(serial.getId())
                            .maSerial(serial.getMaSerial())
                            .daChon(false)
                            .ngayTao(serial.getNgayTao())
                            .build());
                }
            }

            // Sắp xếp theo mã serial
            serialItems.sort(Comparator.comparing(PosSerialSelectionDTO.SerialItem::getMaSerial));

            groups.add(PosSerialSelectionDTO.SerialGroupItem.builder()
                    .hoaDonChiTietId(chiTiet.getId())
                    .sanPhamChiTietId(spctId)
                    .tenSanPham(tenSanPham)
                    .tenBienThe(tenBienThe)
                    .soLuongMua(soLuongMua)
                    .soLuongDaChon(assignedSerials.size())
                    .serials(serialItems)
                    .build());
        }

        return PosSerialSelectionDTO.builder()
                .hoaDonId(hoaDonId)
                .maDonHang(hoaDon.getMaDonHang())
                .serialGroups(groups)
                .build();
    }

    /**
     * API gán serial cho đơn POS
     */
    @PostMapping("/api/serial/assign")
    @ResponseBody
    @Transactional
    public Map<String, Object> assignSerialsForPos(@RequestBody Map<String, Object> request) {
        Map<String, Object> result = new HashMap<>();

        try {
            Integer hoaDonId = (Integer) request.get("hoaDonId");
            @SuppressWarnings("unchecked")
            Map<String, Object> serialsByBienTheRaw = (Map<String, Object>) request.get("serialsByBienThe");

            if (hoaDonId == null) {
                result.put("success", false);
                result.put("message", "Không có thông tin hóa đơn");
                return result;
            }

            HoaDon hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElse(null);

            if (hoaDon == null) {
                result.put("success", false);
                result.put("message", "Không tìm thấy hóa đơn với ID: " + hoaDonId);
                return result;
            }

            Map<Integer, List<Integer>> serialsByBienThe = new HashMap<>();
            if (serialsByBienTheRaw != null) {
                for (Map.Entry<String, Object> entry : serialsByBienTheRaw.entrySet()) {
                    Integer hoaDonChiTietId = Integer.parseInt(entry.getKey());
                    @SuppressWarnings("unchecked")
                    List<Integer> serialIds = (List<Integer>) entry.getValue();
                    serialsByBienThe.put(hoaDonChiTietId, serialIds);
                }
            }

            // Giải phóng và gán serial
            List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);

            for (HoaDonChiTiet chiTiet : chiTiets) {
                Integer hoaDonChiTietId = chiTiet.getId();
                int soLuongMua = chiTiet.getSoLuong() != null ? chiTiet.getSoLuong() : 0;

                // Giải phóng serial cũ
                List<SerialSanPham> currentAssigned = serialSanPhamRepository
                        .findByHoaDonChiTietIdOrderByIdAsc(hoaDonChiTietId);

                for (SerialSanPham serial : currentAssigned) {
                    serial.setHoaDonChiTiet(null);
                    serial.setTrangThai(SerialSanPham.TRANG_THAI_TRONG_KHO);
                    serial.setNgayXuatKho(null);
                    serial.setNgayHetBaoHanh(null);
                }
                if (!currentAssigned.isEmpty()) {
                    serialSanPhamRepository.saveAll(currentAssigned);
                }

                // Lấy serial mới được chọn
                List<Integer> selectedSerialIds = serialsByBienThe.get(hoaDonChiTietId);
                if (selectedSerialIds == null || selectedSerialIds.isEmpty()) {
                    continue;
                }

                // Kiểm tra số lượng
                if (selectedSerialIds.size() != soLuongMua) {
                    result.put("success", false);
                    String tenSp = "Sản phẩm";
                    if (chiTiet.getSanPhamChiTiet() != null && chiTiet.getSanPhamChiTiet().getSanPham() != null) {
                        tenSp = chiTiet.getSanPhamChiTiet().getSanPham().getTenSanPham();
                    }
                    result.put("message", "Số lượng serial không khớp với số lượng mua cho: " + tenSp);
                    return result;
                }

                LocalDateTime now = LocalDateTime.now();
                for (Integer serialId : selectedSerialIds) {
                    SerialSanPham serial = serialSanPhamRepository.findById(serialId).orElse(null);
                    if (serial == null) {
                        result.put("success", false);
                        result.put("message", "Không tìm thấy serial với ID: " + serialId);
                        return result;
                    }

                    // Kiểm tra serial có trong kho không
                    if (serial.getTrangThai() != SerialSanPham.TRANG_THAI_TRONG_KHO) {
                        result.put("success", false);
                        result.put("message", "Serial " + serial.getMaSerial() + " không còn trong kho");
                        return result;
                    }

                    // Kiểm tra serial có thuộc đúng biến thể không
                    if (serial.getSanPhamChiTiet() == null || !serial.getSanPhamChiTiet().getId().equals(chiTiet.getSanPhamChiTiet().getId())) {
                        result.put("success", false);
                        result.put("message", "Serial " + serial.getMaSerial() + " không thuộc biến thể sản phẩm này");
                        return result;
                    }

                    // Gán serial cho hóa đơn chi tiết
                    serial.setHoaDonChiTiet(chiTiet);
                    // Đơn OFFLINE: chỉ đánh dấu CHỜ XÁC NHẬN, chưa trừ kho
                    // Đơn ONLINE: đánh dấu ĐÃ BÁN luôn
                    if ("OFFLINE".equals(hoaDon.getLoaiHoaDon())) {
                        serial.setTrangThai(SerialSanPham.TRANG_THAI_CHO_XAC_NHAN);
                        serial.setNgayXuatKho(null);
                        serial.setNgayHetBaoHanh(null);
                    } else {
                        serial.setTrangThai(SerialSanPham.TRANG_THAI_DA_BAN);
                        serial.setNgayXuatKho(now);
                        serial.setNgayHetBaoHanh(now.plusMonths(12));
                    }
                    serialSanPhamRepository.save(serial);
                }
            }


            serialSanPhamRepository.flush();

            result.put("success", true);
            result.put("message", "Đã gán serial thành công");
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            result.put("success", false);
            result.put("message", "Lỗi: " + e.getMessage());
            return result;
        }
    }

    private String buildTenBienThe(SanPhamChiTiet spct) {
        if (spct == null) return null;
        List<String> parts = new ArrayList<>();
        if (spct.getMauSac() != null && spct.getMauSac().getTenMauSac() != null && !spct.getMauSac().getTenMauSac().isBlank()) {
            parts.add(spct.getMauSac().getTenMauSac().trim());
        }
        if (spct.getKichThuoc() != null && spct.getKichThuoc().getTenKichThuoc() != null && !spct.getKichThuoc().getTenKichThuoc().isBlank()) {
            parts.add(spct.getKichThuoc().getTenKichThuoc().trim());
        }
        return parts.isEmpty() ? null : String.join(" · ", parts);
    }

    private KhuyenMaiPriceResult resolveGiaKhuyenMai(
            SanPhamChiTiet spct,
            LocalDateTime now,
            List<KhuyenMai> khuyenMaiDangChay) {
        if (spct == null) {
            return KhuyenMaiPriceResult.none(BigDecimal.ZERO);
        }
        LocalDateTime t = now != null ? now : LocalDateTime.now();
        return sanPhamChiTietKhuyenMaiService.resolveBestForCartOrOrderLine(spct, t, khuyenMaiDangChay);
    }

}
