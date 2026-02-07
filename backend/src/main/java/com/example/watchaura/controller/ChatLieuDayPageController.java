package com.example.watchaura.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class ChatLieuDayPageController {

    @GetMapping("/chat-lieu-day")
    public String chatLieuDayPage(Model model) {
        model.addAttribute("title", "Chất liệu dây");
        model.addAttribute("content", "admin/chat-lieu-day");
        return "layout/admin-layout";
    }
}

