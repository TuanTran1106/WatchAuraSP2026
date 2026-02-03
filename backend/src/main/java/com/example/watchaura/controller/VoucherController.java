package com.example.watchaura.controller;

import com.example.watchaura.entity.Voucher;
import com.example.watchaura.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/voucher")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    @GetMapping
    public String list(Model model) {
        List<Voucher> list = voucherService.getAll();
        model.addAttribute("title", "Voucher");
        model.addAttribute("content", "admin/voucher-list");
        model.addAttribute("list", list);
        return "layout/admin-layout";
    }

    @GetMapping("/them")
    public String formCreate(Model model) {
        model.addAttribute("title", "Thêm voucher");
        model.addAttribute("content", "admin/voucher-form");
        model.addAttribute("voucher", new Voucher());
        model.addAttribute("formAction", "/admin/voucher");
        return "layout/admin-layout";
    }

    @GetMapping("/{id}/sua")
    public String formEdit(@PathVariable Integer id, Model model) {
        Voucher voucher = voucherService.getById(id);
        model.addAttribute("title", "Sửa voucher");
        model.addAttribute("content", "admin/voucher-form");
        model.addAttribute("voucher", voucher);
        model.addAttribute("formAction", "/admin/voucher/" + id);
        return "layout/admin-layout";
    }

    @PostMapping
    public String create(@ModelAttribute Voucher voucher, RedirectAttributes redirect) {
        voucherService.create(voucher);
        redirect.addFlashAttribute("message", "Thêm voucher thành công.");
        return "redirect:/admin/voucher";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id, @ModelAttribute Voucher voucher, RedirectAttributes redirect) {
        voucherService.update(id, voucher);
        redirect.addFlashAttribute("message", "Cập nhật voucher thành công.");
        return "redirect:/admin/voucher";
    }

    @PostMapping("/{id}/xoa")
    public String delete(@PathVariable Integer id, RedirectAttributes redirect) {
        voucherService.delete(id);
        redirect.addFlashAttribute("message", "Xóa voucher thành công.");
        return "redirect:/admin/voucher";
    }
}
