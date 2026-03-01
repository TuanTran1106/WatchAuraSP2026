package com.example.watchaura.controller;

import com.example.watchaura.dto.CartAjaxResponse;
import com.example.watchaura.dto.GioHangDTO;
import com.example.watchaura.dto.GioHangChiTietRequest;
import com.example.watchaura.service.GioHangChiTietService;
import com.example.watchaura.service.GioHangService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/gio-hang")
@RequiredArgsConstructor
public class UserGioHangController {

    private final GioHangService gioHangService;
    private final GioHangChiTietService gioHangChiTietService;

    @GetMapping
    public String page(HttpSession session, Model model, RedirectAttributes redirect) {
        Integer userId = (Integer) session.getAttribute(AuthController.SESSION_CURRENT_USER_ID);
        if (userId == null) {
            redirect.addAttribute("error", "Vui lòng đăng nhập để xem giỏ hàng.");
            return "redirect:/dang-nhap";
        }
        GioHangDTO cart = gioHangService.getOrCreateCart(userId);
        model.addAttribute("title", "Giỏ hàng - WatchAura");
        model.addAttribute("content", "user/gio-hang :: content");
        model.addAttribute("cart", cart);
        return "layout/user-layout";
    }

    @PostMapping("/them")
    public String them(@RequestParam("sanPhamChiTietId") Integer sanPhamChiTietId,
                       @RequestParam(value = "soLuong", defaultValue = "1") Integer soLuong,
                       @RequestParam(value = "redirectUrl", required = false) String redirectUrl,
                       HttpSession session,
                       RedirectAttributes redirect) {
        Integer userId = (Integer) session.getAttribute(AuthController.SESSION_CURRENT_USER_ID);
        if (userId == null) {
            redirect.addAttribute("error", "Vui lòng đăng nhập để thêm vào giỏ hàng.");
            return "redirect:/dang-nhap";
        }
        if (soLuong == null || soLuong < 1) soLuong = 1;
        try {
            GioHangDTO cart = gioHangService.getOrCreateCart(userId);
            GioHangChiTietRequest request = new GioHangChiTietRequest();
            request.setGioHangId(cart.getId());
            request.setSanPhamChiTietId(sanPhamChiTietId);
            request.setSoLuong(soLuong);
            gioHangChiTietService.create(request);
            redirect.addFlashAttribute("success", "Đã thêm sản phẩm vào giỏ hàng.");
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage() != null ? e.getMessage() : "Không thể thêm vào giỏ hàng.");
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

    private static boolean isAjax(HttpServletRequest request) {
        return "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"));
    }
}
