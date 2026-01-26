package com.example.watchaura.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @GetMapping
    public String getAdminPage() {
        // Trả về template: src/main/resources/templates/admin/admin.html
        return "admin/admin";
    }
}


