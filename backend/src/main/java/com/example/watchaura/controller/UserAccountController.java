package com.example.watchaura.controller;

import com.example.watchaura.dto.ChangePasswordRequest;
import com.example.watchaura.service.KhachHangService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/nguoidung")
@RequiredArgsConstructor
public class UserAccountController {

    private final KhachHangService khachHangService;

    /** Trang tài khoản: thông tin cá nhân + đổi mật khẩu. Chỉ dành cho user đã đăng nhập. */
    @GetMapping
    public String page(HttpSession session, Model model, RedirectAttributes redirect) {
        Object userId = session.getAttribute(AuthController.SESSION_CURRENT_USER_ID);
        if (!(userId instanceof Integer)) {
            redirect.addFlashAttribute("infoMessage", "Vui lòng đăng nhập để xem tài khoản.");
            return "redirect:/dang-nhap";
        }
        model.addAttribute("title", "Tài khoản - WatchAura");
        model.addAttribute("content", "user/tai-khoan :: content");
        model.addAttribute("changePasswordRequest", new ChangePasswordRequest());
        model.addAttribute("passwordFormVisible", false);
        return "layout/user-layout";
    }
}
