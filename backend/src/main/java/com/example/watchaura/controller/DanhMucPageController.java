package com.example.watchaura.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Spring MVC page controller (trả về HTML).
 * Hiển thị trong layout admin.
 */
@Controller
@RequestMapping("/admin")
public class DanhMucPageController {

    @GetMapping("/danh-muc")
    public String danhMucPage(Model model) {
        model.addAttribute("title", "Danh mục");
        model.addAttribute("content", "admin/quan-ly-danh-muc");
        return "layout/admin-layout";
    }
}

