package com.example.watchaura.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class MauSacPageController {

    @GetMapping("/mau-sac")
    public String mauSacPage(Model model) {
        model.addAttribute("title", "Màu sắc");
        model.addAttribute("content", "admin/mau-sac");
        return "layout/admin-layout";
    }
}

