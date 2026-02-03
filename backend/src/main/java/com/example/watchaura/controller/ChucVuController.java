package com.example.watchaura.controller;

import com.example.watchaura.entity.ChucVu;
import com.example.watchaura.service.ChucVuService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/chuc-vu")
@RequiredArgsConstructor
public class ChucVuController {

    private final ChucVuService chucVuService;

    @GetMapping
    public String list(Model model) {
        List<ChucVu> list = chucVuService.getAll();
        model.addAttribute("title", "Chức vụ");
        model.addAttribute("content", "admin/chucvu-list");
        model.addAttribute("list", list);
        return "layout/admin-layout";
    }

    @GetMapping("/them")
    public String formCreate(Model model) {
        model.addAttribute("title", "Thêm chức vụ");
        model.addAttribute("content", "admin/chucvu-form");
        model.addAttribute("chucVu", new ChucVu());
        model.addAttribute("formAction", "/admin/chuc-vu");
        return "layout/admin-layout";
    }

    @GetMapping("/{id}/sua")
    public String formEdit(@PathVariable Integer id, Model model) {
        ChucVu chucVu = chucVuService.getById(id);
        model.addAttribute("title", "Sửa chức vụ");
        model.addAttribute("content", "admin/chucvu-form");
        model.addAttribute("chucVu", chucVu);
        model.addAttribute("formAction", "/admin/chuc-vu/" + id);
        return "layout/admin-layout";
    }

    @PostMapping
    public String create(@ModelAttribute ChucVu chucVu, RedirectAttributes redirect) {
        chucVuService.create(chucVu);
        redirect.addFlashAttribute("message", "Thêm chức vụ thành công.");
        return "redirect:/admin/chuc-vu";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id, @ModelAttribute ChucVu chucVu, RedirectAttributes redirect) {
        chucVuService.update(id, chucVu);
        redirect.addFlashAttribute("message", "Cập nhật chức vụ thành công.");
        return "redirect:/admin/chuc-vu";
    }

    @PostMapping("/{id}/xoa")
    public String delete(@PathVariable Integer id, RedirectAttributes redirect) {
        chucVuService.delete(id);
        redirect.addFlashAttribute("message", "Xóa chức vụ thành công.");
        return "redirect:/admin/chuc-vu";
    }
}
