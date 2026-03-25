package com.example.watchaura.controller;

import com.example.watchaura.config.VNPayProperties;
import com.example.watchaura.dto.GioHangDTO;
import com.example.watchaura.dto.CheckoutStockResponse;
import com.example.watchaura.dto.StockWarningItem;
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
import com.example.watchaura.service.DiaChiService;
import com.example.watchaura.entity.DiaChi;
import com.example.watchaura.entity.Voucher;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private final DiaChiService diaChiService;
    private final VNPayProperties vnPayProperties;

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

        // Kiểm tra tồn kho ngay khi vào trang thanh toán (sau khi bấm "Thanh toán" từ giỏ hàng)
        CheckoutStockResponse stockResponse = hoaDonService.checkAndAdjustStockBeforeCheckout(cart.getId(), userId);
        if (stockResponse.hasWarnings()) {
            cart = gioHangService.getOrCreateCart(userId);
            if (cart.getItems() == null || cart.getItems().isEmpty()) {
                redirect.addFlashAttribute("error", "Tất cả sản phẩm trong giỏ hàng đã hết hàng hoặc không đủ số lượng. Vui lòng kiểm tra lại giỏ hàng.");
                return "redirect:/gio-hang";
            }
            StringBuilder warningMsg = new StringBuilder();
            for (int i = 0; i < stockResponse.getWarnings().size(); i++) {
                StockWarningItem w = stockResponse.getWarnings().get(i);
                if (w.getSoLuongDuocDat() == null || w.getSoLuongDuocDat() == 0) {
                    warningMsg.append(String.format("Sản phẩm \"%s\" chỉ còn %d sản phẩm, đã xóa khỏi giỏ hàng.",
                            w.getTenSanPham(), w.getSoLuongKhaDung()));
                } else {
                    warningMsg.append(String.format("Sản phẩm \"%s\" chỉ còn %d sản phẩm, đã cập nhật lại số lượng trong giỏ.",
                            w.getTenSanPham(), w.getSoLuongKhaDung()));
                }
                if (i < stockResponse.getWarnings().size() - 1) {
                    warningMsg.append(" ");
                }
            }
            model.addAttribute("stockWarning", warningMsg.toString());
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
            form.setEmail(khachHang.getEmail() != null ? khachHang.getEmail() : "");
            form.setDiaChi(buildDiaChiFromKhachHang(userId));
            form.setPhuongThucThanhToan("COD");
            model.addAttribute("checkoutForm", form);
        }
        return "layout/user-layout";
    }

    @GetMapping("/vouchers")
    @ResponseBody
    public Map<String, Object> getAvailableVouchers(HttpSession session) {
        Map<String, Object> resp = new HashMap<>();
        Integer userId = (Integer) session.getAttribute(AuthController.SESSION_CURRENT_USER_ID);
        if (userId == null) {
            resp.put("success", false);
            resp.put("message", "Vui lòng đăng nhập.");
            return resp;
        }

        try {
            GioHangDTO cart = gioHangService.getOrCreateCart(userId);
            if (cart.getItems() == null || cart.getItems().isEmpty()) {
                resp.put("success", false);
                resp.put("message", "Giỏ hàng trống.");
                return resp;
            }

            // Lấy danh sách các danh mục trong giỏ hàng
            Set<String> danhMucSet = cart.getItems().stream()
                    .map(item -> item.getTenDanhMuc())
                    .filter(dm -> dm != null && !dm.isBlank())
                    .collect(Collectors.toSet());

            // Tính tổng tiền giỏ hàng
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

            List<Voucher> availableVouchers = new ArrayList<>();

            // Lấy voucher áp dụng cho từng danh mục
            for (String danhMuc : danhMucSet) {
                List<Voucher> vouchers = voucherService.findVouchersByDanhMuc(danhMuc);
                if (vouchers != null) {
                    availableVouchers.addAll(vouchers);
                }
            }

            // Lấy tất cả voucher chung (không giới hạn danh mục)
            List<Voucher> allVouchers = voucherService.findAllValidVouchers();
            if (allVouchers != null) {
                for (Voucher v : allVouchers) {
                    boolean exists = availableVouchers.stream()
                            .anyMatch(av -> av.getId().equals(v.getId()));
                    if (!exists) {
                        availableVouchers.add(v);
                    }
                }
            }

            // Lọc bỏ voucher trùng lặp và sắp xếp theo giá trị giảm giá
            List<Voucher> distinctVouchers = availableVouchers.stream()
                    .collect(Collectors.toMap(Voucher::getId, v -> v, (v1, v2) -> v1))
                    .values()
                    .stream()
                    .sorted((v1, v2) -> {
                        BigDecimal g1 = v1.getGiaTri() != null ? v1.getGiaTri() : BigDecimal.ZERO;
                        BigDecimal g2 = v2.getGiaTri() != null ? v2.getGiaTri() : BigDecimal.ZERO;
                        return g2.compareTo(g1);
                    })
                    .collect(Collectors.toList());

            resp.put("success", true);
            resp.put("vouchers", distinctVouchers);
            resp.put("tongTien", tongTien);
            resp.put("message", "Lấy danh sách voucher thành công.");
        } catch (Exception e) {
            resp.put("success", false);
            resp.put("message", "Lỗi: " + (e.getMessage() != null ? e.getMessage() : "Không thể lấy voucher."));
        }

        return resp;
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

        CheckoutStockResponse stockResponse = hoaDonService.checkAndAdjustStockBeforeCheckout(cart.getId(), userId);

        if (stockResponse.hasWarnings()) {
            cart = gioHangService.getOrCreateCart(userId);
            if (cart.getItems() == null || cart.getItems().isEmpty()) {
                redirect.addFlashAttribute("error", "Tất cả sản phẩm trong giỏ hàng đã hết hàng hoặc không đủ số lượng. Vui lòng kiểm tra lại giỏ hàng.");
                return "redirect:/gio-hang";
            }

            StringBuilder warningMsg = new StringBuilder();
            for (int i = 0; i < stockResponse.getWarnings().size(); i++) {
                StockWarningItem w = stockResponse.getWarnings().get(i);
                if (w.getSoLuongDuocDat() == null || w.getSoLuongDuocDat() == 0) {
                    warningMsg.append(String.format("Sản phẩm \"%s\" chỉ còn %d sản phẩm, đã xóa khỏi giỏ hàng.",
                            w.getTenSanPham(), w.getSoLuongKhaDung()));
                } else {
                    warningMsg.append(String.format("Sản phẩm \"%s\" chỉ còn %d sản phẩm, đã cập nhật lại số lượng trong giỏ.",
                            w.getTenSanPham(), w.getSoLuongKhaDung()));
                }
                if (i < stockResponse.getWarnings().size() - 1) {
                    warningMsg.append(" ");
                }
            }
            redirect.addFlashAttribute("stockWarning", warningMsg.toString());
            return "redirect:/thanh-toan";
        }

        cart = gioHangService.getOrCreateCart(userId);

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
                String voucherValue = form.getMaVoucher().trim();
                // Kiểm tra nếu là số (voucher ID mới) hoặc là mã voucher cũ
                try {
                    voucherId = Integer.parseInt(voucherValue);
                    // Nếu là số, kiểm tra voucher có tồn tại không
                    Voucher voucher = voucherService.findById(voucherId);
                    if (voucher == null) {
                        throw new RuntimeException("Voucher không tồn tại hoặc không hợp lệ.");
                    }
                } catch (NumberFormatException e) {
                    // Nếu không phải số, thử tìm theo mã voucher (backward compatibility)
                    var voucher = voucherService.findByMaVoucher(voucherValue);
                    if (voucher == null) {
                        throw new RuntimeException("Voucher không tồn tại hoặc không hợp lệ.");
                    }
                    voucherId = voucher.getId();
                }
            }

            HoaDonRequest request = new HoaDonRequest();
            request.setKhachHangId(userId);
            request.setPhuongThucThanhToan(paymentMethod);
            request.setLoaiHoaDon("ONLINE");
            request.setTenKhachHang(form.getTenKhachHang().trim());
            request.setSdtKhachHang(form.getSdtKhachHang().trim());
            request.setEmail(form.getEmail() != null ? form.getEmail().trim() : null);
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
                String returnUrl = vnPayProperties.getReturnUrl();
                // Nếu return URL là localhost, thay thế bằng base URL thực tế
                if (returnUrl.contains("localhost") || returnUrl.contains("127.0.0.1")) {
                    returnUrl = baseUrl + "/thanh-toan/vnpay/return";
                }
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

    @PostMapping("/apply-voucher-by-id")
    @ResponseBody
    public Map<String, Object> applyVoucherById(
            HttpSession session,
            @RequestParam("voucherId") Integer voucherId) {
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

        if (voucherId == null) {
            resp.put("success", false);
            resp.put("message", "Vui lòng chọn voucher.");
            return resp;
        }

        try {
            var voucher = voucherService.findById(voucherId);
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
            resp.put("voucher", voucher);
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
            Model model,
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
                // Redirect sang trang thành công với mã đơn hàng
                return "redirect:/thanh-toan/thanh-cong?maDonHang=" + java.net.URLEncoder.encode(hoaDon.getMaDonHang(), java.nio.charset.StandardCharsets.UTF_8);
            }

            // Thanh toán thất bại
            hoaDonService.updateTrangThaiDonHang(hoaDon.getId(), "DA_HUY");
            session.removeAttribute("pendingVnPayCartId");
            session.removeAttribute("pendingVnPayUserId");

            // Hiển thị trang kết quả với thông tin từ VNPay
            model.addAttribute("title", "Kết quả thanh toán - WatchAura");
            model.addAttribute("content", "user/vnpay-result :: content");
            model.addAttribute("vnpParams", params);
            model.addAttribute("maDonHang", vnpTxnRef);
            return "layout/user-layout";
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

    /** Lấy địa chỉ giao hàng từ danh sách địa chỉ của khách: ưu tiên địa chỉ mặc định, không có thì lấy đầu tiên. */
    private String buildDiaChiFromKhachHang(Integer khachHangId) {
        java.util.List<DiaChi> list = diaChiService.getByKhachHang(khachHangId);
        if (list == null || list.isEmpty()) return "";
        DiaChi dc = list.stream()
                .filter(d -> Boolean.TRUE.equals(d.getMacDinh()))
                .findFirst()
                .orElse(list.get(0));
        StringBuilder sb = new StringBuilder();
        if (dc.getDiaChiCuThe() != null && !dc.getDiaChiCuThe().isBlank()) sb.append(dc.getDiaChiCuThe());
        if (dc.getPhuongXa() != null && !dc.getPhuongXa().isBlank()) { if (sb.length() > 0) sb.append(", "); sb.append(dc.getPhuongXa()); }
        if (dc.getQuanHuyen() != null && !dc.getQuanHuyen().isBlank()) { if (sb.length() > 0) sb.append(", "); sb.append(dc.getQuanHuyen()); }
        if (dc.getTinhThanh() != null && !dc.getTinhThanh().isBlank()) { if (sb.length() > 0) sb.append(", "); sb.append(dc.getTinhThanh()); }
        return sb.toString();
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
        try {
            HoaDonDTO hoaDon = hoaDonService.getByMaDonHang(maDonHang);
            model.addAttribute("hoaDon", hoaDon);
        } catch (Exception e) {
            model.addAttribute("hoaDon", null);
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
        @NotBlank(message = "Email không được để trống")
        private String email;
        @NotBlank(message = "Địa chỉ giao hàng không được để trống")
        private String diaChi;
        private String ghiChu;
        private String phuongThucThanhToan = "COD";
        private String maVoucher;
    }
}
