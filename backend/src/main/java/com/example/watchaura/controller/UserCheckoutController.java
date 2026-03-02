package com.example.watchaura.controller;

import com.example.watchaura.dto.GioHangDTO;
import com.example.watchaura.dto.HoaDonChiTietRequest;
import com.example.watchaura.dto.HoaDonDTO;
import com.example.watchaura.dto.HoaDonRequest;
import com.example.watchaura.entity.KhachHang;
import com.example.watchaura.service.GioHangChiTietService;
import com.example.watchaura.service.GioHangService;
import com.example.watchaura.service.HoaDonService;
import com.example.watchaura.service.KhachHangService;
import com.example.watchaura.service.VNPayService;
import com.example.watchaura.service.VoucherService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/thanh-toan")
@RequiredArgsConstructor
public class UserCheckoutController {

    private final GioHangService gioHangService;
    private final GioHangChiTietService gioHangChiTietService;
    private final HoaDonService hoaDonService;
    private final KhachHangService khachHangService;
    private final VNPayService vnPayService;
    private final VoucherService voucherService;

    @GetMapping
    public String page(HttpSession session, Model model, RedirectAttributes redirect) {
        Integer userId = (Integer) session.getAttribute(AuthController.SESSION_CURRENT_USER_ID);
        if (userId == null) {
            redirect.addAttribute("error", "Vui lòng đăng nhập để thanh toán.");
            return "redirect:/dang-nhap";
        }
        GioHangDTO cart = gioHangService.getOrCreateCart(userId);
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            redirect.addFlashAttribute("error", "Giỏ hàng trống. Vui lòng thêm sản phẩm trước khi thanh toán.");
            return "redirect:/gio-hang";
        }
        KhachHang khachHang = khachHangService.getById(userId);
        model.addAttribute("title", "Thanh toán - WatchAura");
        model.addAttribute("content", "user/thanh-toan :: content");
        model.addAttribute("cart", cart);
        model.addAttribute("khachHang", khachHang);
        if (!model.containsAttribute("checkoutForm")) {
            CheckoutForm form = new CheckoutForm();
            form.setTenKhachHang(khachHang.getTenNguoiDung() != null ? khachHang.getTenNguoiDung() : "");
            form.setSdtKhachHang(khachHang.getSdt() != null ? khachHang.getSdt() : "");
            form.setPhuongThucThanhToan("COD");
            model.addAttribute("checkoutForm", form);
        }
        return "layout/user-layout";
    }

    @PostMapping("/dat-hang")
    public String datHang(
            HttpSession session,
            HttpServletRequest httpRequest,
            @Valid @ModelAttribute("checkoutForm") CheckoutForm form,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {
        Integer userId = (Integer) session.getAttribute(AuthController.SESSION_CURRENT_USER_ID);
        if (userId == null) {
            redirect.addAttribute("error", "Vui lòng đăng nhập.");
            return "redirect:/dang-nhap";
        }
        GioHangDTO cart = gioHangService.getOrCreateCart(userId);
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            redirect.addFlashAttribute("error", "Giỏ hàng trống.");
            return "redirect:/gio-hang";
        }
        if (result.hasErrors()) {
            model.addAttribute("title", "Thanh toán - WatchAura");
            model.addAttribute("content", "user/thanh-toan :: content");
            model.addAttribute("cart", cart);
            model.addAttribute("khachHang", khachHangService.getById(userId));
            return "layout/user-layout";
        }

        String paymentMethod = form.getPhuongThucThanhToan() != null && !form.getPhuongThucThanhToan().isBlank()
                ? form.getPhuongThucThanhToan() : "COD";
        boolean isVnPay = "VNPAY".equalsIgnoreCase(paymentMethod);

        try {
            Integer voucherId = null;
            if (form.getMaVoucher() != null && !form.getMaVoucher().isBlank()) {
                var voucher = voucherService.findByMaVoucher(form.getMaVoucher());
                if (voucher == null) {
                    throw new RuntimeException("Voucher không tồn tại hoặc không hợp lệ.");
                }
                voucherId = voucher.getId();
            }

            HoaDonRequest request = new HoaDonRequest();
            request.setKhachHangId(userId);
            request.setPhuongThucThanhToan(paymentMethod);
            request.setLoaiHoaDon("BAN_LE");
            request.setTenKhachHang(form.getTenKhachHang().trim());
            request.setSdtKhachHang(form.getSdtKhachHang().trim());
            request.setDiaChi(form.getDiaChi().trim());
            request.setGhiChu(form.getGhiChu() != null ? form.getGhiChu().trim() : null);
            request.setVoucherId(voucherId);
            request.setNhanVienId(null);

            List<HoaDonChiTietRequest> items = cart.getItems().stream()
                    .map(item -> new HoaDonChiTietRequest(item.getSanPhamChiTietId(), item.getSoLuong()))
                    .collect(Collectors.toList());
            request.setItems(items);

            HoaDonDTO hoaDon = hoaDonService.create(request);

            if (isVnPay) {
                String baseUrl = getBaseUrl(httpRequest);
                String returnUrl = baseUrl + "/thanh-toan/vnpay/return";
                long amountVnd = hoaDon.getTongTienThanhToan() != null ? hoaDon.getTongTienThanhToan().longValue() : 0L;
                String orderInfo = "Thanh toan don hang " + hoaDon.getMaDonHang();
                String clientIp = getClientIp(httpRequest);
                String paymentUrl = vnPayService.createPaymentUrl(amountVnd, hoaDon.getMaDonHang(), orderInfo, returnUrl, clientIp, "vn");
                session.setAttribute("pendingVnPayCartId", cart.getId());
                session.setAttribute("pendingVnPayUserId", userId);
                return "redirect:" + paymentUrl;
            }

            gioHangChiTietService.deleteByGioHangId(cart.getId());
            return "redirect:/thanh-toan/thanh-cong?maDonHang=" + java.net.URLEncoder.encode(hoaDon.getMaDonHang(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage() != null ? e.getMessage() : "Không thể đặt hàng. Vui lòng thử lại.");
            redirect.addFlashAttribute("checkoutForm", form);
            return "redirect:/thanh-toan";
        }
    }

    @PostMapping("/apply-voucher")
    @ResponseBody
    public Map<String, Object> applyVoucher(
            HttpSession session,
            @RequestParam("maVoucher") String maVoucher) {
        Map<String, Object> resp = new HashMap<>();

        Integer userId = (Integer) session.getAttribute(AuthController.SESSION_CURRENT_USER_ID);
        if (userId == null) {
            resp.put("success", false);
            resp.put("message", "Vui lòng đăng nhập để áp dụng voucher.");
            return resp;
        }

        GioHangDTO cart = gioHangService.getOrCreateCart(userId);
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            resp.put("success", false);
            resp.put("message", "Giỏ hàng trống, không thể áp dụng voucher.");
            return resp;
        }

        if (maVoucher == null || maVoucher.isBlank()) {
            resp.put("success", false);
            resp.put("message", "Vui lòng nhập mã voucher.");
            return resp;
        }

        try {
            var voucher = voucherService.findByMaVoucher(maVoucher);
            if (voucher == null) {
                throw new RuntimeException("Voucher không tồn tại hoặc đã ngừng hoạt động.");
            }
            BigDecimal tongTien = BigDecimal.ZERO;
            if (cart.getItems() != null) {
                for (var item : cart.getItems()) {
                    if (item.getThanhTien() != null) {
                        tongTien = tongTien.add(item.getThanhTien());
                    } else if (item.getGiaBan() != null && item.getSoLuong() != null) {
                        tongTien = tongTien.add(item.getGiaBan().multiply(BigDecimal.valueOf(item.getSoLuong())));
                    }
                }
            }
            BigDecimal giam = hoaDonService.tinhTienGiamVoucher(userId, voucher.getId(), tongTien);
            BigDecimal thanhToan = tongTien.subtract(giam);
            if (thanhToan.compareTo(BigDecimal.ZERO) < 0) {
                thanhToan = BigDecimal.ZERO;
            }

            resp.put("success", true);
            resp.put("discount", giam);
            resp.put("finalTotal", thanhToan);
            resp.put("message", "Áp dụng voucher thành công.");
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", e.getMessage() != null ? e.getMessage() : "Không thể áp dụng voucher.");
        }

        return resp;
    }

    @GetMapping("/vnpay/return")
    public String vnpayReturn(
            HttpServletRequest request,
            HttpSession session,
            RedirectAttributes redirect) {
        Map<String, String> params = vnPayService.getReturnParams(request);
        String vnpTxnRef = params.get("vnp_TxnRef");

        if (vnpTxnRef == null || vnpTxnRef.isBlank()) {
            redirect.addFlashAttribute("error", "Mã đơn hàng không hợp lệ.");
            return "redirect:/thanh-toan";
        }

        try {
            HoaDonDTO hoaDon = hoaDonService.getByMaDonHang(vnpTxnRef);
            Integer userId = (Integer) session.getAttribute("pendingVnPayUserId");
            Integer pendingCartId = (Integer) session.getAttribute("pendingVnPayCartId");

            if (vnPayService.verifyReturn(request)) {
                hoaDonService.updateTrangThaiDonHang(hoaDon.getId(), "CHO_XAC_NHAN");
                if (pendingCartId != null && userId != null) {
                    GioHangDTO cart = gioHangService.getOrCreateCart(userId);
                    if (cart.getId().equals(pendingCartId)) {
                        gioHangChiTietService.deleteByGioHangId(cart.getId());
                    }
                    session.removeAttribute("pendingVnPayCartId");
                    session.removeAttribute("pendingVnPayUserId");
                }
                return "redirect:/thanh-toan/thanh-cong?maDonHang=" + java.net.URLEncoder.encode(hoaDon.getMaDonHang(), java.nio.charset.StandardCharsets.UTF_8);
            }

            hoaDonService.updateTrangThaiDonHang(hoaDon.getId(), "DA_HUY");
            session.removeAttribute("pendingVnPayCartId");
            session.removeAttribute("pendingVnPayUserId");
            redirect.addFlashAttribute("error", "Thanh toán VN Pay thất bại hoặc đã hủy. Đơn hàng đã được hủy. Bạn có thể đặt lại từ giỏ hàng.");
            return "redirect:/gio-hang";
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Xử lý kết quả thanh toán thất bại: " + (e.getMessage() != null ? e.getMessage() : "Vui lòng liên hệ hỗ trợ."));
            return "redirect:/thanh-toan";
        }
    }

    private static String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int port = request.getServerPort();
        String contextPath = request.getContextPath();
        StringBuilder url = new StringBuilder().append(scheme).append("://").append(serverName);
        if (("http".equals(scheme) && port != 80) || ("https".equals(scheme) && port != 443)) {
            url.append(":").append(port);
        }
        url.append(contextPath);
        return url.toString();
    }

    private static String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "127.0.0.1";
    }

    @GetMapping("/thanh-cong")
    public String thanhCong(
            @RequestParam(value = "maDonHang", required = false) String maDonHang,
            HttpSession session,
            Model model,
            RedirectAttributes redirect) {
        Integer userId = (Integer) session.getAttribute(AuthController.SESSION_CURRENT_USER_ID);
        if (userId == null) {
            return "redirect:/dang-nhap";
        }
        if (maDonHang == null || maDonHang.isBlank()) {
            redirect.addFlashAttribute("error", "Không tìm thấy thông tin đơn hàng.");
            return "redirect:/";
        }
        model.addAttribute("title", "Đặt hàng thành công - WatchAura");
        model.addAttribute("content", "user/thanh-toan-thanh-cong :: content");
        model.addAttribute("maDonHang", maDonHang);
        return "layout/user-layout";
    }

    /** Form binding cho trang thanh toán (chỉ COD). */
    @lombok.Getter
    @lombok.Setter
    public static class CheckoutForm {
        @NotBlank(message = "Họ tên người nhận không được để trống")
        private String tenKhachHang;
        @NotBlank(message = "Số điện thoại không được để trống")
        private String sdtKhachHang;
        @NotBlank(message = "Địa chỉ giao hàng không được để trống")
        private String diaChi;
        private String ghiChu;
        private String phuongThucThanhToan = "COD";
        private String maVoucher;
    }
}
