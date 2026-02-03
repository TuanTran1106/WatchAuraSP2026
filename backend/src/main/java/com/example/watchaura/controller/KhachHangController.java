package com.example.watchaura.controller;

import com.example.watchaura.entity.KhachHang;
import com.example.watchaura.service.ChucVuService;
import com.example.watchaura.service.KhachHangService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;

@Controller
@RequestMapping("/admin/khach-hang")
@RequiredArgsConstructor
public class KhachHangController {

    private final KhachHangService khachHangService;
    private final ChucVuService chucVuService;

    private static final int PAGE_SIZE = 6;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<KhachHang> pageResult = khachHangService.getPage(pageable);
        model.addAttribute("title", "Khách hàng");
        model.addAttribute("content", "admin/khachhang-list");
        model.addAttribute("list", pageResult.getContent());
        model.addAttribute("page", pageResult);
        model.addAttribute("khachHang", new KhachHang());
        model.addAttribute("chucVuList", chucVuService.getAll());
        model.addAttribute("formAction", "/admin/khach-hang");
        return "layout/admin-layout";
    }

    @GetMapping("/them")
    public String formCreate() {
        return "redirect:/admin/khach-hang";
    }

    @GetMapping("/{id}/sua")
    public String formEdit(@PathVariable Integer id, @RequestParam(defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<KhachHang> pageResult = khachHangService.getPage(pageable);
        KhachHang khachHang = khachHangService.getById(id);
        model.addAttribute("title", "Khách hàng");
        model.addAttribute("content", "admin/khachhang-list");
        model.addAttribute("list", pageResult.getContent());
        model.addAttribute("page", pageResult);
        model.addAttribute("khachHang", khachHang);
        model.addAttribute("chucVuList", chucVuService.getAll());
        model.addAttribute("formAction", "/admin/khach-hang/" + id + "?page=" + page);
        return "layout/admin-layout";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("khachHang") KhachHang khachHang, BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            Pageable pageable = PageRequest.of(0, PAGE_SIZE);
            Page<KhachHang> pageResult = khachHangService.getPage(pageable);
            model.addAttribute("title", "Khách hàng");
            model.addAttribute("content", "admin/khachhang-list");
            model.addAttribute("list", pageResult.getContent());
            model.addAttribute("page", pageResult);
            model.addAttribute("chucVuList", chucVuService.getAll());
            model.addAttribute("formAction", "/admin/khach-hang");
            return "layout/admin-layout";
        }
        khachHangService.create(khachHang);
        redirect.addFlashAttribute("message", "Thêm khách hàng thành công.");
        return "redirect:/admin/khach-hang";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id, @RequestParam(defaultValue = "0") int page, @Valid @ModelAttribute("khachHang") KhachHang khachHang, BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            Pageable pageable = PageRequest.of(page, PAGE_SIZE);
            Page<KhachHang> pageResult = khachHangService.getPage(pageable);
            model.addAttribute("title", "Khách hàng");
            model.addAttribute("content", "admin/khachhang-list");
            model.addAttribute("list", pageResult.getContent());
            model.addAttribute("page", pageResult);
            model.addAttribute("chucVuList", chucVuService.getAll());
            model.addAttribute("formAction", "/admin/khach-hang/" + id);
            model.addAttribute("currentPage", page);
            return "layout/admin-layout";
        }
        khachHangService.update(id, khachHang);
        redirect.addFlashAttribute("message", "Cập nhật khách hàng thành công.");
        return "redirect:/admin/khach-hang?page=" + page;
    }

    @PostMapping("/{id}/xoa")
    public String delete(@PathVariable Integer id, @RequestParam(defaultValue = "0") int page, RedirectAttributes redirect) {
        khachHangService.delete(id);
        redirect.addFlashAttribute("message", "Xóa khách hàng thành công.");
        return "redirect:/admin/khach-hang?page=" + page;
    }
}
