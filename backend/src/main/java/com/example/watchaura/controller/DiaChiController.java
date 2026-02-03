package com.example.watchaura.controller;

import com.example.watchaura.entity.DiaChi;
import com.example.watchaura.service.DiaChiService;
import com.example.watchaura.service.KhachHangService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin/dia-chi")
@RequiredArgsConstructor
public class DiaChiController {

    private final DiaChiService diaChiService;
    private final KhachHangService khachHangService;

    @GetMapping
    public String list(@RequestParam(required = false) Integer khachHangId, Model model) {
        List<DiaChi> list = khachHangId != null
                ? diaChiService.getByKhachHang(khachHangId)
                : List.of();
        model.addAttribute("title", "Địa chỉ");
        model.addAttribute("content", "admin/diachi-list");
        model.addAttribute("list", list);
        model.addAttribute("khachHangList", khachHangService.getAll());
        model.addAttribute("selectedKhachHangId", khachHangId);
        return "layout/admin-layout";
    }
}
