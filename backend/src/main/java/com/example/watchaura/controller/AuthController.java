package com.example.watchaura.controller;

import com.example.watchaura.dto.RegisterRequest;
import com.example.watchaura.entity.KhachHang;
import com.example.watchaura.service.KhachHangService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class AuthController {

    public static final String SESSION_CURRENT_USER_ID = "currentUserId";

    private final KhachHangService khachHangService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/dang-nhap")
    public String dangNhapPage(
            @RequestParam(value = "error", required = false) String error,
            Model model) {
        model.addAttribute("title", "Đăng nhập - WatchAura");
        model.addAttribute("content", "user/dang-nhap :: content");
        if (error != null) model.addAttribute("errorMessage", error);
        return "layout/user-layout";
    }

    @PostMapping("/dang-nhap")
    public String dangNhap(
            @RequestParam("email") String email,
            @RequestParam("matKhau") String matKhau,
            HttpSession session,
            RedirectAttributes redirect) {
        if (email == null || email.isBlank()) {
            redirect.addAttribute("error", "Vui lòng nhập email.");
            return "redirect:/dang-nhap";
        }
        Optional<KhachHang> opt = khachHangService.findByEmail(email.trim());
        if (opt.isEmpty()) {
            redirect.addAttribute("error", "Email hoặc mật khẩu không đúng.");
            return "redirect:/dang-nhap";
        }
        KhachHang kh = opt.get();
        if (Boolean.FALSE.equals(kh.getTrangThai())) {
            redirect.addAttribute("error", "Tài khoản đã bị khóa.");
            return "redirect:/dang-nhap";
        }
        if (matKhau == null || !passwordEncoder.matches(matKhau, kh.getMatKhau())) {
            redirect.addAttribute("error", "Email hoặc mật khẩu không đúng.");
            return "redirect:/dang-nhap";
        }
        session.setAttribute(SESSION_CURRENT_USER_ID, kh.getId());
        redirect.addFlashAttribute("welcomeMessage", "Xin chào, " + kh.getTenNguoiDung() + "!");
        return "redirect:/";
    }

    @GetMapping("/dang-xuat")
    public String dangXuat(HttpSession session, RedirectAttributes redirect) {
        session.invalidate();
        redirect.addFlashAttribute("infoMessage", "Bạn đã đăng xuất.");
        return "redirect:/";
    }

    @GetMapping("/dang-ky")
    public String dangKyPage(Model model) {
        model.addAttribute("title", "Đăng ký - WatchAura");
        model.addAttribute("content", "user/dang-ky :: content");
        model.addAttribute("registerRequest", new RegisterRequest());
        return "layout/user-layout";
    }

    @PostMapping("/dang-ky")
    public String dangKy(
            @Valid @ModelAttribute("registerRequest") RegisterRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("title", "Đăng ký - WatchAura");
            model.addAttribute("content", "user/dang-ky :: content");
            return "layout/user-layout";
        }
        if (!request.getMatKhau().equals(request.getXacNhanMatKhau())) {
            model.addAttribute("title", "Đăng ký - WatchAura");
            model.addAttribute("content", "user/dang-ky :: content");
            model.addAttribute("passwordMismatch", "Mật khẩu và xác nhận mật khẩu không khớp.");
            return "layout/user-layout";
        }
        try {
            khachHangService.registerKhachHang(
                    request.getTenNguoiDung(),
                    request.getEmail().trim(),
                    request.getSdt(),
                    request.getMatKhau(),
                    request.getNgaySinh(),
                    request.getGioiTinh()
            );
        } catch (RuntimeException e) {
            model.addAttribute("title", "Đăng ký - WatchAura");
            model.addAttribute("content", "user/dang-ky :: content");
            model.addAttribute("registerError", e.getMessage());
            return "layout/user-layout";
        }
        redirect.addFlashAttribute("successMessage", "Đăng ký thành công. Vui lòng đăng nhập.");
        return "redirect:/dang-nhap";
    }
}
