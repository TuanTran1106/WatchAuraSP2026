package com.example.watchaura.controller;

import com.example.watchaura.dto.DiaChiGiaoHangDTO;
import com.example.watchaura.service.DiaChiGiaoHangService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin/dia-chi-giao-hang")
@RequiredArgsConstructor
public class DiaChiGiaoHangController {

    private final DiaChiGiaoHangService diaChiGiaoHangService;

    @GetMapping
    public String list(Model model) {
        List<DiaChiGiaoHangDTO> list = diaChiGiaoHangService.getAll();
        model.addAttribute("title", "Địa chỉ giao hàng");
        model.addAttribute("content", "admin/diachigiaohang-list");
        model.addAttribute("list", list);
        return "layout/admin-layout";
    }
}
