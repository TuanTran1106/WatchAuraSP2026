package com.example.watchaura.controller;

import com.example.watchaura.dto.GioHangDTO;
import com.example.watchaura.service.GioHangService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/gio-hang")
@RequiredArgsConstructor
public class GioHangController {

    private final GioHangService gioHangService;

    @GetMapping
    public String list(Model model) {
        List<GioHangDTO> list = gioHangService.getAll();
        model.addAttribute("title", "Giỏ hàng");
        model.addAttribute("content", "admin/giohang-list");
        model.addAttribute("list", list);
        return "layout/admin-layout";
    }
}
