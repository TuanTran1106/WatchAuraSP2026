package com.example.watchaura.controller;

import com.example.watchaura.entity.KhuyenMai;
import com.example.watchaura.service.KhuyenMaiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/khuyen-mai")
public class KhuyenMaiController {

    @Autowired
    private KhuyenMaiService khuyenMaiService;

    // ================== LIST ==================
    @GetMapping
    public String list(
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        int size = 5; // 5 khuyến mãi / trang

        var pageData = khuyenMaiService.findAll(page, size);

        model.addAttribute("listKhuyenMai", pageData.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageData.getTotalPages());

        return "khuyenmai/list";
    }

    // ================== ADD FORM ==================
    @GetMapping("/them")
    public String showAddForm(Model model) {
        model.addAttribute("khuyenMai", new KhuyenMai());
        return "khuyenmai/form";
    }

    // ================== SAVE ==================
    @PostMapping("/luu")
    public String save(@ModelAttribute("khuyenMai") KhuyenMai khuyenMai,
                       Model model) {

        if (khuyenMaiService.existsByMaKhuyenMai(khuyenMai.getMaKhuyenMai())) {
            model.addAttribute("error", "Mã khuyến mãi đã tồn tại!");
            return "khuyenmai/form";
        }

        khuyenMaiService.save(khuyenMai);
        return "redirect:/admin/khuyen-mai";
    }

    // ================== EDIT FORM ==================
    @GetMapping("/sua/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        KhuyenMai khuyenMai = khuyenMaiService.findById(id);
        if (khuyenMai == null) {
            return "redirect:/admin/khuyen-mai";
        }
        model.addAttribute("khuyenMai", khuyenMai);
        return "khuyenmai/form";
    }

    // ================== UPDATE ==================
    @PostMapping("/cap-nhat/{id}")
    public String update(@PathVariable Integer id,
                         @ModelAttribute("khuyenMai") KhuyenMai khuyenMai) {

        khuyenMaiService.update(id, khuyenMai);
        return "redirect:/admin/khuyen-mai";
    }

    // ================== DELETE ==================
    @GetMapping("/xoa/{id}")
    public String delete(@PathVariable Integer id) {
        khuyenMaiService.delete(id);
        return "redirect:/admin/khuyen-mai";
    }
}
