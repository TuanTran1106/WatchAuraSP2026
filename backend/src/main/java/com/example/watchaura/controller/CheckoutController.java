package com.example.watchaura.controller;

import com.example.watchaura.annotation.RequiresRole;
import com.example.watchaura.dto.GioHangDTO;
import com.example.watchaura.dto.KhuyenMaiPriceResult;
import com.example.watchaura.entity.HoaDon;
import com.example.watchaura.entity.HoaDonChiTiet;
import com.example.watchaura.entity.KhuyenMai;
import com.example.watchaura.repository.HoaDonChiTietRepository;
import com.example.watchaura.repository.HoaDonRepository;
import com.example.watchaura.repository.SanPhamChiTietRepository;
import com.example.watchaura.service.EmailService;
import com.example.watchaura.service.GuestCartViewService;
import com.example.watchaura.service.KhuyenMaiService;
import com.example.watchaura.service.SanPhamChiTietKhuyenMaiService;
import com.example.watchaura.util.ShippingFeeUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class CheckoutController {

    private static final Logger log = LoggerFactory.getLogger(CheckoutController.class);

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final EmailService emailService;
    private final GuestCartViewService guestCartViewService;
    private final KhuyenMaiService khuyenMaiService;
    private final SanPhamChiTietKhuyenMaiService sanPhamChiTietKhuyenMaiService;

    /**
     * Thanh toán khách (không đăng nhập): form nhập thông tin + COD.
     * User đã đăng nhập dùng {@link UserCheckoutController} tại /thanh-toan.
     */
    @GetMapping("/checkout")
    public String checkoutPage(HttpSession session, Model model, RedirectAttributes redirect) {
        Integer userId = (Integer) session.getAttribute(AuthController.SESSION_CURRENT_USER_ID);
        if (userId != null) {
            return "redirect:/thanh-toan";
        }

        GioHangDTO cart = guestCartViewService.buildCartDto(session);
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            redirect.addFlashAttribute("error", "Giỏ hàng trống. Vui lòng thêm sản phẩm trước khi thanh toán.");
            return "redirect:/gio-hang";
        }

        model.addAttribute("cart", cart);
        BigDecimal sub = cart.getTongTien() != null ? cart.getTongTien() : BigDecimal.ZERO;
        model.addAttribute("checkoutShippingFee", ShippingFeeUtil.feeForMerchandiseSubtotal(sub));
        model.addAttribute("checkoutGrandTotal", sub.add(ShippingFeeUtil.feeForMerchandiseSubtotal(sub)));
        model.addAttribute("title", "Thanh toán - WatchAura");
        model.addAttribute("content", "user/checkout :: content");
        return "layout/user-layout";
    }

    @PostMapping("/checkout")
    public String checkoutSubmit(
            @RequestParam String tenKhachHang,
            @RequestParam String email,
            @RequestParam String sdt,
            @RequestParam String diaChi,
            @RequestParam(required = false) String ghiChu,
            HttpSession session,
            RedirectAttributes redirect
    ) {
        Integer userId = (Integer) session.getAttribute(AuthController.SESSION_CURRENT_USER_ID);
        if (userId != null) {
            return "redirect:/thanh-toan";
        }

        Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("cart");

        if (cart == null || cart.isEmpty()) {
            return "redirect:/gio-hang";
        }

        LocalDateTime thoiDiem = LocalDateTime.now();
        List<KhuyenMai> khuyenMaiDangChay = khuyenMaiService.getActivePromotions(thoiDiem);

        BigDecimal tongTienTamTinh = BigDecimal.ZERO;
        List<HoaDonChiTiet> chiTietList = new ArrayList<>();

        HoaDon hoaDon = new HoaDon();
        hoaDon.setMaDonHang("WA" + System.currentTimeMillis());
        hoaDon.setTenKhachHang(tenKhachHang);
        hoaDon.setEmail(email);
        hoaDon.setSdtKhachHang(sdt);
        hoaDon.setDiaChi(diaChi);
        hoaDon.setGhiChu(ghiChu);
        hoaDon.setTongTienTamTinh(BigDecimal.ZERO);
        hoaDon.setTongTienThanhToan(BigDecimal.ZERO);
        hoaDon.setTrangThaiDonHang("CHO_XAC_NHAN");
        hoaDon.setPhuongThucThanhToan("COD");
        hoaDon.setLoaiHoaDon("ONLINE");
        hoaDon.setNgayDat(thoiDiem);
        hoaDon.setTrangThai(true);

        hoaDonRepository.save(hoaDon);

        for (Map.Entry<Integer, Integer> item : cart.entrySet()) {
            var spctOpt = sanPhamChiTietRepository.findByIdWithDetails(item.getKey());
            if (spctOpt.isEmpty()) {
                continue;
            }
            var spct = spctOpt.get();
            KhuyenMaiPriceResult pr = sanPhamChiTietKhuyenMaiService.resolveBestForCartOrOrderLine(
                    spct, thoiDiem, khuyenMaiDangChay);
            BigDecimal donGia = pr.giaSauGiam() != null ? pr.giaSauGiam() : BigDecimal.ZERO;
            tongTienTamTinh = tongTienTamTinh.add(donGia.multiply(BigDecimal.valueOf(item.getValue())));

            HoaDonChiTiet ct = new HoaDonChiTiet();
            ct.setHoaDon(hoaDon);
            ct.setSanPhamChiTiet(spct);
            ct.setSoLuong(item.getValue());
            ct.setDonGia(donGia);
            hoaDonChiTietRepository.save(ct);
            chiTietList.add(ct);
        }

        BigDecimal phiVanChuyen = ShippingFeeUtil.feeForMerchandiseSubtotal(tongTienTamTinh);
        BigDecimal tongTienThanhToan = tongTienTamTinh.add(phiVanChuyen);
        hoaDon.setTongTienTamTinh(tongTienTamTinh);
        hoaDon.setTongTienThanhToan(tongTienThanhToan);
        hoaDonRepository.save(hoaDon);

        hoaDon.setChiTietList(chiTietList);

        try {
            log.info("📧 [CHECKOUT] Gọi sendOrderConfirmation, email={}, maDon={}",
                    email, hoaDon.getMaDonHang());
            emailService.sendOrderConfirmation(hoaDon);
        } catch (Exception e) {
            log.error("❌ [CHECKOUT] Lỗi gửi mail: {} | {}", e.getClass().getName(), e.getMessage());
        }

        session.removeAttribute("cart");

        // Thông báo đặt hàng thành công
        redirect.addFlashAttribute("success", "Đặt hàng thành công! Mã đơn: " + hoaDon.getMaDonHang());
        return "redirect:/gio-hang";
    }
}
