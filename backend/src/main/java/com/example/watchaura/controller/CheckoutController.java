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
import com.example.watchaura.service.ghn.DistrictService;
import com.example.watchaura.service.ghn.ProvinceService;
import com.example.watchaura.service.ghn.ShippingService;
import com.example.watchaura.service.ghn.WardService;
import com.example.watchaura.dto.ghn.DistrictDTO;
import com.example.watchaura.dto.ghn.ProvinceDTO;
import com.example.watchaura.dto.ghn.WardDTO;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequiresRole(value = {}, requireAuth = true)
public class CheckoutController {

    private static final Logger log = LoggerFactory.getLogger(CheckoutController.class);

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final EmailService emailService;
    private final GuestCartViewService guestCartViewService;
    private final KhuyenMaiService khuyenMaiService;
    private final SanPhamChiTietKhuyenMaiService sanPhamChiTietKhuyenMaiService;
    private final DistrictService districtService;
    private final ProvinceService provinceService;
    private final WardService wardService;
    private final ShippingService shippingService;

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
        model.addAttribute("checkoutShippingFee", BigDecimal.ZERO);
        model.addAttribute("checkoutGrandTotal", sub);
        List<ProvinceDTO> provinces = provinceService.getProvinces();
        model.addAttribute("provinces", provinces);
        model.addAttribute("title", "Thanh toán - WatchAura");
        model.addAttribute("content", "user/checkout :: content");
        return "layout/user-layout";
    }

    @GetMapping("/districts")
    @ResponseBody
    public List<DistrictDTO> districts(@RequestParam("provinceId") Integer provinceId) {
        return districtService.getDistrictsByProvince(provinceId);
    }

    @GetMapping("/wards")
    @ResponseBody
    public List<WardDTO> wards(@RequestParam("districtId") Integer districtId) {
        return wardService.getWardsByDistrict(districtId);
    }

    @PostMapping("/shipping-fee")
    @ResponseBody
    public Map<String, Object> shippingFee(
            @RequestParam("district_id") Integer districtId,
            @RequestParam("ward_code") String wardCode,
            @RequestParam(value = "subtotal", required = false) BigDecimal subtotal
    ) {
        Map<String, Object> resp = new HashMap<>();
        try {
            BigDecimal fee = shippingService.calculateShippingFee(subtotal, districtId, wardCode);
            resp.put("success", true);
            resp.put("fee", fee);
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", e.getMessage() != null ? e.getMessage() : "Không tính được phí giao hàng");
        }
        return resp;
    }

    @PostMapping("/checkout")
    public String checkoutSubmit(
            @RequestParam String tenKhachHang,
            @RequestParam String email,
            @RequestParam String sdt,
            @RequestParam String diaChi,
            @RequestParam Integer districtId,
            @RequestParam String wardCode,
            @RequestParam(required = false) String ghiChu,
            HttpSession session,
            RedirectAttributes redirect
    ) {
        Integer userId = (Integer) session.getAttribute(AuthController.SESSION_CURRENT_USER_ID);
        if (userId != null) {
            return "redirect:/thanh-toan";
        }

        Object rawCart = session.getAttribute("cart");
        Map<Integer, Integer> cart = new java.util.HashMap<>();
        if (rawCart instanceof Map<?, ?> m) {
            for (Map.Entry<?, ?> e : m.entrySet()) {
                Integer k = null;
                Integer v = null;
                if (e.getKey() instanceof Integer ik) {
                    k = ik;
                } else if (e.getKey() != null) {
                    try { k = Integer.parseInt(String.valueOf(e.getKey())); } catch (Exception ignored) {}
                }
                if (e.getValue() instanceof Integer iv) {
                    v = iv;
                } else if (e.getValue() != null) {
                    try { v = Integer.parseInt(String.valueOf(e.getValue())); } catch (Exception ignored) {}
                }
                if (k != null && v != null) {
                    cart.put(k, v);
                }
            }
        }

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

        BigDecimal phiVanChuyen;
        try {
            phiVanChuyen = shippingService.calculateShippingFee(tongTienTamTinh, districtId, wardCode);
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Không tính được phí giao hàng (GHN). Vui lòng chọn lại quận/huyện, phường/xã.");
            return "redirect:/checkout";
        }
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

        // Redirect về giỏ hàng với thông tin để hiện modal đăng ký
        redirect.addFlashAttribute("checkoutSuccess", true);
        redirect.addFlashAttribute("checkoutMaDon", hoaDon.getMaDonHang());
        redirect.addFlashAttribute("checkoutEmail", email);
        redirect.addFlashAttribute("checkoutTen", tenKhachHang);
        redirect.addFlashAttribute("checkoutSdt", sdt);
        return "redirect:/gio-hang";
    }
}
