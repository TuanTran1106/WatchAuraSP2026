package com.example.watchaura.controller;

import com.example.watchaura.entity.Voucher;
import com.example.watchaura.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/admin/voucher")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    private static final int PAGE_SIZE = 6;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                      @RequestParam(required = false) String q,
                      @RequestParam(required = false) String trangThai,
                      Model model) {
        Boolean filterTrangThai = parseTrangThai(trangThai);
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<Voucher> pageResult = voucherService.searchPage(q, filterTrangThai, pageable);
        model.addAttribute("title", "Voucher");
        model.addAttribute("content", "admin/voucher-list");
        model.addAttribute("list", pageResult.getContent());
        model.addAttribute("page", pageResult);
        model.addAttribute("searchKeyword", q != null ? q : "");
        model.addAttribute("filterTrangThai", trangThai != null ? trangThai : "");
        model.addAttribute("voucher", new Voucher());
        model.addAttribute("formAction", "/admin/voucher");
        return "layout/admin-layout";
    }

    @GetMapping("/them")
    public String formCreate() {
        return "redirect:/admin/voucher";
    }

    @GetMapping("/{id}/sua")
    public String formEdit(@PathVariable Integer id,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(required = false) String q,
                           @RequestParam(required = false) String trangThai,
                           Model model) {
        Boolean filterTrangThai = parseTrangThai(trangThai);
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<Voucher> pageResult = voucherService.searchPage(q, filterTrangThai, pageable);
        Voucher voucher = voucherService.getById(id);
        model.addAttribute("title", "Voucher");
        model.addAttribute("content", "admin/voucher-list");
        model.addAttribute("list", pageResult.getContent());
        model.addAttribute("page", pageResult);
        model.addAttribute("searchKeyword", q != null ? q : "");
        model.addAttribute("filterTrangThai", trangThai != null ? trangThai : "");
        model.addAttribute("voucher", voucher);
        model.addAttribute("voucherId", id);
        String formAction = "/admin/voucher/" + id + "?page=" + page;
        if (q != null && !q.isBlank()) formAction += "&q=" + URLEncoder.encode(q, StandardCharsets.UTF_8);
        if (trangThai != null && !trangThai.isBlank()) formAction += "&trangThai=" + URLEncoder.encode(trangThai, StandardCharsets.UTF_8);
        model.addAttribute("formAction", formAction);
        return "layout/admin-layout";
    }

    private Boolean parseTrangThai(String trangThai) {
        if (trangThai == null || trangThai.isBlank()) return null;
        if ("true".equalsIgnoreCase(trangThai)) return true;
        if ("false".equalsIgnoreCase(trangThai)) return false;
        return null;
    }

    @PostMapping
    public String create(@Validated(Voucher.OnCreate.class) @ModelAttribute("voucher") Voucher voucher,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            Pageable pageable = PageRequest.of(0, PAGE_SIZE);
            Page<Voucher> pageResult = voucherService.searchPage(null, null, pageable);
            model.addAttribute("title", "Voucher");
            model.addAttribute("content", "admin/voucher-list");
            model.addAttribute("list", pageResult.getContent());
            model.addAttribute("page", pageResult);
            model.addAttribute("searchKeyword", "");
            model.addAttribute("filterTrangThai", "");
            model.addAttribute("formAction", "/admin/voucher");
            return "layout/admin-layout";
        }
        try {
            voucherService.create(voucher);
            redirect.addFlashAttribute("message", "Thêm voucher thành công.");
            return "redirect:/admin/voucher#listVoucher";
        } catch (RuntimeException e) {
            Pageable pageable = PageRequest.of(0, PAGE_SIZE);
            Page<Voucher> pageResult = voucherService.searchPage(null, null, pageable);
            model.addAttribute("title", "Voucher");
            model.addAttribute("content", "admin/voucher-list");
            model.addAttribute("list", pageResult.getContent());
            model.addAttribute("page", pageResult);
            model.addAttribute("searchKeyword", "");
            model.addAttribute("filterTrangThai", "");
            model.addAttribute("voucher", voucher);
            model.addAttribute("formAction", "/admin/voucher");
            model.addAttribute("error", e.getMessage());
            return "layout/admin-layout";
        }
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id, @RequestParam(defaultValue = "0") int page,
                         @RequestParam(required = false) String q,
                         @RequestParam(required = false) String trangThai,
                         @Valid @ModelAttribute("voucher") Voucher voucher,
                         BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            Boolean filterTrangThai = parseTrangThai(trangThai);
            Pageable pageable = PageRequest.of(page, PAGE_SIZE);
            Page<Voucher> pageResult = voucherService.searchPage(q, filterTrangThai, pageable);
            model.addAttribute("title", "Voucher");
            model.addAttribute("content", "admin/voucher-list");
            model.addAttribute("list", pageResult.getContent());
            model.addAttribute("page", pageResult);
            model.addAttribute("searchKeyword", q != null ? q : "");
            model.addAttribute("filterTrangThai", trangThai != null ? trangThai : "");
            model.addAttribute("voucherId", id);
            String formAction = "/admin/voucher/" + id + "?page=" + page;
            if (q != null && !q.isBlank()) formAction += "&q=" + URLEncoder.encode(q, StandardCharsets.UTF_8);
            if (trangThai != null && !trangThai.isBlank()) formAction += "&trangThai=" + URLEncoder.encode(trangThai, StandardCharsets.UTF_8);
            model.addAttribute("formAction", formAction);
            return "layout/admin-layout";
        }
        try {
            voucherService.update(id, voucher);
            redirect.addFlashAttribute("message", "Cập nhật voucher thành công.");
            redirect.addAttribute("page", page);
            if (q != null && !q.isBlank()) redirect.addAttribute("q", q);
            if (trangThai != null && !trangThai.isBlank()) redirect.addAttribute("trangThai", trangThai);
            return "redirect:/admin/voucher#listVoucher";
        } catch (RuntimeException e) {
            Boolean filterTrangThai = parseTrangThai(trangThai);
            Pageable pageable = PageRequest.of(page, PAGE_SIZE);
            Page<Voucher> pageResult = voucherService.searchPage(q, filterTrangThai, pageable);
            model.addAttribute("title", "Voucher");
            model.addAttribute("content", "admin/voucher-list");
            model.addAttribute("list", pageResult.getContent());
            model.addAttribute("page", pageResult);
            model.addAttribute("searchKeyword", q != null ? q : "");
            model.addAttribute("filterTrangThai", trangThai != null ? trangThai : "");
            model.addAttribute("voucher", voucher);
            model.addAttribute("voucherId", id);
            String formAction = "/admin/voucher/" + id + "?page=" + page;
            if (q != null && !q.isBlank()) formAction += "&q=" + URLEncoder.encode(q, StandardCharsets.UTF_8);
            if (trangThai != null && !trangThai.isBlank()) formAction += "&trangThai=" + URLEncoder.encode(trangThai, StandardCharsets.UTF_8);
            model.addAttribute("formAction", formAction);
            model.addAttribute("error", e.getMessage());
            return "layout/admin-layout";
        }
    }

    @PostMapping("/{id}/xoa")
    public String delete(@PathVariable Integer id, @RequestParam(defaultValue = "0") int page,
                         @RequestParam(required = false) String q,
                         @RequestParam(required = false) String trangThai,
                         RedirectAttributes redirect) {
        voucherService.delete(id);
        redirect.addFlashAttribute("message", "Xóa voucher thành công.");
        redirect.addAttribute("page", page);
        if (q != null && !q.isBlank()) redirect.addAttribute("q", q);
        if (trangThai != null && !trangThai.isBlank()) redirect.addAttribute("trangThai", trangThai);
        return "redirect:/admin/voucher#listVoucher";
    }

    @PostMapping("/{id}/trang-thai")
    public String toggleTrangThai(@PathVariable Integer id, @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(required = false) String q,
                                  @RequestParam(required = false) String trangThai,
                                  RedirectAttributes redirect) {
        voucherService.toggleTrangThai(id);
        redirect.addFlashAttribute("message", "Đã cập nhật trạng thái voucher.");
        redirect.addAttribute("page", page);
        if (q != null && !q.isBlank()) redirect.addAttribute("q", q);
        if (trangThai != null && !trangThai.isBlank()) redirect.addAttribute("trangThai", trangThai);
        return "redirect:/admin/voucher#listVoucher";
    }
}
