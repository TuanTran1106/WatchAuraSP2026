package com.example.watchaura.controller;

import com.example.watchaura.dto.CartResponse;
import com.example.watchaura.dto.HoaDonChiTietDTO;
import com.example.watchaura.dto.HoaDonDTO;
import com.example.watchaura.entity.*;
import com.example.watchaura.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/ban-hang")
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
            return "redirect:/dang-nhap?error=Vui lòng đăng nhập để sử dụng chức năng bán hàng.";
        }

        List<SanPhamChiTiet> sanPhamList =
                sanPhamChiTietRepository.findActiveForSaleWithDetails();

        model.addAttribute("sanPhamList", sanPhamList);


        Optional<HoaDon> existingHoaDon = hoaDonRepository
                .findFirstByLoaiHoaDonAndTrangThaiDonHangAndTrangThaiOrderByIdDesc(
                        "OFFLINE",
                        TRANG_THAI_GIO_QUAY,
                        true
                );

        if (existingHoaDon.isPresent()) {
            model.addAttribute("hoaDonId", existingHoaDon.get().getId());
        } else {

            model.addAttribute("hoaDonId", 0);
        }

        addTenNhanVienToModel(session, model);
        return "admin/banhang/ban-hang";
    }




    @PostMapping("/them-san-pham")
    @ResponseBody
    @Transactional
    public String themSanPham(Integer hoaDonId, Integer sanPhamChiTietId){


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
            hoaDon = hoaDonRepository.save(hoaDon);
            hoaDonId = hoaDon.getId();
        } else {
            hoaDon = hoaDonRepository.findById(hoaDonId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy hóa đơn"));
            if (!TRANG_THAI_GIO_QUAY.equals(hoaDon.getTrangThaiDonHang())) {
                return "Hóa đơn đã chốt hoặc đã thanh toán. Vui lòng làm mới trang để bắt đầu giỏ mới.";
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
            ct.setDonGia(sp.getGiaBan());

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
            CartResponse res = new CartResponse();
            res.setItems(List.of());
            res.setTamTinh(BigDecimal.ZERO);
            res.setGiamGia(BigDecimal.ZERO);
            res.setTongThanhToan(BigDecimal.ZERO);
            return res;
        }

        List<HoaDonChiTiet> list =
                hoaDonChiTietRepository.findByHoaDonIdWithDetails(hoaDonId);

        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).get();

        CartResponse res = new CartResponse();

        res.setItems(list);
        res.setTamTinh(hoaDon.getTongTienTamTinh());
        res.setGiamGia(hoaDon.getTienGiam());
        res.setTongThanhToan(hoaDon.getTongTienThanhToan());

        return res;
    }


    // =============================
    // CẬP NHẬT TỔNG TIỀN
    // =============================

    public void updateTongTien(Integer hoaDonId){

        List<HoaDonChiTiet> list =
                hoaDonChiTietRepository.findByHoaDonId(hoaDonId);

        BigDecimal tong = BigDecimal.ZERO;

        for(HoaDonChiTiet ct:list){

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
        if (!TRANG_THAI_GIO_QUAY.equals(ct.getHoaDon().getTrangThaiDonHang())) {
            return "HOA_DON_DA_CHOT";
        }

        // DÙNG PESSIMISTIC LOCK - Khóa dòng sản phẩm để tránh race condition
        SanPhamChiTiet sp = sanPhamChiTietRepository.findByIdWithLock(ct.getSanPhamChiTiet().getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        // FIFO: Chỉ kiểm tra soLuongTon
        Integer soLuongTon = sp.getSoLuongTon() != null ? sp.getSoLuongTon() : 0;

        if (soLuongTon < 1) {
            return "Sản phẩm '" + sp.getSanPham().getTenSanPham() + "' đã hết hàng.";
        }

        ct.setSoLuong(ct.getSoLuong() + 1);

        hoaDonChiTietRepository.save(ct);

        updateTongTien(ct.getHoaDon().getId());

        return "OK";

    }

    @PostMapping("/giam-so-luong")
    @ResponseBody
    @Transactional
    public String giamSoLuong(Integer id){

        HoaDonChiTiet ct = hoaDonChiTietRepository.findById(id).get();
        if (!TRANG_THAI_GIO_QUAY.equals(ct.getHoaDon().getTrangThaiDonHang())) {
            return "HOA_DON_DA_CHOT";
        }

        HoaDon hoaDon = ct.getHoaDon();
        Integer hoaDonId = hoaDon.getId();

        if(ct.getSoLuong() > 1){

            ct.setSoLuong(ct.getSoLuong() - 1);

            hoaDonChiTietRepository.save(ct);

            updateTongTien(hoaDonId);

            return "OK";

        } else {

            hoaDonChiTietRepository.delete(ct);

            List<HoaDonChiTiet> conLai = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);
            if (conLai == null || conLai.isEmpty()) {
                hoaDonRepository.delete(hoaDon);
                return "CART_EMPTY";
            }

            updateTongTien(hoaDonId);

            return "OK";

        }

    }

    @PostMapping("/xoa-san-pham")
    @ResponseBody
    @Transactional
    public String xoaSanPham(Integer id){

        HoaDonChiTiet ct = hoaDonChiTietRepository.findById(id).get();
        if (!TRANG_THAI_GIO_QUAY.equals(ct.getHoaDon().getTrangThaiDonHang())) {
            return "HOA_DON_DA_CHOT";
        }

        HoaDon hoaDon = ct.getHoaDon();
        Integer hoaDonId = hoaDon.getId();

        hoaDonChiTietRepository.delete(ct);


        List<HoaDonChiTiet> conLai = hoaDonChiTietRepository.findByHoaDonId(hoaDonId);
        if (conLai == null || conLai.isEmpty()) {
            hoaDonRepository.delete(hoaDon);
            return "CART_EMPTY";
        }

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

        // Chuẩn hóa trạng thái đã thanh toán (UI / DB dùng "DA THANH TOAN")
        hoaDon.setTrangThaiDonHang("DA THANH TOAN");
        hoaDon.setPhuongThucThanhToan(phuongThuc != null ? phuongThuc : "TIEN_MAT");
        // Cập nhật thời điểm hoàn tất để danh sách "Quản lý đơn hàng" luôn xếp đơn mới nhất lên đầu
        hoaDon.setNgayDat(LocalDateTime.now());
        hoaDonRepository.save(hoaDon);

        return "OK";
    }

    @PostMapping("/ap-dung-voucher")
    @ResponseBody
    public String apDungVoucher(String maVoucher, Integer hoaDonId){

        Voucher voucher = voucherRepository
                .findByMaVoucherIgnoreCase(maVoucher)
                .orElse(null);

        if(voucher == null){
            return "Voucher không tồn tại";
        }

        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).get();
        if (!TRANG_THAI_GIO_QUAY.equals(hoaDon.getTrangThaiDonHang())) {
            return "Không thể áp dụng voucher: hóa đơn đã chốt.";
        }

        BigDecimal tongTien = hoaDon.getTongTienTamTinh();

        // kiểm tra đơn tối thiểu
        if(voucher.getDonHangToiThieu() != null &&
                tongTien.compareTo(voucher.getDonHangToiThieu()) < 0){

            return "Đơn chưa đạt giá trị tối thiểu";
        }

        BigDecimal giam = BigDecimal.ZERO;

// giảm theo %
        if(voucher.getLoaiVoucher().equalsIgnoreCase("PERCENT")){

            giam = tongTien
                    .multiply(voucher.getGiaTri())
                    .divide(BigDecimal.valueOf(100));

            if(voucher.getGiaTriToiDa()!=null &&
                    giam.compareTo(voucher.getGiaTriToiDa()) > 0){

                giam = voucher.getGiaTriToiDa();
            }
        }

// giảm tiền trực tiếp
        else if(voucher.getLoaiVoucher().equalsIgnoreCase("FIXED")){

            giam = voucher.getGiaTri();

        }


        hoaDon.setTienGiam(giam);

        hoaDon.setTongTienThanhToan(
                tongTien.subtract(giam)
        );

        hoaDonRepository.save(hoaDon);

        return "Áp dụng voucher thành công";
    }

    // =============================
    // QUẢN LÝ ĐƠN HÀNG CHO NHÂN VIÊN
    // =============================

    @GetMapping("/don-hang")
    public String quanLyDonHang(Model model,
                                  HttpSession session,
                                  @RequestParam(required = false) String q,
                                  @RequestParam(required = false) String trangThai,
                                  @RequestParam(defaultValue = "0") int page) {

        // Bắt buộc đăng nhập
        if (session.getAttribute(AuthController.SESSION_CURRENT_USER_ID) == null) {
            return "redirect:/dang-nhap?error=Vui lòng đăng nhập để xem đơn hàng.";
        }

        // Lấy danh sách hóa đơn bán OFFLINE cho nhân viên
        List<HoaDon> hoaDonList = hoaDonRepository.findByLoaiHoaDon("OFFLINE");

        // Không hiển thị giỏ đang soạn (DRAFT) và hóa đơn 0đ
        hoaDonList = hoaDonList.stream()
                .filter(hd -> !TRANG_THAI_GIO_QUAY.equals(hd.getTrangThaiDonHang()))
                .filter(hd -> hd.getTongTienThanhToan() != null && hd.getTongTienThanhToan().compareTo(BigDecimal.ZERO) > 0)
                .toList();

        // Lọc theo từ khóa nếu có
        if (q != null && !q.isBlank()) {
            final String searchKey = q.toLowerCase();
            hoaDonList = hoaDonList.stream()
                    .filter(hd -> (hd.getMaDonHang() != null && hd.getMaDonHang().toLowerCase().contains(searchKey)) ||
                            (hd.getTenKhachHang() != null && hd.getTenKhachHang().toLowerCase().contains(searchKey)) ||
                            (hd.getSdtKhachHang() != null && hd.getSdtKhachHang().contains(q)))
                    .toList();
        }

        // Lọc theo trạng thái nếu có (hỗ trợ cả giá trị cũ trong DB)
        if (trangThai != null && !trangThai.isBlank()) {
            final String fTrangThai = trangThai;
            hoaDonList = hoaDonList.stream()
                    .filter(hd -> {
                        String ts = hd.getTrangThaiDonHang();
                        if (ts == null) return false;
                        if (ts.equals(fTrangThai)) return true;
                        if ("CHO_THANH_TOAN".equals(fTrangThai)) {
                            return "CHỜ THANH TOAN".equals(ts) || "CHO_XAC_NHAN".equals(ts) || "CHO XAC NHAN".equals(ts);
                        }
                        if ("DA_THANH_TOAN".equals(fTrangThai)) {
                            return "DA THANH TOAN".equals(ts) || isPaidOfflineStatus(ts);
                        }
                        return false;
                    })
                    .toList();
        }

        // Mới nhất trước: theo ngày (null xuống cuối), cùng giây thì theo ID
        hoaDonList = hoaDonList.stream()
                .sorted(Comparator
                        .comparing(HoaDon::getNgayDat, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(HoaDon::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        model.addAttribute("hoaDonList", hoaDonList);
        model.addAttribute("searchKeyword", q != null ? q : "");
        model.addAttribute("filterTrangThai", trangThai != null ? trangThai : "");

        addTenNhanVienToModel(session, model);
        return "admin/banhang/don-hang";
    }

    // Cập nhật trạng thái đơn hàng
    @PostMapping("/don-hang/cap-nhat-trang-thai")
    @ResponseBody
    @Transactional
    public String capNhatTrangThai(@RequestParam Integer id, @RequestParam String trangThai) {
        HoaDon hoaDon = hoaDonRepository.findById(id).orElse(null);
        if (hoaDon == null) {
            return "Không tìm thấy đơn hàng";
        }

        String trangThaiHienTai = hoaDon.getTrangThaiDonHang();
        String trangThaiMoi = normalizeOfflineStatusParam(trangThai);

        // Đã thanh toán: không đổi sang trạng thái khác (kể cả về chờ thanh toán)
        if (isPaidOfflineStatus(trangThaiHienTai)) {
            if (!"DA THANH TOAN".equals(trangThaiMoi)) {
                return "Không thể đổi trạng thái sau khi đã thanh toán";
            }
            return "OK";
        }

        // Đã hủy: không đổi sang trạng thái khác
        if ("DA_HUY".equals(trangThaiHienTai)) {
            if (!"DA_HUY".equals(trangThaiMoi)) {
                return "Đơn hàng đã hủy không thể thay đổi";
            }
            return "OK";
        }

        // Chờ thanh toán (hoặc tương đương): chỉ sang Thanh toán hoặc Đã hủy; giữ chờ chỉ để đồng bộ/no-op
        if (!"DA THANH TOAN".equals(trangThaiMoi) && !"DA_HUY".equals(trangThaiMoi)) {
            if ("CHO_THANH_TOAN".equals(trangThaiMoi) || "CHO_XAC_NHAN".equals(trangThaiMoi)) {
                hoaDon.setTrangThaiDonHang("CHO_THANH_TOAN");
                hoaDonRepository.save(hoaDon);
                return "OK";
            }
            return "Chỉ có thể chuyển từ chờ thanh toán sang Thanh toán hoặc Đã hủy";
        }

        List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByHoaDonId(hoaDon.getId());

        // Chuyển sang đã thanh toán: chỉ trừ kho khi trước đó chưa thanh toán (tránh trừ 2 lần)
        if ("DA THANH TOAN".equals(trangThaiMoi)) {
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
                String lyDoHetHang = loiTonKho.toString();
                hoaDon.setGhiChu("Không đủ tồn kho: " + lyDoHetHang + " (Chờ xử lý)");
                hoaDonRepository.save(hoaDon);
                return "KHONG_DU_TON_KHO:" + lyDoHetHang;
            }

            for (HoaDonChiTiet ct : chiTiets) {
                if (ct.getSanPhamChiTiet() != null) {
                    sanPhamChiTietRepository.deductStock(ct.getSanPhamChiTiet().getId(), ct.getSoLuong());
                }
            }
            hoaDon.setNgayDat(LocalDateTime.now());
            hoaDon.setTrangThaiDonHang("DA THANH TOAN");
        } else if ("DA_HUY".equals(trangThaiMoi)) {
            hoaDon.setTrangThaiDonHang("DA_HUY");
        }

        hoaDonRepository.save(hoaDon);
        return "OK";
    }

    /** DB có thể lưu "DA THANH TOAN" hoặc "DA_THANH_TOAN" */
    private static boolean isPaidOfflineStatus(String s) {
        if (s == null || s.isBlank()) return false;
        return "DA THANH TOAN".equals(s) || "DA_THANH_TOAN".equals(s);
    }

    /** Frontend gửi DA_THANH_TOAN; lưu thống nhất "DA THANH TOAN" */
    private static String normalizeOfflineStatusParam(String trangThai) {
        if (trangThai == null) return null;
        if ("DA_THANH_TOAN".equals(trangThai)) return "DA THANH TOAN";
        return trangThai;
    }

    // Xem chi tiết đơn hàng
    @GetMapping("/don-hang/{id}")
    @ResponseBody
    public ResponseEntity<?> getDonHangChiTiet(@PathVariable Integer id) {
        return hoaDonRepository.findById(id)
                .map(hoaDon -> {
                    HoaDonDTO dto = new HoaDonDTO();
                    dto.setId(hoaDon.getId());
                    dto.setMaDonHang(hoaDon.getMaDonHang());
                    dto.setTenKhachHang(hoaDon.getTenKhachHang());
                    dto.setSdtKhachHang(hoaDon.getSdtKhachHang());
                    dto.setDiaChi(hoaDon.getDiaChi());
                    dto.setNgayDat(hoaDon.getNgayDat());
                    dto.setPhuongThucThanhToan(hoaDon.getPhuongThucThanhToan());
                    dto.setLoaiHoaDon(hoaDon.getLoaiHoaDon());
                    dto.setTrangThaiDonHang(hoaDon.getTrangThaiDonHang());
                    dto.setTongTienTamTinh(hoaDon.getTongTienTamTinh());
                    dto.setTienGiam(hoaDon.getTienGiam());
                    dto.setTongTienThanhToan(hoaDon.getTongTienThanhToan());
                    dto.setGhiChu(hoaDon.getGhiChu());
                    
                    if (hoaDon.getChiTietList() != null) {
                        List<HoaDonChiTietDTO> items = hoaDon.getChiTietList().stream()
                                .map(ct -> {
                                    HoaDonChiTietDTO item = new HoaDonChiTietDTO();
                                    item.setId(ct.getId());
                                    item.setSoLuong(ct.getSoLuong());
                                    item.setDonGia(ct.getDonGia());
                                    item.setHoaDonId(hoaDon.getId());
                                    
                                    if (ct.getSanPhamChiTiet() != null) {
                                        item.setSanPhamChiTietId(ct.getSanPhamChiTiet().getId());
                                        if (ct.getSanPhamChiTiet().getSanPham() != null) {
                                            item.setTenSanPham(ct.getSanPhamChiTiet().getSanPham().getTenSanPham());
                                            item.setHinhAnh(ct.getSanPhamChiTiet().getSanPham().getHinhAnh());
                                        }
                                        // Build variant name
                                        String bienThe = "";
                                        if (ct.getSanPhamChiTiet().getMauSac() != null) {
                                            bienThe += ct.getSanPhamChiTiet().getMauSac().getTenMauSac();
                                        }
                                        if (ct.getSanPhamChiTiet().getKichThuoc() != null) {
                                            bienThe += (bienThe.isEmpty() ? "" : " - ") + ct.getSanPhamChiTiet().getKichThuoc().getTenKichThuoc();
                                        }
                                        item.setTenBienThe(bienThe);
                                    }
                                    return item;
                                })
                                .toList();
                        dto.setItems(items);
                    }
                    
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }

}
