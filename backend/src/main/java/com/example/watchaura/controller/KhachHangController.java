package com.example.watchaura.controller;

import com.example.watchaura.annotation.RequiresRole;
import com.example.watchaura.entity.KhachHang;
import com.example.watchaura.service.ChucVuService;
import com.example.watchaura.service.KhachHangService;
import com.example.watchaura.util.PhoneUtils;
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

@Controller
@RequestMapping("/admin/khach-hang")
@RequiredArgsConstructor
@RequiresRole({"Admin", "Quản lý"})
public class KhachHangController {

    private final KhachHangService khachHangService;
    private final ChucVuService chucVuService;

    private static final int PAGE_SIZE = 6;

    @InitBinder("khachHang")
    public void initKhachHangBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, "email", new StringTrimmerEditor(false));
        binder.registerCustomEditor(String.class, "sdt", new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                setValue(text == null ? "" : PhoneUtils.normalizeVnMobile(text));
            }
        });
    }

    private void rejectDuplicateContact(KhachHang kh, BindingResult bindingResult) {
        if (kh.getEmail() != null && !kh.getEmail().isBlank()
                && khachHangService.existsEmailTakenByOther(kh.getEmail(), kh.getId())) {
            bindingResult.rejectValue("email", "duplicate.email", "Email này đã được sử dụng.");
        }
        if (kh.getSdt() != null && !kh.getSdt().isBlank()
                && khachHangService.existsSdtTakenByOther(kh.getSdt(), kh.getId())) {
            bindingResult.rejectValue("sdt", "duplicate.sdt", "Số điện thoại này đã được sử dụng.");
        }
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                      @RequestParam(required = false) String q,
                      @RequestParam(required = false) String trangThai,
                      Model model,
                      @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        Boolean filterTrangThai = parseTrangThai(trangThai);
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id"));
        Page<KhachHang> pageResult = khachHangService.searchPage(q, filterTrangThai, pageable);
        model.addAttribute("title", "Khách hàng");
        model.addAttribute("content", "admin/khachhang-list");
        model.addAttribute("list", pageResult.getContent());
        model.addAttribute("page", pageResult);
        model.addAttribute("searchKeyword", q != null ? q : "");
        model.addAttribute("filterTrangThai", trangThai != null ? trangThai : "");
        model.addAttribute("khachHang", new KhachHang());
        model.addAttribute("chucVuList", chucVuService.getAll());
        model.addAttribute("formAction", "/admin/khach-hang");

        if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
            return "admin/khachhang-list :: content";
        }
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
        return "redirect:/admin/khach-hang";
    }

    @GetMapping("/{id}/sua")
    public String formEdit(@PathVariable Integer id,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(required = false) String q,
                           @RequestParam(required = false) String trangThai,
                           Model model,
                           @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        Boolean filterTrangThai = parseTrangThai(trangThai);
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id"));
        Page<KhachHang> pageResult = khachHangService.searchPage(q, filterTrangThai, pageable);
        KhachHang khachHang = khachHangService.getById(id);
        model.addAttribute("title", "Khách hàng");
        model.addAttribute("content", "admin/khachhang-list");
        model.addAttribute("list", pageResult.getContent());
        model.addAttribute("page", pageResult);
        model.addAttribute("searchKeyword", q != null ? q : "");
        model.addAttribute("filterTrangThai", trangThai != null ? trangThai : "");
        model.addAttribute("khachHang", khachHang);
        model.addAttribute("chucVuList", chucVuService.getAll());
        String formAction = "/admin/khach-hang/" + id + "?page=" + page;
        if (q != null && !q.isBlank()) formAction += "&q=" + URLEncoder.encode(q, StandardCharsets.UTF_8);
        if (trangThai != null && !trangThai.isBlank()) formAction += "&filterTrangThai=" + URLEncoder.encode(trangThai, StandardCharsets.UTF_8);
        model.addAttribute("formAction", formAction);
        if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
            return "admin/khachhang-list :: content";
        }
        return "layout/admin-layout";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("khachHang") KhachHang khachHang, BindingResult result,
                        @RequestParam(required = false) String q,
                        @RequestParam(required = false) String filterTrangThai,
                        Model model,
                        RedirectAttributes redirect,
                        @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        if (!result.hasErrors()) {
            rejectDuplicateContact(khachHang, result);
        }
        if (result.hasErrors()) {
            Boolean filterTrangThaiBool = parseTrangThai(filterTrangThai);
            Pageable pageable = PageRequest.of(0, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id"));
            Page<KhachHang> pageResult = khachHangService.searchPage(q, filterTrangThaiBool, pageable);
            model.addAttribute("title", "Khách hàng");
            model.addAttribute("content", "admin/khachhang-list");
            model.addAttribute("list", pageResult.getContent());
            model.addAttribute("page", pageResult);
            model.addAttribute("searchKeyword", q != null ? q : "");
            model.addAttribute("filterTrangThai", filterTrangThai != null ? filterTrangThai : "");
            model.addAttribute("chucVuList", chucVuService.getAll());
            model.addAttribute("formAction", "/admin/khach-hang");
            if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
                return "admin/khachhang-list :: content";
            }
            return "layout/admin-layout";
        }
        khachHangService.create(khachHang);
        if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
            Boolean filterTrangThaiBool = parseTrangThai(filterTrangThai);
            Pageable pageable = PageRequest.of(0, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id"));
            Page<KhachHang> pageResult = khachHangService.searchPage(q, filterTrangThaiBool, pageable);
            model.addAttribute("title", "Khách hàng");
            model.addAttribute("content", "admin/khachhang-list");
            model.addAttribute("list", pageResult.getContent());
            model.addAttribute("page", pageResult);
            model.addAttribute("searchKeyword", q != null ? q : "");
            model.addAttribute("filterTrangThai", filterTrangThai != null ? filterTrangThai : "");
            model.addAttribute("khachHang", new KhachHang());
            model.addAttribute("chucVuList", chucVuService.getAll());
            model.addAttribute("formAction", "/admin/khach-hang");
            model.addAttribute("message", "Thêm khách hàng mới thành công!");
            return "admin/khachhang-list :: content";
        }
        redirect.addFlashAttribute("message", "Thêm khách hàng mới thành công!");
        redirect.addAttribute("page", 0);
        if (q != null && !q.isBlank()) redirect.addAttribute("q", q);
        if (filterTrangThai != null && !filterTrangThai.isBlank()) redirect.addAttribute("trangThai", filterTrangThai);
        return "redirect:/admin/khach-hang#listKhachHang";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id, @RequestParam(defaultValue = "0") int page,
                         @RequestParam(required = false) String q,
                         @RequestParam(required = false) String filterTrangThai,
                         @Valid @ModelAttribute("khachHang") KhachHang khachHang, BindingResult result,
                         Model model,
                         RedirectAttributes redirect,
                         @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        if (!result.hasErrors()) {
            rejectDuplicateContact(khachHang, result);
        }
        if (result.hasErrors()) {
            Boolean filterTrangThaiBool = parseTrangThai(filterTrangThai);
            Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id"));
            Page<KhachHang> pageResult = khachHangService.searchPage(q, filterTrangThaiBool, pageable);
            model.addAttribute("title", "Khách hàng");
            model.addAttribute("content", "admin/khachhang-list");
            model.addAttribute("list", pageResult.getContent());
            model.addAttribute("page", pageResult);
            model.addAttribute("searchKeyword", q != null ? q : "");
            model.addAttribute("filterTrangThai", filterTrangThai != null ? filterTrangThai : "");
            model.addAttribute("chucVuList", chucVuService.getAll());
            String formAction = "/admin/khach-hang/" + id + "?page=" + page;
            if (q != null && !q.isBlank()) formAction += "&q=" + URLEncoder.encode(q, StandardCharsets.UTF_8);
            if (filterTrangThai != null && !filterTrangThai.isBlank()) formAction += "&filterTrangThai=" + URLEncoder.encode(filterTrangThai, StandardCharsets.UTF_8);
            model.addAttribute("formAction", formAction);
            if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
                return "admin/khachhang-list :: content";
            }
            return "layout/admin-layout";
        }
        khachHangService.update(id, khachHang);
        if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
            Boolean filterTrangThaiBool = parseTrangThai(filterTrangThai);
            Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id"));
            Page<KhachHang> pageResult = khachHangService.searchPage(q, filterTrangThaiBool, pageable);
            model.addAttribute("title", "Khách hàng");
            model.addAttribute("content", "admin/khachhang-list");
            model.addAttribute("list", pageResult.getContent());
            model.addAttribute("page", pageResult);
            model.addAttribute("searchKeyword", q != null ? q : "");
            model.addAttribute("filterTrangThai", filterTrangThai != null ? filterTrangThai : "");
            model.addAttribute("khachHang", new KhachHang());
            model.addAttribute("chucVuList", chucVuService.getAll());
            String formAction = "/admin/khach-hang";
            model.addAttribute("formAction", formAction);
            model.addAttribute("message", "Cập nhật khách hàng thành công.");
            return "admin/khachhang-list :: content";
        }
        redirect.addFlashAttribute("message", "Cập nhật khách hàng thành công.");
        redirect.addAttribute("page", page);
        if (q != null && !q.isBlank()) redirect.addAttribute("q", q);
        if (filterTrangThai != null && !filterTrangThai.isBlank()) redirect.addAttribute("trangThai", filterTrangThai);
        return "redirect:/admin/khach-hang#listKhachHang";
    }

    @PostMapping("/{id}/xoa")
    public String delete(@PathVariable Integer id, @RequestParam(defaultValue = "0") int page,
                         @RequestParam(required = false) String q,
                         @RequestParam(required = false) String trangThai,
                         Model model,
                         RedirectAttributes redirect,
                         @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        khachHangService.delete(id);
        if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
            Boolean filterTrangThaiBool = parseTrangThai(trangThai);
            Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id"));
            Page<KhachHang> pageResult = khachHangService.searchPage(q, filterTrangThaiBool, pageable);
            model.addAttribute("title", "Khách hàng");
            model.addAttribute("content", "admin/khachhang-list");
            model.addAttribute("list", pageResult.getContent());
            model.addAttribute("page", pageResult);
            model.addAttribute("searchKeyword", q != null ? q : "");
            model.addAttribute("filterTrangThai", trangThai != null ? trangThai : "");
            model.addAttribute("khachHang", new KhachHang());
            model.addAttribute("chucVuList", chucVuService.getAll());
            model.addAttribute("formAction", "/admin/khach-hang");
            model.addAttribute("message", "Xóa khách hàng thành công.");
            return "admin/khachhang-list :: content";
        }
        redirect.addFlashAttribute("message", "Xóa khách hàng thành công.");
        redirect.addAttribute("page", page);
        if (q != null && !q.isBlank()) redirect.addAttribute("q", q);
        if (trangThai != null && !trangThai.isBlank()) redirect.addAttribute("trangThai", trangThai);
        return "redirect:/admin/khach-hang#listKhachHang";
    }

    @PostMapping("/{id}/trang-thai")
    public String toggleTrangThai(@PathVariable Integer id, @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(required = false) String q,
                                  @RequestParam(required = false) String trangThai,
                                  Model model,
                                  RedirectAttributes redirect,
                                  @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        khachHangService.toggleTrangThai(id);
        if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
            Boolean filterTrangThaiBool = parseTrangThai(trangThai);
            Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id"));
            Page<KhachHang> pageResult = khachHangService.searchPage(q, filterTrangThaiBool, pageable);
            model.addAttribute("title", "Khách hàng");
            model.addAttribute("content", "admin/khachhang-list");
            model.addAttribute("list", pageResult.getContent());
            model.addAttribute("page", pageResult);
            model.addAttribute("searchKeyword", q != null ? q : "");
            model.addAttribute("filterTrangThai", trangThai != null ? trangThai : "");
            model.addAttribute("khachHang", new KhachHang());
            model.addAttribute("chucVuList", chucVuService.getAll());
            model.addAttribute("formAction", "/admin/khach-hang");
            model.addAttribute("message", "Đã cập nhật trạng thái.");
            return "admin/khachhang-list :: content";
        }
        redirect.addFlashAttribute("message", "Đã cập nhật trạng thái.");
        redirect.addAttribute("page", page);
        if (q != null && !q.isBlank()) redirect.addAttribute("q", q);
        if (trangThai != null && !trangThai.isBlank()) redirect.addAttribute("trangThai", trangThai);
        return "redirect:/admin/khach-hang#listKhachHang";
    }
}
