package com.example.watchaura.controller;

import com.example.watchaura.dto.HoaDonChiTietDTO;
import com.example.watchaura.service.HoaDonChiTietService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/hoa-don-chi-tiet")
@RequiredArgsConstructor
public class HoaDonChiTietController {

    private final HoaDonChiTietService hoaDonChiTietService;

    @GetMapping
    public String list(Model model) {
        List<HoaDonChiTietDTO> list = hoaDonChiTietService.getAll();
        model.addAttribute("title", "Chi tiết hóa đơn");
        model.addAttribute("content", "admin/hoadonchitiet-list");
        model.addAttribute("list", list);
        return "layout/admin-layout";
    }
}
