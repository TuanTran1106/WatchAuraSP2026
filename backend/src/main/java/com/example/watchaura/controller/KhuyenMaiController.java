package com.example.watchaura.controller;

import com.example.watchaura.entity.KhuyenMai;
import com.example.watchaura.service.KhuyenMaiService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/admin/khuyen-mai")
@RequiredArgsConstructor
public class KhuyenMaiController {

    private final KhuyenMaiService khuyenMaiService;

    private static final int PAGE_SIZE = 6;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                      @RequestParam(required = false) String q,
                      @RequestParam(required = false) String trangThai,
                      Model model) {
        Boolean filterTrangThai = parseTrangThai(trangThai);
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").descending());
        Page<KhuyenMai> pageResult = khuyenMaiService.searchPage(q, filterTrangThai, pageable);
        model.addAttribute("title", "Khuyến mãi");
        model.addAttribute("content", "admin/khuyenmai-list");
        model.addAttribute("list", pageResult.getContent());
        model.addAttribute("page", pageResult);
        model.addAttribute("searchKeyword", q != null ? q : "");
        model.addAttribute("filterTrangThai", trangThai != null ? trangThai : "");
        model.addAttribute("khuyenMai", new KhuyenMai());
        model.addAttribute("formAction", "/admin/khuyen-mai");
        return "layout/admin-layout";
    }

    private Boolean parseTrangThai(String trangThai) {
        if (trangThai == null || trangThai.isBlank()) return null;
        if ("true".equalsIgnoreCase(trangThai)) return true;
        if ("false".equalsIgnoreCase(trangThai)) return false;
        return null;
    }

    @GetMapping("/them")
    public String formCreate() {
        return "redirect:/admin/khuyen-mai";
    }

    @GetMapping("/{id}/sua")
    public String formEdit(@PathVariable Integer id,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(required = false) String q,
                           @RequestParam(required = false) String trangThai,
                           Model model) {
        Boolean filterTrangThai = parseTrangThai(trangThai);
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").descending());
        Page<KhuyenMai> pageResult = khuyenMaiService.searchPage(q, filterTrangThai, pageable);
        KhuyenMai khuyenMai = khuyenMaiService.findById(id);
        if (khuyenMai == null) {
            return "redirect:/admin/khuyen-mai";
        }
        model.addAttribute("title", "Khuyến mãi");
        model.addAttribute("content", "admin/khuyenmai-list");
        model.addAttribute("list", pageResult.getContent());
        model.addAttribute("page", pageResult);
        model.addAttribute("searchKeyword", q != null ? q : "");
        model.addAttribute("filterTrangThai", trangThai != null ? trangThai : "");
        model.addAttribute("khuyenMai", khuyenMai);
        String formAction = "/admin/khuyen-mai/" + id + "?page=" + page;
        if (q != null && !q.isBlank()) formAction += "&q=" + URLEncoder.encode(q, StandardCharsets.UTF_8);
        if (trangThai != null && !trangThai.isBlank()) formAction += "&trangThai=" + URLEncoder.encode(trangThai, StandardCharsets.UTF_8);
        model.addAttribute("formAction", formAction);
        return "layout/admin-layout";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("khuyenMai") KhuyenMai khuyenMai, BindingResult result,
                         @RequestParam(required = false) String q,
                         @RequestParam(required = false) String filterTrangThai,
                         Model model, RedirectAttributes redirect) {
        if (khuyenMai.getMaKhuyenMai() != null && !khuyenMai.getMaKhuyenMai().isBlank()
                && khuyenMaiService.existsByMaKhuyenMai(khuyenMai.getMaKhuyenMai())) {
            result.rejectValue("maKhuyenMai", "duplicate", "Mã khuyến mãi đã tồn tại.");
        }
        if (result.hasErrors()) {
            Boolean filterTrangThaiBool = parseTrangThai(filterTrangThai);
            Pageable pageable = PageRequest.of(0, PAGE_SIZE, Sort.by("id").descending());
            Page<KhuyenMai> pageResult = khuyenMaiService.searchPage(q, filterTrangThaiBool, pageable);
            model.addAttribute("title", "Khuyến mãi");
            model.addAttribute("content", "admin/khuyenmai-list");
            model.addAttribute("list", pageResult.getContent());
            model.addAttribute("page", pageResult);
            model.addAttribute("searchKeyword", q != null ? q : "");
            model.addAttribute("filterTrangThai", filterTrangThai != null ? filterTrangThai : "");
            model.addAttribute("formAction", "/admin/khuyen-mai");
            return "layout/admin-layout";
        }
        khuyenMaiService.save(khuyenMai);
        redirect.addFlashAttribute("message", "Thêm khuyến mãi thành công.");
        if (q != null && !q.isBlank()) redirect.addAttribute("q", q);
        if (filterTrangThai != null && !filterTrangThai.isBlank()) redirect.addAttribute("trangThai", filterTrangThai);
        return "redirect:/admin/khuyen-mai#listKhuyenMai";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id, @RequestParam(defaultValue = "0") int page,
                         @RequestParam(required = false) String q,
                         @RequestParam(required = false) String filterTrangThai,
                         @Valid @ModelAttribute("khuyenMai") KhuyenMai khuyenMai, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        if (khuyenMai.getMaKhuyenMai() != null && !khuyenMai.getMaKhuyenMai().isBlank()
                && khuyenMaiService.existsByMaKhuyenMaiAndIdNot(khuyenMai.getMaKhuyenMai(), id)) {
            result.rejectValue("maKhuyenMai", "duplicate", "Mã khuyến mãi đã tồn tại.");
        }
        if (result.hasErrors()) {
            Boolean filterTrangThaiBool = parseTrangThai(filterTrangThai);
            Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").descending());
            Page<KhuyenMai> pageResult = khuyenMaiService.searchPage(q, filterTrangThaiBool, pageable);
            model.addAttribute("title", "Khuyến mãi");
            model.addAttribute("content", "admin/khuyenmai-list");
            model.addAttribute("list", pageResult.getContent());
            model.addAttribute("page", pageResult);
            model.addAttribute("searchKeyword", q != null ? q : "");
            model.addAttribute("filterTrangThai", filterTrangThai != null ? filterTrangThai : "");
            model.addAttribute("khuyenMai", khuyenMai);
            String formAction = "/admin/khuyen-mai/" + id + "?page=" + page;
            if (q != null && !q.isBlank()) formAction += "&q=" + URLEncoder.encode(q, StandardCharsets.UTF_8);
            if (filterTrangThai != null && !filterTrangThai.isBlank()) formAction += "&filterTrangThai=" + URLEncoder.encode(filterTrangThai, StandardCharsets.UTF_8);
            model.addAttribute("formAction", formAction);
            return "layout/admin-layout";
        }
        khuyenMaiService.update(id, khuyenMai);
        redirect.addFlashAttribute("message", "Cập nhật khuyến mãi thành công.");
        redirect.addAttribute("page", page);
        if (q != null && !q.isBlank()) redirect.addAttribute("q", q);
        if (filterTrangThai != null && !filterTrangThai.isBlank()) redirect.addAttribute("trangThai", filterTrangThai);
        return "redirect:/admin/khuyen-mai#listKhuyenMai";
    }

    @PostMapping("/{id}/xoa")
    public String delete(@PathVariable Integer id, @RequestParam(defaultValue = "0") int page,
                        @RequestParam(required = false) String q,
                        @RequestParam(required = false) String trangThai,
                        RedirectAttributes redirect) {
        khuyenMaiService.delete(id);
        redirect.addFlashAttribute("message", "Xóa khuyến mãi thành công.");
        redirect.addAttribute("page", page);
        if (q != null && !q.isBlank()) redirect.addAttribute("q", q);
        if (trangThai != null && !trangThai.isBlank()) redirect.addAttribute("trangThai", trangThai);
        return "redirect:/admin/khuyen-mai#listKhuyenMai";
    }

    @PostMapping("/{id}/trang-thai")
    public String toggleTrangThai(@PathVariable Integer id, @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(required = false) String q,
                                 @RequestParam(required = false) String trangThai,
                                 RedirectAttributes redirect) {
        khuyenMaiService.toggleTrangThai(id);
        redirect.addFlashAttribute("message", "Đã cập nhật trạng thái.");
        redirect.addAttribute("page", page);
        if (q != null && !q.isBlank()) redirect.addAttribute("q", q);
        if (trangThai != null && !trangThai.isBlank()) redirect.addAttribute("trangThai", trangThai);
        return "redirect:/admin/khuyen-mai#listKhuyenMai";
    }
}
