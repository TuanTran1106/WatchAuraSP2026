package com.example.watchaura.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class LoaiMayPageController {

    @GetMapping("/loai-may")
    public String loaiMayPage(Model model) {
        model.addAttribute("title", "Loại máy");
        model.addAttribute("content", "admin/loai-may");
        return "layout/admin-layout";
    }
}

