package com.example.watchaura.controller;

import com.example.watchaura.annotation.RequiresRole;
import com.example.watchaura.dto.CartAjaxResponse;
import com.example.watchaura.dto.GioHangDTO;
import com.example.watchaura.dto.GioHangChiTietRequest;
import com.example.watchaura.dto.KhuyenMaiPriceResult;
import com.example.watchaura.entity.KhuyenMai;
import com.example.watchaura.entity.SanPhamChiTiet;
import com.example.watchaura.repository.SanPhamChiTietRepository;
import com.example.watchaura.service.GioHangChiTietService;
import com.example.watchaura.service.GioHangService;
import com.example.watchaura.service.GuestCartViewService;
import com.example.watchaura.service.KhuyenMaiService;
import com.example.watchaura.service.SanPhamChiTietKhuyenMaiService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/gio-hang")
@RequiredArgsConstructor
public class UserGioHangController {

    private final GioHangService gioHangService;
    private final GioHangChiTietService gioHangChiTietService;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final GuestCartViewService guestCartViewService;
    private final KhuyenMaiService khuyenMaiService;
    private final SanPhamChiTietKhuyenMaiService sanPhamChiTietKhuyenMaiService;

    @GetMapping("/so-luong")
    @ResponseBody
    public ResponseEntity<java.util.Map<String, Integer>> soLuong(HttpSession session) {
        Integer userId = (Integer) session.getAttribute(AuthController.SESSION_CURRENT_USER_ID);
        int count = 0;

        if (userId != null) {
            count = gioHangService.getSoLuongGioHang(userId);
        } else {
            // Đếm số lượng trong session cart của guest
            Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("cart");
            if (cart != null) {
                count = cart.values().stream().mapToInt(Integer::intValue).sum();
            }
        }

        return ResponseEntity.ok(java.util.Map.of("soLuongGioHang", count));
    }

    @GetMapping
    public String page(HttpSession session, Model model, RedirectAttributes redirect) {
        Integer userId = (Integer) session.getAttribute(AuthController.SESSION_CURRENT_USER_ID);

        // =============================
        // ✅ CHƯA LOGIN → dùng SESSION CART
        // =============================
        if (userId == null) {
            GioHangDTO cart = guestCartViewService.buildCartDto(session);
            model.addAttribute("cart", cart);
            model.addAttribute("guestCart", true);
            model.addAttribute("content", "user/gio-hang :: content");
            return "layout/user-layout";
        }

        // =============================
        // ✅ ĐÃ LOGIN → dùng DB CART
        // =============================
        GioHangDTO cart = gioHangService.getOrCreateCart(userId);
        model.addAttribute("cart", cart);
        model.addAttribute("guestCart", false);
        model.addAttribute("content", "user/gio-hang :: content");

        return "layout/user-layout";
    }

    @PostMapping("/them")
    public Object them(@RequestParam("sanPhamChiTietId") Integer sanPhamChiTietId,
                       @RequestParam(value = "soLuong", defaultValue = "1") Integer soLuong,
                       @RequestParam(value = "redirectUrl", required = false) String redirectUrl,
                       HttpSession session,
                       HttpServletRequest request,
                       RedirectAttributes redirect) {
        Integer userId = (Integer) session.getAttribute(AuthController.SESSION_CURRENT_USER_ID);

        if (soLuong == null || soLuong < 1) {
            soLuong = 1;
        }

        // =============================
        // ✅ CHƯA LOGIN → dùng SESSION CART
        // =============================
        if (userId == null) {

            Map<Integer, Integer> cart =
                    (Map<Integer, Integer>) session.getAttribute("cart");

            if (cart == null) {
                cart = new java.util.HashMap<>();
            }

            if (sanPhamChiTietId == null) {
                if (isAjax(request)) {
                    return ResponseEntity.ok(new CartAjaxResponse(false, "Thiếu mã biến thể sản phẩm.", null, guestCartTotalSoLuong(cart), null, null, null));
                }
                redirect.addFlashAttribute("error", "Thiếu mã biến thể sản phẩm.");
                if (redirectUrl != null && !redirectUrl.isBlank() && redirectUrl.startsWith("/")) {
                    return "redirect:" + redirectUrl;
                }
                return "redirect:/gio-hang";
            }

            var spOpt = sanPhamChiTietRepository.findById(sanPhamChiTietId);
            if (spOpt.isEmpty()) {
                if (isAjax(request)) {
                    return ResponseEntity.ok(new CartAjaxResponse(false, "Không tìm thấy sản phẩm.", null, guestCartTotalSoLuong(cart), null, null, null));
                }
                redirect.addFlashAttribute("error", "Không tìm thấy sản phẩm.");
                if (redirectUrl != null && !redirectUrl.isBlank() && redirectUrl.startsWith("/")) {
                    return "redirect:" + redirectUrl;
                }
                return "redirect:/gio-hang";
            }
            SanPhamChiTiet spct = spOpt.get();
            int khaDung = spct.getSoLuongKhaDung() != null ? spct.getSoLuongKhaDung() : 0;
            if (khaDung < 1) {
                String msg = "Sản phẩm không còn hàng.";
                if (isAjax(request)) {
                    return ResponseEntity.ok(new CartAjaxResponse(false, msg, null, guestCartTotalSoLuong(cart), null, null, null));
                }
                redirect.addFlashAttribute("error", msg);
                if (redirectUrl != null && !redirectUrl.isBlank() && redirectUrl.startsWith("/")) {
                    return "redirect:" + redirectUrl;
                }
                return "redirect:/gio-hang";
            }
            int soLuongHienTai = cart.getOrDefault(sanPhamChiTietId, 0);
            int soLuongMoi = soLuongHienTai + soLuong;
            if (soLuongMoi > khaDung) {
                String msg = soLuongHienTai > 0
                        ? ("Thêm sản phẩm thất bại: đã có " + soLuongHienTai + " trong giỏ, thêm " + soLuong + " -> tổng " + soLuongMoi + " vượt quá số lượng khả dụng (" + khaDung + ").")
                        : ("Thêm sản phẩm thất bại: muốn thêm " + soLuong + " nhưng chỉ còn " + khaDung + " có thể bán.");
                if (isAjax(request)) {
                    return ResponseEntity.ok(new CartAjaxResponse(false, msg, null, guestCartTotalSoLuong(cart), null, null, null));
                }
                redirect.addFlashAttribute("error", msg);
                if (redirectUrl != null && !redirectUrl.isBlank() && redirectUrl.startsWith("/")) {
                    return "redirect:" + redirectUrl;
                }
                return "redirect:/gio-hang";
            }

            cart.put(sanPhamChiTietId, soLuongMoi);

            session.setAttribute("cart", cart);

            if (isAjax(request)) {
                int totalItems = cart.values().stream().mapToInt(Integer::intValue).sum();
                return ResponseEntity.ok(new CartAjaxResponse(true, "Đã thêm sản phẩm vào giỏ hàng.", null, totalItems, null, null, null));
            }
            redirect.addFlashAttribute("success", "Đã thêm sản phẩm vào giỏ hàng.");
            if (redirectUrl != null && !redirectUrl.isBlank() && redirectUrl.startsWith("/")) {
                return "redirect:" + redirectUrl;
            }
            return "redirect:/gio-hang";
        }

        // =============================
        // ✅ ĐÃ LOGIN → dùng DB CART
        // =============================
        if (soLuong == null || soLuong < 1) soLuong = 1;
        try {
            GioHangDTO cart = gioHangService.getOrCreateCart(userId);
            GioHangChiTietRequest req = new GioHangChiTietRequest();
            req.setGioHangId(cart.getId());
            req.setSanPhamChiTietId(sanPhamChiTietId);
            req.setSoLuong(soLuong);
            gioHangChiTietService.create(req);
            if (isAjax(request)) {
                GioHangDTO updatedCart = gioHangService.getOrCreateCart(userId);
                return ResponseEntity.ok(new CartAjaxResponse(true, "Đã thêm sản phẩm vào giỏ hàng.", updatedCart.getTongTien(), gioHangService.getSoLuongGioHang(userId), null, null, null));
            }
            redirect.addFlashAttribute("success", "Đã thêm sản phẩm vào giỏ hàng.");
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Không thể thêm vào giỏ hàng.";
            if (isAjax(request)) {
                return ResponseEntity.ok(new CartAjaxResponse(false, msg, null, 0, null, null, null));
            }
            redirect.addFlashAttribute("error", msg);
        }
        if (redirectUrl != null && !redirectUrl.isBlank() && redirectUrl.startsWith("/")) {
            return "redirect:" + redirectUrl;
        }
        return "redirect:/gio-hang";
    }

    @PostMapping("/cap-nhat/{id}")
    public Object capNhat(@PathVariable Integer id,
                          @RequestParam("soLuong") Integer soLuong,
                          HttpSession session,
                          HttpServletRequest request,
                          RedirectAttributes redirect) {
        Integer userId = (Integer) session.getAttribute(AuthController.SESSION_CURRENT_USER_ID);
        if (userId == null) {
            if (isAjax(request)) {
                return ResponseEntity.ok(new CartAjaxResponse(false, "Vui lòng đăng nhập.", null, 0, null, null, null));
            }
            redirect.addAttribute("error", "Vui lòng đăng nhập.");
            return "redirect:/dang-nhap";
        }
        try {
            // Giảm về 0 → xóa khỏi giỏ
            if (soLuong == null || soLuong < 1) {
                gioHangChiTietService.delete(id);
                if (isAjax(request)) {
                    GioHangDTO cart = gioHangService.getOrCreateCart(userId);
                    return ResponseEntity.ok(new CartAjaxResponse(true, null, cart.getTongTien(), gioHangService.getSoLuongGioHang(userId), id, null, null));
                }
                redirect.addFlashAttribute("success", "Đã xóa sản phẩm khỏi giỏ hàng.");
                return "redirect:/gio-hang";
            }
            var existing = gioHangChiTietService.getById(id);
            GioHangChiTietRequest req = new GioHangChiTietRequest();
            req.setGioHangId(existing.getGioHangId());
            req.setSanPhamChiTietId(existing.getSanPhamChiTietId());
            req.setSoLuong(soLuong);
            gioHangChiTietService.update(id, req);
            if (isAjax(request)) {
                GioHangDTO cart = gioHangService.getOrCreateCart(userId);
                var updated = gioHangChiTietService.getById(id);
                return ResponseEntity.ok(new CartAjaxResponse(true, null, cart.getTongTien(), gioHangService.getSoLuongGioHang(userId), id, updated.getSoLuong(), updated.getThanhTien()));
            }
            redirect.addFlashAttribute("success", "Đã cập nhật số lượng.");
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Không thể cập nhật.";
            if (isAjax(request)) {
                return ResponseEntity.ok(new CartAjaxResponse(false, msg, null, 0, null, null, null));
            }
            redirect.addFlashAttribute("error", msg);
        }
        return "redirect:/gio-hang";
    }

    @PostMapping("/xoa/{id}")
    public Object xoa(@PathVariable Integer id, HttpSession session, HttpServletRequest request, RedirectAttributes redirect) {
        Integer userId = (Integer) session.getAttribute(AuthController.SESSION_CURRENT_USER_ID);
        if (userId == null) {
            if (isAjax(request)) {
                return ResponseEntity.ok(new CartAjaxResponse(false, "Vui lòng đăng nhập.", null, 0, null, null, null));
            }
            redirect.addAttribute("error", "Vui lòng đăng nhập.");
            return "redirect:/dang-nhap";
        }
        try {
            gioHangChiTietService.delete(id);
            if (isAjax(request)) {
                GioHangDTO cart = gioHangService.getOrCreateCart(userId);
                return ResponseEntity.ok(new CartAjaxResponse(true, null, cart.getTongTien(), gioHangService.getSoLuongGioHang(userId), id, null, null));
            }
            redirect.addFlashAttribute("success", "Đã xóa sản phẩm khỏi giỏ hàng.");
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Không thể xóa.";
            if (isAjax(request)) {
                return ResponseEntity.ok(new CartAjaxResponse(false, msg, null, 0, null, null, null));
            }
            redirect.addFlashAttribute("error", msg);
        }
        return "redirect:/gio-hang";
    }

    private Object handleNotLogin(HttpServletRequest request,
                                  RedirectAttributes redirect,
                                  String message) {
        if (isAjax(request)) {
            return ResponseEntity.ok(
                    new CartAjaxResponse(false, message, null, 0, null, null, null)
            );
        }

        redirect.addAttribute("error", message);
        return "redirect:/dang-nhap";
    }

    private Object handleException(Exception e,
                                   HttpServletRequest request,
                                   RedirectAttributes redirect) {

        String msg = (e.getMessage() != null)
                ? e.getMessage()
                : "Có lỗi xảy ra.";

        if (isAjax(request)) {
            return ResponseEntity.ok(
                    new CartAjaxResponse(false, msg, null, 0, null, null, null)
            );
        }

        redirect.addFlashAttribute("error", msg);
        return "redirect:/gio-hang";
    }

    private boolean isAjax(HttpServletRequest request) {
        return "XMLHttpRequest".equalsIgnoreCase(
                request.getHeader("X-Requested-With")
        );
    }

    private int guestCartTotalSoLuong(Map<Integer, Integer> cart) {
        if (cart == null) {
            return 0;
        }
        return cart.values().stream().mapToInt(Integer::intValue).sum();
    }

    private BigDecimal guestCartTongTien(Map<Integer, Integer> cart) {
        if (cart == null || cart.isEmpty()) {
            return BigDecimal.ZERO;
        }
        LocalDateTime now = LocalDateTime.now();
        List<KhuyenMai> activeKm = khuyenMaiService.getActivePromotions(now);
        BigDecimal t = BigDecimal.ZERO;
        for (Map.Entry<Integer, Integer> e : cart.entrySet()) {
            if (e.getValue() == null) {
                continue;
            }
            var opt = sanPhamChiTietRepository.findByIdWithDetails(e.getKey());
            if (opt.isEmpty()) {
                continue;
            }
            SanPhamChiTiet spct = opt.get();
            KhuyenMaiPriceResult r = sanPhamChiTietKhuyenMaiService.resolveBestForCartOrOrderLine(spct, now, activeKm);
            if (r.giaSauGiam() != null) {
                t = t.add(r.giaSauGiam().multiply(BigDecimal.valueOf(e.getValue())));
            }
        }
        return t;
    }

    @PostMapping("/guest/update")
    @ResponseBody
    public ResponseEntity<CartAjaxResponse> updateGuest(
            @RequestParam Integer sanPhamChiTietId,
            @RequestParam Integer soLuong,
            HttpSession session
    ) {
        Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("cart");
        if (cart == null) {
            cart = new HashMap<>();
        }
        if (soLuong == null || soLuong <= 0) {
            cart.remove(sanPhamChiTietId);
        } else {
            cart.put(sanPhamChiTietId, soLuong);
        }
        session.setAttribute("cart", cart);

        int count = guestCartTotalSoLuong(cart);
        BigDecimal tongTien = guestCartTongTien(cart);

        var spOpt = sanPhamChiTietRepository.findByIdWithDetails(sanPhamChiTietId);
        Integer lineSoLuong = cart.get(sanPhamChiTietId);
        BigDecimal thanhTien = null;
        if (spOpt.isPresent() && lineSoLuong != null) {
            SanPhamChiTiet spct = spOpt.get();
            LocalDateTime now = LocalDateTime.now();
            List<KhuyenMai> activeKm = khuyenMaiService.getActivePromotions(now);
            KhuyenMaiPriceResult r = sanPhamChiTietKhuyenMaiService.resolveBestForCartOrOrderLine(spct, now, activeKm);
            if (r.giaSauGiam() != null) {
                thanhTien = r.giaSauGiam().multiply(BigDecimal.valueOf(lineSoLuong));
            }
        }
        return ResponseEntity.ok(new CartAjaxResponse(true, null, tongTien, count, sanPhamChiTietId, lineSoLuong, thanhTien));
    }

    @PostMapping("/guest/delete")
    @ResponseBody
    public ResponseEntity<CartAjaxResponse> deleteGuest(
            @RequestParam Integer sanPhamChiTietId,
            HttpSession session
    ) {
        Map<Integer, Integer> cart = (Map<Integer, Integer>) session.getAttribute("cart");
        if (cart != null) {
            cart.remove(sanPhamChiTietId);
        }
        if (cart != null) {
            session.setAttribute("cart", cart);
        }
        int count = guestCartTotalSoLuong(cart);
        BigDecimal tongTien = guestCartTongTien(cart);
        return ResponseEntity.ok(new CartAjaxResponse(true, null, tongTien, count, sanPhamChiTietId, null, null));
    }
}
