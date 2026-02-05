package com.example.watchaura.controller;

import com.example.watchaura.entity.LoaiMay;
import com.example.watchaura.repository.LoaiMayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/loai-may")
@RequiredArgsConstructor
public class LoaiMayController {

    private final LoaiMayRepository loaiMayRepository;

    @GetMapping
    public String list(Model model) {
        List<LoaiMay> list = loaiMayRepository.findAll();
        model.addAttribute("title", "Loại máy");
        model.addAttribute("content", "admin/loaimay-list");
        model.addAttribute("list", list);
        model.addAttribute("loaiMay", new LoaiMay());
        model.addAttribute("loaiMayId", null);
        model.addAttribute("formAction", "/admin/loai-may");
        return "layout/admin-layout";
    }

    @GetMapping("/them")
    public String formCreate() {
        return "redirect:/admin/loai-may";
    }

    @GetMapping("/{id}/sua")
    public String formEdit(@PathVariable Integer id, Model model) {
        List<LoaiMay> list = loaiMayRepository.findAll();
        LoaiMay loaiMay = loaiMayRepository.findById(id).orElseThrow();
        model.addAttribute("title", "Loại máy");
        model.addAttribute("content", "admin/loaimay-list");
        model.addAttribute("list", list);
        model.addAttribute("loaiMay", loaiMay);
        model.addAttribute("loaiMayId", id);
        model.addAttribute("formAction", "/admin/loai-may/" + id);
        return "layout/admin-layout";
    }

    @PostMapping
    public String create(@ModelAttribute LoaiMay loaiMay, RedirectAttributes redirect) {
        loaiMayRepository.save(loaiMay);
        redirect.addFlashAttribute("message", "Thêm loại máy thành công.");
        return "redirect:/admin/loai-may";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id, @ModelAttribute LoaiMay loaiMay, RedirectAttributes redirect) {
        loaiMay.setId(id);
        loaiMayRepository.save(loaiMay);
        redirect.addFlashAttribute("message", "Cập nhật loại máy thành công.");
        return "redirect:/admin/loai-may";
    }

    @PostMapping("/{id}/xoa")
    public String delete(@PathVariable Integer id, RedirectAttributes redirect) {
        loaiMayRepository.deleteById(id);
        redirect.addFlashAttribute("message", "Xóa loại máy thành công.");
        return "redirect:/admin/loai-may";
    }
}
