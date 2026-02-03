package com.example.watchaura.controller;

import com.example.watchaura.service.HoaDonService;
import com.example.watchaura.service.KhachHangService;
import com.example.watchaura.service.SanPhamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final KhachHangService khachHangService;
    private final HoaDonService hoaDonService;
    private final SanPhamService sanPhamService;

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
}


