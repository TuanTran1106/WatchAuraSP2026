package com.example.watchaura.controller;

import com.example.watchaura.dto.GioHangChiTietDTO;
import com.example.watchaura.service.GioHangChiTietService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/gio-hang-chi-tiet")
@RequiredArgsConstructor
public class GioHangChiTietController {

    private final GioHangChiTietService gioHangChiTietService;

    @GetMapping
    public String list(Model model) {
        List<GioHangChiTietDTO> list = gioHangChiTietService.getAll();
        model.addAttribute("title", "Chi tiết giỏ hàng");
        model.addAttribute("content", "admin/giohangchitiet-list");
        model.addAttribute("list", list);
        return "layout/admin-layout";
    }
}
