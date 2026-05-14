package com.example.watchaura.controller;

import com.example.watchaura.annotation.RequiresRole;
import com.example.watchaura.dto.KhuyenMaiRequest;
import com.example.watchaura.entity.KhuyenMai;
import com.example.watchaura.service.DanhMucService;
import com.example.watchaura.service.KhuyenMaiService;
import com.example.watchaura.validator.KhuyenMaiValidator;
import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/khuyen-mai")
@RequiredArgsConstructor
@RequiresRole({"Admin", "Quản lý"})
public class KhuyenMaiController {

    private final KhuyenMaiService khuyenMaiService;
    private final DanhMucService danhMucService;
    private final KhuyenMaiValidator khuyenMaiValidator;

    private static final int PAGE_SIZE = 6;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                      @RequestParam(required = false) String q,
                      @RequestParam(required = false) String trangThai,
                      @RequestParam(required = false) LocalDate fromDate,
                      @RequestParam(required = false) LocalDate toDate,
                      @RequestParam(required = false) String phamViApDung,
                      Model model,
                      @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        Boolean filterTrangThai = parseTrangThai(trangThai);
        KhuyenMai.PhamViApDung filterPhamVi = parsePhamViApDung(phamViApDung);
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").descending());
        Page<KhuyenMai> pageResult = khuyenMaiService.searchPage(q, filterTrangThai, fromDate, toDate, filterPhamVi, pageable);
        fillListModel(model, pageResult, q, trangThai, fromDate, toDate, phamViApDung);
        model.addAttribute("khuyenMai", new KhuyenMaiRequest());
        model.addAttribute("formAction", "/admin/khuyen-mai");
        attachDanhMucList(model);
        if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
            return "admin/khuyenmai-list :: content";
        }
        return "layout/admin-layout";
    }

    private void attachDanhMucList(Model model) {
        var list = danhMucService.getAll();
        model.addAttribute("danhMucList", list);
        model.addAttribute("tenDanhMucList", list.stream()
                .map(dm -> dm.getTenDanhMuc())
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
    }

    private void fillListModel(Model model,
                               Page<KhuyenMai> pageResult,
                               String q,
                               String trangThai,
                               LocalDate fromDate,
                               LocalDate toDate,
                               String phamViApDung) {
        model.addAttribute("title", "Khuyến mãi");
        model.addAttribute("content", "admin/khuyenmai-list");
        model.addAttribute("list", pageResult.getContent());
        model.addAttribute("page", pageResult);
        model.addAttribute("searchKeyword", q != null ? q : "");
        model.addAttribute("filterTrangThai", trangThai != null ? trangThai : "");
        model.addAttribute("fromDate", fromDate);
        model.addAttribute("toDate", toDate);
        model.addAttribute("filterPhamViApDung", phamViApDung != null ? phamViApDung : "");
    }

    private Boolean parseTrangThai(String trangThai) {
        if (trangThai == null || trangThai.isBlank()) return null;
        if ("true".equalsIgnoreCase(trangThai)) return true;
        if ("false".equalsIgnoreCase(trangThai)) return false;
        return null;
    }

    private KhuyenMai.PhamViApDung parsePhamViApDung(String phamViApDung) {
        if (phamViApDung == null || phamViApDung.isBlank()) {
            return null;
        }
        try {
            return KhuyenMai.PhamViApDung.valueOf(phamViApDung.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private LocalDate parseLocalDateOrNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value.trim());
        } catch (Exception ex) {
            return null;
        }
    }

    private KhuyenMai toEntity(KhuyenMaiRequest req) {
        KhuyenMai km = new KhuyenMai();
        km.setId(req.getId());
        km.setMaKhuyenMai(req.getMaKhuyenMai() != null ? req.getMaKhuyenMai().trim() : null);
        km.setTenChuongTrinh(req.getTenChuongTrinh());
        km.setMoTa(req.getMoTa());
        km.setDanhMucApDung(req.getDanhMucApDung());
        km.setLoaiGiam(req.getLoaiGiam());
        km.setGiaTriGiam(req.getGiaTriGiam());
        km.setGiamToiDa(req.getGiamToiDa());
        km.setNgayBatDau(req.getNgayBatDau());
        km.setNgayKetThuc(req.getNgayKetThuc());
        km.setDonToiThieu(req.getDonToiThieu());
        km.setGioiHanLuotDung(req.getGioiHanLuotDung());
        km.setSoLuotDaDung(req.getSoLuotDaDung());
        km.setPhamViApDung(req.getPhamViApDung());
        km.setTrangThai(req.getTrangThai());
        return km;
    }

    private KhuyenMaiRequest toRequest(KhuyenMai km) {
        KhuyenMaiRequest req = new KhuyenMaiRequest();
        if (km == null) return req;
        req.setId(km.getId());
        req.setMaKhuyenMai(km.getMaKhuyenMai());
        req.setTenChuongTrinh(km.getTenChuongTrinh());
        req.setMoTa(km.getMoTa());
        req.setDanhMucApDung(km.getDanhMucApDung());
        req.setLoaiGiam(km.getLoaiGiam());
        req.setGiaTriGiam(km.getGiaTriGiam());
        req.setGiamToiDa(km.getGiamToiDa());
        req.setNgayBatDau(km.getNgayBatDau());
        req.setNgayKetThuc(km.getNgayKetThuc());
        req.setDonToiThieu(km.getDonToiThieu());
        req.setGioiHanLuotDung(km.getGioiHanLuotDung());
        req.setSoLuotDaDung(km.getSoLuotDaDung());
        req.setPhamViApDung(km.getPhamViApDung());
        req.setTrangThai(km.getTrangThai());
        return req;
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
                           @RequestParam(required = false) LocalDate fromDate,
                           @RequestParam(required = false) LocalDate toDate,
                           @RequestParam(required = false) String phamViApDung,
                           Model model,
                           @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        Boolean filterTrangThai = parseTrangThai(trangThai);
        KhuyenMai.PhamViApDung filterPhamVi = parsePhamViApDung(phamViApDung);
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").descending());
        Page<KhuyenMai> pageResult = khuyenMaiService.searchPage(q, filterTrangThai, fromDate, toDate, filterPhamVi, pageable);
        KhuyenMai khuyenMai = khuyenMaiService.findById(id);
        if (khuyenMai == null) {
            return "redirect:/admin/khuyen-mai";
        }
        fillListModel(model, pageResult, q, trangThai, fromDate, toDate, phamViApDung);
        model.addAttribute("khuyenMai", toRequest(khuyenMai));
        String formAction = "/admin/khuyen-mai/" + id + "?page=" + page;
        if (q != null && !q.isBlank()) formAction += "&q=" + URLEncoder.encode(q, StandardCharsets.UTF_8);
        if (trangThai != null && !trangThai.isBlank()) formAction += "&filterTrangThai=" + URLEncoder.encode(trangThai, StandardCharsets.UTF_8);
        if (fromDate != null) formAction += "&fromDate=" + URLEncoder.encode(fromDate.toString(), StandardCharsets.UTF_8);
        if (toDate != null) formAction += "&toDate=" + URLEncoder.encode(toDate.toString(), StandardCharsets.UTF_8);
        if (phamViApDung != null && !phamViApDung.isBlank()) formAction += "&phamViApDung=" + URLEncoder.encode(phamViApDung, StandardCharsets.UTF_8);
        model.addAttribute("formAction", formAction);
        attachDanhMucList(model);
        if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
            return "admin/khuyenmai-list :: content";
        }
        return "layout/admin-layout";
    }

    @PostMapping
    @Validated(Default.class)
    public String create(@Valid @ModelAttribute("khuyenMai") KhuyenMaiRequest khuyenMai,
                         BindingResult result,
                         @RequestParam(required = false) String q,
                         @RequestParam(required = false) String filterTrangThai,
                         @RequestParam(required = false) String fromDate,
                         @RequestParam(required = false) String toDate,
                         @RequestParam(required = false) String phamViApDung,
                         Model model,
                         RedirectAttributes redirect,
                         @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        LocalDate fromDateParsed = parseLocalDateOrNull(fromDate);
        LocalDate toDateParsed = parseLocalDateOrNull(toDate);
        KhuyenMai.PhamViApDung filterPhamVi = parsePhamViApDung(phamViApDung);
        Boolean filterTrangThaiBool = parseTrangThai(filterTrangThai);

        if (khuyenMai.getMaKhuyenMai() != null) {
            khuyenMai.setMaKhuyenMai(khuyenMai.getMaKhuyenMai().trim());
        }

        khuyenMaiValidator.validate(khuyenMai, result);
        if (khuyenMai.getMaKhuyenMai() != null && !khuyenMai.getMaKhuyenMai().isBlank()
                && khuyenMaiService.existsByMaKhuyenMai(khuyenMai.getMaKhuyenMai())) {
            result.rejectValue("maKhuyenMai", "duplicate", "Mã khuyến mãi đã tồn tại.");
        }

        Pageable pageable = PageRequest.of(0, PAGE_SIZE, Sort.by("id").descending());
        if (result.hasErrors()) {
            Page<KhuyenMai> pageResult = khuyenMaiService.searchPage(q, filterTrangThaiBool, fromDateParsed, toDateParsed, filterPhamVi, pageable);
            fillListModel(model, pageResult, q, filterTrangThai, fromDateParsed, toDateParsed, phamViApDung);
            model.addAttribute("formAction", "/admin/khuyen-mai");
            attachDanhMucList(model);
            if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
                return "admin/khuyenmai-list :: content";
            }
            return "layout/admin-layout";
        }

        khuyenMaiService.save(toEntity(khuyenMai));
        if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
            Page<KhuyenMai> pageResult = khuyenMaiService.searchPage(q, filterTrangThaiBool, fromDateParsed, toDateParsed, filterPhamVi, pageable);
            fillListModel(model, pageResult, q, filterTrangThai, fromDateParsed, toDateParsed, phamViApDung);
            model.addAttribute("khuyenMai", new KhuyenMaiRequest());
            model.addAttribute("formAction", "/admin/khuyen-mai");
            model.addAttribute("message", "Thêm khuyến mãi thành công.");
            attachDanhMucList(model);
            return "admin/khuyenmai-list :: content";
        }

        redirect.addFlashAttribute("message", "Thêm khuyến mãi thành công.");
        if (q != null && !q.isBlank()) redirect.addAttribute("q", q);
        if (filterTrangThai != null && !filterTrangThai.isBlank()) redirect.addAttribute("trangThai", filterTrangThai);
        if (fromDateParsed != null) redirect.addAttribute("fromDate", fromDateParsed);
        if (toDateParsed != null) redirect.addAttribute("toDate", toDateParsed);
        if (phamViApDung != null && !phamViApDung.isBlank()) redirect.addAttribute("phamViApDung", phamViApDung);
        return "redirect:/admin/khuyen-mai#listKhuyenMai";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(required = false) String q,
                         @RequestParam(required = false) String filterTrangThai,
                         @RequestParam(required = false) String fromDate,
                         @RequestParam(required = false) String toDate,
                         @RequestParam(required = false) String phamViApDung,
                         @Valid @ModelAttribute("khuyenMai") KhuyenMaiRequest khuyenMai,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirect,
                         @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        LocalDate fromDateParsed = parseLocalDateOrNull(fromDate);
        LocalDate toDateParsed = parseLocalDateOrNull(toDate);
        KhuyenMai.PhamViApDung filterPhamVi = parsePhamViApDung(phamViApDung);
        Boolean filterTrangThaiBool = parseTrangThai(filterTrangThai);

        if (khuyenMai.getMaKhuyenMai() != null) {
            khuyenMai.setMaKhuyenMai(khuyenMai.getMaKhuyenMai().trim());
        }

        khuyenMaiValidator.validate(khuyenMai, result);
        if (khuyenMai.getMaKhuyenMai() != null && !khuyenMai.getMaKhuyenMai().isBlank()
                && khuyenMaiService.existsByMaKhuyenMaiAndIdNot(khuyenMai.getMaKhuyenMai(), id)) {
            result.rejectValue("maKhuyenMai", "duplicate", "Mã khuyến mãi đã tồn tại.");
        }

        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").descending());
        if (result.hasErrors()) {
            Page<KhuyenMai> pageResult = khuyenMaiService.searchPage(q, filterTrangThaiBool, fromDateParsed, toDateParsed, filterPhamVi, pageable);
            fillListModel(model, pageResult, q, filterTrangThai, fromDateParsed, toDateParsed, phamViApDung);
            model.addAttribute("khuyenMai", khuyenMai);
            String formAction = "/admin/khuyen-mai/" + id + "?page=" + page;
            if (q != null && !q.isBlank()) formAction += "&q=" + URLEncoder.encode(q, StandardCharsets.UTF_8);
            if (filterTrangThai != null && !filterTrangThai.isBlank()) formAction += "&filterTrangThai=" + URLEncoder.encode(filterTrangThai, StandardCharsets.UTF_8);
            if (fromDateParsed != null) formAction += "&fromDate=" + URLEncoder.encode(fromDateParsed.toString(), StandardCharsets.UTF_8);
            if (toDateParsed != null) formAction += "&toDate=" + URLEncoder.encode(toDateParsed.toString(), StandardCharsets.UTF_8);
            if (phamViApDung != null && !phamViApDung.isBlank()) formAction += "&phamViApDung=" + URLEncoder.encode(phamViApDung, StandardCharsets.UTF_8);
            model.addAttribute("formAction", formAction);
            attachDanhMucList(model);
            if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
                return "admin/khuyenmai-list :: content";
            }
            return "layout/admin-layout";
        }

        khuyenMaiService.update(id, toEntity(khuyenMai));
        if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
            Page<KhuyenMai> pageResult = khuyenMaiService.searchPage(q, filterTrangThaiBool, fromDateParsed, toDateParsed, filterPhamVi, pageable);
            fillListModel(model, pageResult, q, filterTrangThai, fromDateParsed, toDateParsed, phamViApDung);
            model.addAttribute("khuyenMai", new KhuyenMaiRequest());
            model.addAttribute("formAction", "/admin/khuyen-mai");
            model.addAttribute("message", "Cập nhật khuyến mãi thành công.");
            attachDanhMucList(model);
            return "admin/khuyenmai-list :: content";
        }

        redirect.addFlashAttribute("message", "Cập nhật khuyến mãi thành công.");
        redirect.addAttribute("page", page);
        if (q != null && !q.isBlank()) redirect.addAttribute("q", q);
        if (filterTrangThai != null && !filterTrangThai.isBlank()) redirect.addAttribute("trangThai", filterTrangThai);
        if (fromDateParsed != null) redirect.addAttribute("fromDate", fromDateParsed);
        if (toDateParsed != null) redirect.addAttribute("toDate", toDateParsed);
        if (phamViApDung != null && !phamViApDung.isBlank()) redirect.addAttribute("phamViApDung", phamViApDung);
        return "redirect:/admin/khuyen-mai#listKhuyenMai";
    }

    @PostMapping("/{id}/xoa")
    public String delete(@PathVariable Integer id,
                        @RequestParam(defaultValue = "0") int page,
                        @RequestParam(required = false) String q,
                        @RequestParam(required = false) String trangThai,
                        @RequestParam(required = false) String fromDate,
                        @RequestParam(required = false) String toDate,
                        @RequestParam(required = false) String phamViApDung,
                        Model model,
                        RedirectAttributes redirect,
                        @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        LocalDate fromDateParsed = parseLocalDateOrNull(fromDate);
        LocalDate toDateParsed = parseLocalDateOrNull(toDate);
        KhuyenMai.PhamViApDung filterPhamVi = parsePhamViApDung(phamViApDung);
        Boolean filterTrangThaiBool = parseTrangThai(trangThai);

        khuyenMaiService.deactivate(id);

        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").descending());
        if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
            Page<KhuyenMai> pageResult = khuyenMaiService.searchPage(q, filterTrangThaiBool, fromDateParsed, toDateParsed, filterPhamVi, pageable);
            fillListModel(model, pageResult, q, trangThai, fromDateParsed, toDateParsed, phamViApDung);
            model.addAttribute("khuyenMai", new KhuyenMaiRequest());
            model.addAttribute("formAction", "/admin/khuyen-mai");
            model.addAttribute("message", "Đã ngừng kích hoạt khuyến mãi.");
            attachDanhMucList(model);
            return "admin/khuyenmai-list :: content";
        }

        redirect.addFlashAttribute("message", "Đã ngừng kích hoạt khuyến mãi.");
        redirect.addAttribute("page", page);
        if (q != null && !q.isBlank()) redirect.addAttribute("q", q);
        if (trangThai != null && !trangThai.isBlank()) redirect.addAttribute("trangThai", trangThai);
        if (fromDateParsed != null) redirect.addAttribute("fromDate", fromDateParsed);
        if (toDateParsed != null) redirect.addAttribute("toDate", toDateParsed);
        if (phamViApDung != null && !phamViApDung.isBlank()) redirect.addAttribute("phamViApDung", phamViApDung);
        return "redirect:/admin/khuyen-mai#listKhuyenMai";
    }

    @PostMapping("/{id}/trang-thai")
    public String toggleTrangThai(@PathVariable Integer id,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(required = false) String q,
                                 @RequestParam(required = false) String trangThai,
                                 @RequestParam(required = false) String fromDate,
                                 @RequestParam(required = false) String toDate,
                                 @RequestParam(required = false) String phamViApDung,
                                 Model model,
                                 RedirectAttributes redirect,
                                 @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        LocalDate fromDateParsed = parseLocalDateOrNull(fromDate);
        LocalDate toDateParsed = parseLocalDateOrNull(toDate);
        KhuyenMai.PhamViApDung filterPhamVi = parsePhamViApDung(phamViApDung);
        Boolean filterTrangThaiBool = parseTrangThai(trangThai);

        khuyenMaiService.toggleTrangThai(id);

        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").descending());
        if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
            Page<KhuyenMai> pageResult = khuyenMaiService.searchPage(q, filterTrangThaiBool, fromDateParsed, toDateParsed, filterPhamVi, pageable);
            fillListModel(model, pageResult, q, trangThai, fromDateParsed, toDateParsed, phamViApDung);
            model.addAttribute("khuyenMai", new KhuyenMaiRequest());
            model.addAttribute("formAction", "/admin/khuyen-mai");
            model.addAttribute("message", "Đã cập nhật trạng thái.");
            attachDanhMucList(model);
            return "admin/khuyenmai-list :: content";
        }

        redirect.addFlashAttribute("message", "Đã cập nhật trạng thái.");
        redirect.addAttribute("page", page);
        if (q != null && !q.isBlank()) redirect.addAttribute("q", q);
        if (trangThai != null && !trangThai.isBlank()) redirect.addAttribute("trangThai", trangThai);
        if (fromDateParsed != null) redirect.addAttribute("fromDate", fromDateParsed);
        if (toDateParsed != null) redirect.addAttribute("toDate", toDateParsed);
        if (phamViApDung != null && !phamViApDung.isBlank()) redirect.addAttribute("phamViApDung", phamViApDung);
        return "redirect:/admin/khuyen-mai#listKhuyenMai";
    }
}
