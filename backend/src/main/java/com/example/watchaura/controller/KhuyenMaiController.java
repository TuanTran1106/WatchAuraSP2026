package com.example.watchaura.controller;

import com.example.watchaura.entity.KhuyenMai;
import com.example.watchaura.service.KhuyenMaiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/khuyen-mai")
@RequiredArgsConstructor
public class KhuyenMaiController {

    private final KhuyenMaiService khuyenMaiService;

    @GetMapping
    public String list(Model model) {
        List<KhuyenMai> list = khuyenMaiService.getAll();
        model.addAttribute("title", "Khuyến mãi");
        model.addAttribute("content", "admin/khuyenmai-list");
        model.addAttribute("list", list);
        return "layout/admin-layout";
    }

    @GetMapping("/them")
    public String formCreate(Model model) {
        model.addAttribute("title", "Thêm khuyến mãi");
        model.addAttribute("content", "admin/khuyenmai-form");
        model.addAttribute("khuyenMai", new KhuyenMai());
        model.addAttribute("formAction", "/admin/khuyen-mai");
        return "layout/admin-layout";
    }

    @GetMapping("/{id}/sua")
    public String formEdit(@PathVariable Integer id, Model model) {
        KhuyenMai khuyenMai = khuyenMaiService.getById(id);
        model.addAttribute("title", "Sửa khuyến mãi");
        model.addAttribute("content", "admin/khuyenmai-form");
        model.addAttribute("khuyenMai", khuyenMai);
        model.addAttribute("formAction", "/admin/khuyen-mai/" + id);
        return "layout/admin-layout";
    }

    @PostMapping
    public String create(@ModelAttribute KhuyenMai khuyenMai, RedirectAttributes redirect) {
        khuyenMaiService.create(khuyenMai);
        redirect.addFlashAttribute("message", "Thêm khuyến mãi thành công.");
        return "redirect:/admin/khuyen-mai";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id, @ModelAttribute KhuyenMai khuyenMai, RedirectAttributes redirect) {
        khuyenMaiService.update(id, khuyenMai);
        redirect.addFlashAttribute("message", "Cập nhật khuyến mãi thành công.");
        return "redirect:/admin/khuyen-mai";
    }

    @PostMapping("/{id}/xoa")
    public String delete(@PathVariable Integer id, RedirectAttributes redirect) {
        khuyenMaiService.delete(id);
        redirect.addFlashAttribute("message", "Xóa khuyến mãi thành công.");
        return "redirect:/admin/khuyen-mai";
    }
}
