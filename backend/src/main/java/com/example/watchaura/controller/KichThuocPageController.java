package com.example.watchaura.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class KichThuocPageController {

    @GetMapping("/kich-thuoc")
    public String kichThuocPage(Model model) {
        model.addAttribute("title", "Kích thước");
        model.addAttribute("content", "admin/kich-thuoc");
        return "layout/admin-layout";
    }
}

