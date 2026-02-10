package com.example.watchaura.controller;

import com.example.watchaura.entity.Voucher;
import com.example.watchaura.service.VoucherService;
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
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").descending());
        Page<Voucher> pageResult = voucherService.searchPage(q, filterTrangThai, pageable);
        model.addAttribute("title", "Voucher");
        model.addAttribute("content", "admin/voucher-list");
        model.addAttribute("list", pageResult.getContent());
        model.addAttribute("page", pageResult);
        model.addAttribute("searchKeyword", q != null ? q : "");
        model.addAttribute("filterTrangThai", trangThai != null ? trangThai : "");
        Voucher newVoucher = new Voucher();
        newVoucher.setTrangThai(true); // Mặc định Đang hoạt động khi thêm mới (tránh NULL vào DB)
        model.addAttribute("voucher", newVoucher);
        model.addAttribute("formAction", "/admin/voucher");
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
        return "redirect:/admin/voucher";
    }

    @GetMapping("/{id}/sua")
    public String formEdit(@PathVariable Integer id,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(required = false) String q,
                            @RequestParam(required = false) String trangThai,
                            Model model) {
        Boolean filterTrangThai = parseTrangThai(trangThai);
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").descending());
        Page<Voucher> pageResult = voucherService.searchPage(q, filterTrangThai, pageable);
        Voucher voucher = voucherService.findById(id);
        if (voucher == null) {
            return "redirect:/admin/voucher";
        }
        model.addAttribute("title", "Voucher");
        model.addAttribute("content", "admin/voucher-list");
        model.addAttribute("list", pageResult.getContent());
        model.addAttribute("page", pageResult);
        model.addAttribute("searchKeyword", q != null ? q : "");
        model.addAttribute("filterTrangThai", trangThai != null ? trangThai : "");
        model.addAttribute("voucher", voucher);
        String formAction = "/admin/voucher/" + id + "?page=" + page;
        if (q != null && !q.isBlank()) formAction += "&q=" + URLEncoder.encode(q, StandardCharsets.UTF_8);
        if (trangThai != null && !trangThai.isBlank()) formAction += "&trangThai=" + URLEncoder.encode(trangThai, StandardCharsets.UTF_8);
        model.addAttribute("formAction", formAction);
        return "layout/admin-layout";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("voucher") Voucher voucher, BindingResult result,
                         @RequestParam(required = false) String q,
                         @RequestParam(required = false) String filterTrangThai,
                         Model model, RedirectAttributes redirect) {
        if (voucher.getMaVoucher() != null && !voucher.getMaVoucher().isBlank()
                && voucherService.existsByMaVoucher(voucher.getMaVoucher())) {
            result.rejectValue("maVoucher", "duplicate", "Mã voucher đã tồn tại.");
        }
        if (result.hasErrors()) {
            Boolean filterTrangThaiBool = parseTrangThai(filterTrangThai);
            Pageable pageable = PageRequest.of(0, PAGE_SIZE, Sort.by("id").descending());
            Page<Voucher> pageResult = voucherService.searchPage(q, filterTrangThaiBool, pageable);
            model.addAttribute("title", "Voucher");
            model.addAttribute("content", "admin/voucher-list");
            model.addAttribute("list", pageResult.getContent());
            model.addAttribute("page", pageResult);
            model.addAttribute("searchKeyword", q != null ? q : "");
            model.addAttribute("filterTrangThai", filterTrangThai != null ? filterTrangThai : "");
            model.addAttribute("formAction", "/admin/voucher");
            return "layout/admin-layout";
        }
        voucherService.save(voucher);
        redirect.addFlashAttribute("message", "Thêm voucher thành công.");
        if (q != null && !q.isBlank()) redirect.addAttribute("q", q);
        if (filterTrangThai != null && !filterTrangThai.isBlank()) redirect.addAttribute("trangThai", filterTrangThai);
        return "redirect:/admin/voucher#listVoucher";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id, @RequestParam(defaultValue = "0") int page,
                         @RequestParam(required = false) String q,
                         @RequestParam(required = false) String filterTrangThai,
                         @Valid @ModelAttribute("voucher") Voucher voucher, BindingResult result,
                         Model model, RedirectAttributes redirect) {
        if (voucher.getMaVoucher() != null && !voucher.getMaVoucher().isBlank()
                && voucherService.existsByMaVoucherAndIdNot(voucher.getMaVoucher(), id)) {
            result.rejectValue("maVoucher", "duplicate", "Mã voucher đã tồn tại.");
        }
        if (result.hasErrors()) {
            Boolean filterTrangThaiBool = parseTrangThai(filterTrangThai);
            Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").descending());
            Page<Voucher> pageResult = voucherService.searchPage(q, filterTrangThaiBool, pageable);
            model.addAttribute("title", "Voucher");
            model.addAttribute("content", "admin/voucher-list");
            model.addAttribute("list", pageResult.getContent());
            model.addAttribute("page", pageResult);
            model.addAttribute("searchKeyword", q != null ? q : "");
            model.addAttribute("filterTrangThai", filterTrangThai != null ? filterTrangThai : "");
            model.addAttribute("voucher", voucher);
            String formAction = "/admin/voucher/" + id + "?page=" + page;
            if (q != null && !q.isBlank()) formAction += "&q=" + URLEncoder.encode(q, StandardCharsets.UTF_8);
            if (filterTrangThai != null && !filterTrangThai.isBlank()) formAction += "&trangThai=" + URLEncoder.encode(filterTrangThai, StandardCharsets.UTF_8);
            model.addAttribute("formAction", formAction);
            return "layout/admin-layout";
        }
        voucherService.update(id, voucher);
        redirect.addFlashAttribute("message", "Cập nhật voucher thành công.");
        redirect.addAttribute("page", page);
        if (q != null && !q.isBlank()) redirect.addAttribute("q", q);
        if (filterTrangThai != null && !filterTrangThai.isBlank()) redirect.addAttribute("trangThai", filterTrangThai);
        return "redirect:/admin/voucher#listVoucher";
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
        redirect.addFlashAttribute("message", "Đã cập nhật trạng thái.");
        redirect.addAttribute("page", page);
        if (q != null && !q.isBlank()) redirect.addAttribute("q", q);
        if (trangThai != null && !trangThai.isBlank()) redirect.addAttribute("trangThai", trangThai);
        return "redirect:/admin/voucher#listVoucher";
    }
}
