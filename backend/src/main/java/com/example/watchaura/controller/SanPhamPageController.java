package com.example.watchaura.controller;

import com.example.watchaura.annotation.RequiresRole;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@RequiresRole({"Admin", "Quản lý"})
public class SanPhamPageController {

    @GetMapping("/san-pham")
    public String sanPhamPage(Model model) {
        model.addAttribute("title", "Sản phẩm");
        model.addAttribute("content", "admin/san-pham");
        return "layout/admin-layout";
    }
}
