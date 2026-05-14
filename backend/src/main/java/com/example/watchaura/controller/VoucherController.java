package com.example.watchaura.controller;

import com.example.watchaura.annotation.RequiresRole;
import com.example.watchaura.entity.Voucher;
import com.example.watchaura.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.beans.PropertyEditorSupport;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

@Controller
@RequestMapping("/admin/voucher")
@RequiredArgsConstructor
@RequiresRole({"Admin", "Quản lý"})
public class VoucherController {

    private final VoucherService voucherService;

    private static final int PAGE_SIZE = 6;
    private static final DateTimeFormatter DATETIME_LOCAL = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    @InitBinder
    public void initBinderVoucher(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, "maVoucher", new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                if (text == null || text.isBlank()) {
                    setValue(null);
                    return;
                }
                setValue(text.trim().toUpperCase(Locale.ROOT));
            }
        });
        binder.registerCustomEditor(String.class, "loaiVoucher", new StringTrimmerEditor(true));
        binder.registerCustomEditor(String.class, "tenVoucher", new StringTrimmerEditor(true));
        binder.registerCustomEditor(String.class, "moTa", new StringTrimmerEditor(true));
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String q,
                       @RequestParam(required = false) String trangThai,
                       @RequestParam(required = false) String filterTrangThai,
                       Model model,
                       @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        String filterTrangThaiValue = resolveFilterTrangThai(filterTrangThai, trangThai);
        Boolean filterTrangThaiBool = parseTrangThai(filterTrangThaiValue);
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").descending());
        Page<Voucher> pageResult = voucherService.searchPage(q, filterTrangThaiBool, pageable);
        model.addAttribute("title", "Voucher");
        model.addAttribute("content", "admin/voucher-list");
        model.addAttribute("list", pageResult.getContent());
        model.addAttribute("page", pageResult);
        model.addAttribute("searchKeyword", q != null ? q : "");
        model.addAttribute("filterTrangThai", filterTrangThaiValue);
        model.addAttribute("trangThai", filterTrangThaiValue);
        Voucher newVoucher = new Voucher();
        newVoucher.setTrangThai(true);
        model.addAttribute("voucher", newVoucher);
        model.addAttribute("formAction", "/admin/voucher");
        if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
            return "admin/voucher-list :: content";
        }
        return "layout/admin-layout";
    }

    private Boolean parseTrangThai(String trangThai) {
        if (trangThai == null || trangThai.isBlank()) return null;
        if ("true".equalsIgnoreCase(trangThai)) return true;
        if ("false".equalsIgnoreCase(trangThai)) return false;
        return null;
    }

    private String resolveFilterTrangThai(String filterTrangThai, String trangThai) {
        if (filterTrangThai != null && !filterTrangThai.isBlank()) return filterTrangThai;
        if (trangThai != null && !trangThai.isBlank()) return trangThai;
        return "";
    }

    private LocalDateTime parseDateTimeLocalOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(value.trim(), DATETIME_LOCAL);
        } catch (DateTimeParseException ex) {
            return null;
        }
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
                            Model model,
                            @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        Boolean filterTrangThai = parseTrangThai(trangThai);
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").descending());
        Page<Voucher> pageResult = voucherService.searchPage(q, filterTrangThai, pageable);
        Voucher voucher = voucherService.findById(id);
        if (voucher == null) {
            return "redirect:/admin/voucher";
        }
        if (voucher.getTrangThai() == null) {
            voucher.setTrangThai(false);
        }
        model.addAttribute("title", "Voucher");
        model.addAttribute("content", "admin/voucher-list");
        model.addAttribute("list", pageResult.getContent());
        model.addAttribute("page", pageResult);
        model.addAttribute("searchKeyword", q != null ? q : "");
        model.addAttribute("filterTrangThai", trangThai != null ? trangThai : "");
        model.addAttribute("voucher", voucher);
        String formAction = "/admin/voucher/" + id + "?page=" + page;
        if (q != null && !q.isBlank()) {
            formAction += "&q=" + URLEncoder.encode(q, StandardCharsets.UTF_8);
        }
        model.addAttribute("formAction", formAction);
        if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
            return "admin/voucher-list :: content";
        }
        return "layout/admin-layout";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("voucher") Voucher voucher, BindingResult result,
                         @RequestParam(required = false) String q,
                         @RequestParam(required = false) String trangThai,
                         @RequestParam(required = false) String filterTrangThai,
                         @RequestParam(required = false) String ngayBatDau,
                         @RequestParam(required = false) String ngayKetThuc,
                         Model model, RedirectAttributes redirect,
                         @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        if (voucher.getNgayBatDau() == null && ngayBatDau != null && !ngayBatDau.isBlank()) {
            voucher.setNgayBatDau(parseDateTimeLocalOrNull(ngayBatDau));
        }
        if (voucher.getNgayKetThuc() == null && ngayKetThuc != null && !ngayKetThuc.isBlank()) {
            voucher.setNgayKetThuc(parseDateTimeLocalOrNull(ngayKetThuc));
        }

        if (voucher.getMaVoucher() != null && !voucher.getMaVoucher().isBlank()
                && voucherService.existsByMaVoucher(voucher.getMaVoucher())) {
            result.rejectValue("maVoucher", "duplicate", "Mã voucher đã tồn tại.");
        }
        Boolean filterTrangThaiBool = parseTrangThai(filterTrangThai != null ? filterTrangThai : trangThai);
        String filterTrangThaiValue = filterTrangThai != null ? filterTrangThai : (trangThai != null ? trangThai : "");
        if (result.hasErrors()) {
            Pageable pageable = PageRequest.of(0, PAGE_SIZE, Sort.by("id").descending());
            Page<Voucher> pageResult = voucherService.searchPage(q, filterTrangThaiBool, pageable);
            model.addAttribute("title", "Voucher");
            model.addAttribute("content", "admin/voucher-list");
            model.addAttribute("list", pageResult.getContent());
            model.addAttribute("page", pageResult);
            model.addAttribute("searchKeyword", q != null ? q : "");
            model.addAttribute("filterTrangThai", filterTrangThaiValue);
            model.addAttribute("trangThai", filterTrangThaiValue);
            model.addAttribute("formAction", "/admin/voucher");
            model.addAttribute("voucher", voucher);
            if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
                return "admin/voucher-list :: content";
            }
            return "layout/admin-layout";
        }
        voucherService.save(voucher);
        if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
            Pageable pageable = PageRequest.of(0, PAGE_SIZE, Sort.by("id").descending());
            Page<Voucher> pageResult = voucherService.searchPage(q, filterTrangThaiBool, pageable);
            model.addAttribute("title", "Voucher");
            model.addAttribute("content", "admin/voucher-list");
            model.addAttribute("list", pageResult.getContent());
            model.addAttribute("page", pageResult);
            model.addAttribute("searchKeyword", q != null ? q : "");
            model.addAttribute("filterTrangThai", filterTrangThaiValue);
            model.addAttribute("trangThai", filterTrangThaiValue);
            Voucher newVoucher = new Voucher();
            newVoucher.setTrangThai(true);
            model.addAttribute("voucher", newVoucher);
            model.addAttribute("formAction", "/admin/voucher");
            model.addAttribute("message", "Thêm voucher thành công.");
            return "admin/voucher-list :: content";
        }
        redirect.addFlashAttribute("message", "Thêm voucher thành công.");
        if (q != null && !q.isBlank()) redirect.addAttribute("q", q);
        if (!filterTrangThaiValue.isBlank()) {
            redirect.addAttribute("trangThai", filterTrangThaiValue);
        }
        return "redirect:/admin/voucher#listVoucher";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id, @RequestParam(defaultValue = "0") int page,
                         @RequestParam(required = false) String q,
                         @RequestParam(required = false) String filterTrangThai,
                         @RequestParam(required = false) String trangThai,
                         @RequestParam(required = false) String ngayBatDau,
                         @RequestParam(required = false) String ngayKetThuc,
                         @Valid @ModelAttribute("voucher") Voucher voucher, BindingResult result,
                         Model model, RedirectAttributes redirect,
                         @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        if (voucher.getNgayBatDau() == null && ngayBatDau != null && !ngayBatDau.isBlank()) {
            voucher.setNgayBatDau(parseDateTimeLocalOrNull(ngayBatDau));
        }
        if (voucher.getNgayKetThuc() == null && ngayKetThuc != null && !ngayKetThuc.isBlank()) {
            voucher.setNgayKetThuc(parseDateTimeLocalOrNull(ngayKetThuc));
        }

        if (voucher.getMaVoucher() != null && !voucher.getMaVoucher().isBlank()
                && voucherService.existsByMaVoucherAndIdNot(voucher.getMaVoucher(), id)) {
            result.rejectValue("maVoucher", "duplicate", "Mã voucher đã tồn tại.");
        }
        applyVoucherUpdateQuantityRules(id, voucher, result);

        Boolean filterTrangThaiBool = parseTrangThai(filterTrangThai != null ? filterTrangThai : trangThai);
        String filterTrangThaiValue = filterTrangThai != null ? filterTrangThai : (trangThai != null ? trangThai : "");

        if (result.hasErrors()) {
            Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").descending());
            Page<Voucher> pageResult = voucherService.searchPage(q, filterTrangThaiBool, pageable);
            model.addAttribute("title", "Voucher");
            model.addAttribute("content", "admin/voucher-list");
            model.addAttribute("list", pageResult.getContent());
            model.addAttribute("page", pageResult);
            model.addAttribute("searchKeyword", q != null ? q : "");
            model.addAttribute("filterTrangThai", filterTrangThaiValue);
            model.addAttribute("trangThai", filterTrangThaiValue);
            model.addAttribute("voucher", voucher);
            String formAction = "/admin/voucher/" + id + "?page=" + page;
            if (q != null && !q.isBlank()) {
                formAction += "&q=" + URLEncoder.encode(q, StandardCharsets.UTF_8);
            }
            model.addAttribute("formAction", formAction);
            if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
                return "admin/voucher-list :: content";
            }
            return "layout/admin-layout";
        }

        voucherService.update(id, voucher);
        if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
            Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").descending());
            Page<Voucher> pageResult = voucherService.searchPage(q, filterTrangThaiBool, pageable);
            model.addAttribute("title", "Voucher");
            model.addAttribute("content", "admin/voucher-list");
            model.addAttribute("list", pageResult.getContent());
            model.addAttribute("page", pageResult);
            model.addAttribute("searchKeyword", q != null ? q : "");
            model.addAttribute("filterTrangThai", filterTrangThaiValue);
            model.addAttribute("trangThai", filterTrangThaiValue);
            Voucher newVoucher = new Voucher();
            newVoucher.setTrangThai(true);
            model.addAttribute("voucher", newVoucher);
            model.addAttribute("formAction", "/admin/voucher");
            model.addAttribute("message", "Cập nhật voucher thành công.");
            return "admin/voucher-list :: content";
        }

        redirect.addFlashAttribute("message", "Cập nhật voucher thành công.");
        redirect.addAttribute("page", page);
        if (q != null && !q.isBlank()) redirect.addAttribute("q", q);
        if (!filterTrangThaiValue.isBlank()) redirect.addAttribute("trangThai", filterTrangThaiValue);
        return "redirect:/admin/voucher#listVoucher";
    }

    @PostMapping("/{id}/ngung-hoat-dong")
    public String deactivate(@PathVariable Integer id, @RequestParam(defaultValue = "0") int page,
                             @RequestParam(required = false) String q,
                             @RequestParam(required = false) String trangThai,
                             Model model,
                             RedirectAttributes redirect,
                             @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        voucherService.deactivate(id);
        if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
            Boolean filterTrangThaiBool = parseTrangThai(trangThai);
            Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").descending());
            Page<Voucher> pageResult = voucherService.searchPage(q, filterTrangThaiBool, pageable);
            model.addAttribute("title", "Voucher");
            model.addAttribute("content", "admin/voucher-list");
            model.addAttribute("list", pageResult.getContent());
            model.addAttribute("page", pageResult);
            model.addAttribute("searchKeyword", q != null ? q : "");
            model.addAttribute("filterTrangThai", trangThai != null ? trangThai : "");
            Voucher newVoucher = new Voucher();
            newVoucher.setTrangThai(true);
            model.addAttribute("voucher", newVoucher);
            model.addAttribute("formAction", "/admin/voucher");
            model.addAttribute("message", "Đã ngừng hoạt động voucher.");
            return "admin/voucher-list :: content";
        }
        redirect.addFlashAttribute("message", "Đã ngừng hoạt động voucher.");
        redirect.addAttribute("page", page);
        if (q != null && !q.isBlank()) redirect.addAttribute("q", q);
        if (trangThai != null && !trangThai.isBlank()) redirect.addAttribute("trangThai", trangThai);
        return "redirect:/admin/voucher#listVoucher";
    }

    @PostMapping("/{id}/trang-thai")
    public String toggleTrangThai(@PathVariable Integer id, @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(required = false) String q,
                                  @RequestParam(required = false) String trangThai,
                                  Model model,
                                  RedirectAttributes redirect,
                                  @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        String toggleErr = voucherService.toggleTrangThai(id);
        if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
            Boolean filterTrangThaiBool = parseTrangThai(trangThai);
            Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").descending());
            Page<Voucher> pageResult = voucherService.searchPage(q, filterTrangThaiBool, pageable);
            model.addAttribute("title", "Voucher");
            model.addAttribute("content", "admin/voucher-list");
            model.addAttribute("list", pageResult.getContent());
            model.addAttribute("page", pageResult);
            model.addAttribute("searchKeyword", q != null ? q : "");
            model.addAttribute("filterTrangThai", trangThai != null ? trangThai : "");
            Voucher newVoucher = new Voucher();
            newVoucher.setTrangThai(true);
            model.addAttribute("voucher", newVoucher);
            model.addAttribute("formAction", "/admin/voucher");
            if (toggleErr != null) {
                model.addAttribute("toastError", toggleErr);
            } else {
                model.addAttribute("message", "Đã cập nhật trạng thái.");
            }
            return "admin/voucher-list :: content";
        }
        if (toggleErr != null) {
            redirect.addFlashAttribute("toastError", toggleErr);
        } else {
            redirect.addFlashAttribute("message", "Đã cập nhật trạng thái.");
        }
        redirect.addAttribute("page", page);
        if (q != null && !q.isBlank()) redirect.addAttribute("q", q);
        if (trangThai != null && !trangThai.isBlank()) redirect.addAttribute("trangThai", trangThai);
        return "redirect:/admin/voucher#listVoucher";
    }

    /**
     * Quy tắc cần dữ liệu đã lưu (số lượng đã dùng) — không gửi đầy đủ từ form.
     */
    private void applyVoucherUpdateQuantityRules(Integer id, Voucher voucher, BindingResult result) {
        Voucher existing = voucherService.findById(id);
        if (existing == null) {
            return;
        }
        Integer tong = voucher.getSoLuongTong();
        int daDung = existing.getSoLuongDaDung() != null ? existing.getSoLuongDaDung() : 0;
        if (tong != null && tong < daDung) {
            result.rejectValue("soLuongTong", "belowUsed",
                    "Số lượng tổng không được nhỏ hơn số lượng đã dùng (" + daDung + ").");
        }
        if (Boolean.TRUE.equals(voucher.getTrangThai()) && tong != null && tong <= daDung) {
            result.rejectValue("trangThai", "noQuota",
                    "Không thể để đang hoạt động khi đã hết lượt. Hãy tăng số lượng tổng hoặc tắt trạng thái.");
        }
    }
}
