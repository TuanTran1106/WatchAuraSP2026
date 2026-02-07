package com.example.watchaura.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class ThuongHieuPageController {

    @GetMapping("/thuong-hieu")
    public String thuongHieuPage(Model model) {
        model.addAttribute("title", "Thương hiệu");
        model.addAttribute("content", "admin/thuong-hieu");
        return "layout/admin-layout";
    }
}

