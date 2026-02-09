package com.example.watchaura.config;

import com.example.watchaura.controller.AuthController;
import com.example.watchaura.entity.KhachHang;
import com.example.watchaura.service.GioHangService;
import com.example.watchaura.service.KhachHangService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final KhachHangService khachHangService;
    private final GioHangService gioHangService;

    @ModelAttribute
    public void addCurrentUser(HttpSession session, Model model) {
        Object userId = session.getAttribute(AuthController.SESSION_CURRENT_USER_ID);
        if (userId instanceof Integer) {
            try {
                KhachHang kh = khachHangService.getById((Integer) userId);
                model.addAttribute("currentUser", kh);
                model.addAttribute("soLuongGioHang", gioHangService.getSoLuongGioHang((Integer) userId));
            } catch (Exception ignored) {
                session.removeAttribute(AuthController.SESSION_CURRENT_USER_ID);
                model.addAttribute("currentUser", null);
                model.addAttribute("soLuongGioHang", 0);
            }
        } else {
            model.addAttribute("currentUser", null);
            model.addAttribute("soLuongGioHang", 0);
        }
    }
}
