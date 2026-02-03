package com.example.watchaura.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UsersController {

    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("title", "Trang chủ");
        model.addAttribute("content", "user/home :: content");
        return "layout/user-layout";
    }

    @GetMapping("/test-header")
    public String testHeader() {
        return "fragments/header";
    }
}
