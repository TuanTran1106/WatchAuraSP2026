package com.example.watchaura.controller;

import com.example.watchaura.entity.MauSac;
import com.example.watchaura.repository.MauSacRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/mau-sac")
@RequiredArgsConstructor
public class MauSacController {

    private final MauSacRepository mauSacRepository;

    @GetMapping
    public String list(Model model) {
        List<MauSac> list = mauSacRepository.findAll();
        model.addAttribute("title", "Màu sắc");
        model.addAttribute("content", "admin/mausac-list");
        model.addAttribute("list", list);
        model.addAttribute("mauSac", new MauSac());
        model.addAttribute("mauSacId", null);
        model.addAttribute("formAction", "/admin/mau-sac");
        return "layout/admin-layout";
    }

    @GetMapping("/them")
    public String formCreate() {
        return "redirect:/admin/mau-sac";
    }

    @GetMapping("/{id}/sua")
    public String formEdit(@PathVariable Integer id, Model model) {
        List<MauSac> list = mauSacRepository.findAll();
        MauSac mauSac = mauSacRepository.findById(id).orElseThrow();
        model.addAttribute("title", "Màu sắc");
        model.addAttribute("content", "admin/mausac-list");
        model.addAttribute("list", list);
        model.addAttribute("mauSac", mauSac);
        model.addAttribute("mauSacId", id);
        model.addAttribute("formAction", "/admin/mau-sac/" + id);
        return "layout/admin-layout";
    }

    @PostMapping
    public String create(@ModelAttribute MauSac mauSac, RedirectAttributes redirect) {
        mauSacRepository.save(mauSac);
        redirect.addFlashAttribute("message", "Thêm màu sắc thành công.");
        return "redirect:/admin/mau-sac";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id, @ModelAttribute MauSac mauSac, RedirectAttributes redirect) {
        mauSac.setId(id);
        mauSacRepository.save(mauSac);
        redirect.addFlashAttribute("message", "Cập nhật màu sắc thành công.");
        return "redirect:/admin/mau-sac";
    }

    @PostMapping("/{id}/xoa")
    public String delete(@PathVariable Integer id, RedirectAttributes redirect) {
        mauSacRepository.deleteById(id);
        redirect.addFlashAttribute("message", "Xóa màu sắc thành công.");
        return "redirect:/admin/mau-sac";
    }
}
