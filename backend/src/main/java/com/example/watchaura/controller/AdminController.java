package com.example.watchaura.controller;

import com.example.watchaura.service.HoaDonService;
import com.example.watchaura.service.KhachHangService;
import com.example.watchaura.service.SanPhamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final KhachHangService khachHangService;
    private final HoaDonService hoaDonService;
    private final SanPhamService sanPhamService;

    /** Cấu hình in-memory (có thể thay bằng DB sau). */
    private static final Map<String, String> SETTINGS_STORE = new HashMap<>();

    @GetMapping
    public String getAdminPage(Model model) {
        model.addAttribute("title", "Dashboard");
        model.addAttribute("content", "admin/dashboard");
        try {
            model.addAttribute("totalKhachHang", khachHangService.getAll().size());
        } catch (Exception e) {
            model.addAttribute("totalKhachHang", 0);
        }
        try {
            model.addAttribute("totalHoaDon", hoaDonService.getAll().size());
        } catch (Exception e) {
            model.addAttribute("totalHoaDon", 0);
        }
        try {
            model.addAttribute("totalSanPham", sanPhamService.getAllSanPham().size());
        } catch (Exception e) {
            model.addAttribute("totalSanPham", 0);
        }
        return "layout/admin-layout";
    }

    @GetMapping("/cai-dat")
    public String getCaiDatPage(
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String error,
            Model model) {
        model.addAttribute("title", "Cài đặt");
        model.addAttribute("content", "admin/cai-dat");
        model.addAttribute("settings", SETTINGS_STORE.isEmpty() ? null : new HashMap<>(SETTINGS_STORE));
        if (message != null) model.addAttribute("message", message);
        if (error != null) model.addAttribute("error", error);
        return "layout/admin-layout";
    }

    @PostMapping("/cai-dat/thong-tin")
    public String saveThongTin(
            @RequestParam(required = false) String tenCuaHang,
            @RequestParam(required = false) String slogan,
            @RequestParam(required = false) String diaChi,
            @RequestParam(required = false) String hotline,
            @RequestParam(required = false) String email,
            RedirectAttributes redirectAttributes) {
        if (tenCuaHang != null) SETTINGS_STORE.put("site.name", tenCuaHang);
        if (slogan != null) SETTINGS_STORE.put("site.slogan", slogan);
        if (diaChi != null) SETTINGS_STORE.put("site.address", diaChi);
        if (hotline != null) SETTINGS_STORE.put("site.hotline", hotline);
        if (email != null) SETTINGS_STORE.put("site.email", email);
        redirectAttributes.addAttribute("message", "Đã lưu thông tin cửa hàng.");
        return "redirect:/admin/cai-dat";
    }

    @PostMapping("/cai-dat/cau-hinh")
    public String saveCauHinh(
            @RequestParam(required = false) String donViTienTe,
            @RequestParam(required = false) String pageSize,
            @RequestParam(required = false) String timezone,
            RedirectAttributes redirectAttributes) {
        if (donViTienTe != null) SETTINGS_STORE.put("currency", donViTienTe);
        if (pageSize != null) SETTINGS_STORE.put("page.size", pageSize);
        if (timezone != null) SETTINGS_STORE.put("timezone", timezone);
        redirectAttributes.addAttribute("message", "Đã lưu cấu hình chung.");
        return "redirect:/admin/cai-dat";
    }

    @PostMapping("/cai-dat/email")
    public String saveEmail(
            @RequestParam(required = false) String emailXacNhanDon,
            @RequestParam(required = false) String emailFrom,
            RedirectAttributes redirectAttributes) {
        if (emailXacNhanDon != null) SETTINGS_STORE.put("email.orderConfirm", emailXacNhanDon);
        if (emailFrom != null) SETTINGS_STORE.put("email.from", emailFrom);
        redirectAttributes.addAttribute("message", "Đã lưu cấu hình email.");
        return "redirect:/admin/cai-dat";
    }

    @PostMapping("/cai-dat/bao-mat")
    public String saveBaoMat(
            @RequestParam(required = false) String minPasswordLength,
            @RequestParam(required = false) String maxLoginAttempts,
            RedirectAttributes redirectAttributes) {
        if (minPasswordLength != null) SETTINGS_STORE.put("security.minPasswordLength", minPasswordLength);
        if (maxLoginAttempts != null) SETTINGS_STORE.put("security.maxLoginAttempts", maxLoginAttempts);
        redirectAttributes.addAttribute("message", "Đã lưu cấu hình bảo mật.");
        return "redirect:/admin/cai-dat";
    }
}


