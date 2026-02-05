package com.example.watchaura.controller;

import com.example.watchaura.dto.SanPhamDTO;
import com.example.watchaura.dto.SanPhamRequest;
import com.example.watchaura.repository.ChatLieuDayRepository;
import com.example.watchaura.repository.KichThuocRepository;
import com.example.watchaura.repository.LoaiMayRepository;
import com.example.watchaura.repository.MauSacRepository;
import com.example.watchaura.service.DanhMucService;
import com.example.watchaura.service.FileUploadService;
import com.example.watchaura.service.SanPhamService;
import com.example.watchaura.service.ThuongHieuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/admin/san-pham")
@RequiredArgsConstructor
public class SanPhamController {

    private static final int PAGE_SIZE = 6;

    private final SanPhamService sanPhamService;
    private final FileUploadService fileUploadService;
    private final ThuongHieuService thuongHieuService;
    private final DanhMucService danhMucService;
    private final MauSacRepository mauSacRepository;
    private final KichThuocRepository kichThuocRepository;
    private final ChatLieuDayRepository chatLieuDayRepository;
    private final LoaiMayRepository loaiMayRepository;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                      @RequestParam(required = false) String q,
                      @RequestParam(required = false) String trangThai,
                      Model model) {
        Boolean filterTrangThai = parseTrangThai(trangThai);
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<SanPhamDTO> pageResult = sanPhamService.searchPage(q, filterTrangThai, pageable);
        model.addAttribute("title", "Sản phẩm");
        model.addAttribute("content", "admin/sanpham-list");
        model.addAttribute("list", pageResult.getContent());
        model.addAttribute("page", pageResult);
        model.addAttribute("searchKeyword", q != null ? q : "");
        model.addAttribute("filterTrangThai", trangThai != null ? trangThai : "");
        model.addAttribute("sanPham", new SanPhamRequest());
        model.addAttribute("thuongHieuList", thuongHieuService.getAll());
        model.addAttribute("danhMucList", danhMucService.getAll());
        model.addAttribute("mauSacList", mauSacRepository.findAll());
        model.addAttribute("kichThuocList", kichThuocRepository.findAll());
        model.addAttribute("chatLieuDayList", chatLieuDayRepository.findAll());
        model.addAttribute("loaiMayList", loaiMayRepository.findAll());
        model.addAttribute("formAction", "/admin/san-pham");
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
        return "redirect:/admin/san-pham";
    }

    @GetMapping("/{id}/sua")
    public String formEdit(@PathVariable Integer id,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(required = false) String q,
                           @RequestParam(required = false) String trangThai,
                           Model model) {
        Boolean filterTrangThai = parseTrangThai(trangThai);
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<SanPhamDTO> pageResult = sanPhamService.searchPage(q, filterTrangThai, pageable);
        SanPhamDTO dto = sanPhamService.getSanPhamById(id);
        SanPhamRequest sanPham = new SanPhamRequest();
        sanPham.setMaSanPham(dto.getMaSanPham());
        sanPham.setTenSanPham(dto.getTenSanPham());
        sanPham.setMoTa(dto.getMoTa());
        sanPham.setHinhAnh(dto.getHinhAnh());
        sanPham.setIdThuongHieu(dto.getIdThuongHieu());
        sanPham.setIdDanhMuc(dto.getIdDanhMuc());
        sanPham.setPhongCach(dto.getPhongCach());
        sanPham.setTrangThai(dto.getTrangThai());
        sanPham.setGiaBan(dto.getGiaBan());
        sanPham.setSoLuongTon(dto.getSoLuongTon());
        sanPham.setIdMauSac(dto.getIdMauSac());
        sanPham.setIdKichThuoc(dto.getIdKichThuoc());
        sanPham.setIdChatLieuDay(dto.getIdChatLieuDay());
        sanPham.setIdLoaiMay(dto.getIdLoaiMay());
        sanPham.setDuongKinh(dto.getDuongKinh());
        sanPham.setDoChiuNuoc(dto.getDoChiuNuoc());
        sanPham.setBeRongDay(dto.getBeRongDay());
        sanPham.setTrongLuong(dto.getTrongLuong());
        model.addAttribute("title", "Sản phẩm");
        model.addAttribute("content", "admin/sanpham-list");
        model.addAttribute("list", pageResult.getContent());
        model.addAttribute("page", pageResult);
        model.addAttribute("searchKeyword", q != null ? q : "");
        model.addAttribute("filterTrangThai", trangThai != null ? trangThai : "");
        model.addAttribute("sanPham", sanPham);
        model.addAttribute("sanPhamId", id);
        model.addAttribute("thuongHieuList", thuongHieuService.getAll());
        model.addAttribute("danhMucList", danhMucService.getAll());
        model.addAttribute("mauSacList", mauSacRepository.findAll());
        model.addAttribute("kichThuocList", kichThuocRepository.findAll());
        model.addAttribute("chatLieuDayList", chatLieuDayRepository.findAll());
        model.addAttribute("loaiMayList", loaiMayRepository.findAll());
        String formAction = "/admin/san-pham/" + id + "?page=" + page;
        if (q != null && !q.isBlank()) formAction += "&q=" + URLEncoder.encode(q, StandardCharsets.UTF_8);
        if (trangThai != null && !trangThai.isBlank()) formAction += "&trangThai=" + URLEncoder.encode(trangThai, StandardCharsets.UTF_8);
        model.addAttribute("formAction", formAction);
        return "layout/admin-layout";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("sanPham") SanPhamRequest request,
                         BindingResult result,
                         @RequestParam(value = "hinhAnhFile", required = false) MultipartFile hinhAnhFile,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(required = false) String q,
                         @RequestParam(required = false) String trangThai,
                         Model model, RedirectAttributes redirect) {
        Boolean filterTrangThai = parseTrangThai(trangThai);
        if (result.hasErrors()) {
            Pageable pageable = PageRequest.of(page, PAGE_SIZE);
            Page<SanPhamDTO> pageResult = sanPhamService.searchPage(q, filterTrangThai, pageable);
            model.addAttribute("title", "Sản phẩm");
            model.addAttribute("content", "admin/sanpham-list");
            model.addAttribute("list", pageResult.getContent());
            model.addAttribute("page", pageResult);
            model.addAttribute("searchKeyword", q != null ? q : "");
            model.addAttribute("filterTrangThai", trangThai != null ? trangThai : "");
            model.addAttribute("sanPham", request);
            model.addAttribute("thuongHieuList", thuongHieuService.getAll());
            model.addAttribute("danhMucList", danhMucService.getAll());
            model.addAttribute("mauSacList", mauSacRepository.findAll());
            model.addAttribute("kichThuocList", kichThuocRepository.findAll());
            model.addAttribute("chatLieuDayList", chatLieuDayRepository.findAll());
            model.addAttribute("loaiMayList", loaiMayRepository.findAll());
            model.addAttribute("formAction", "/admin/san-pham");
            return "layout/admin-layout";
        }
        if (hinhAnhFile != null && !hinhAnhFile.isEmpty()) {
            try {
                String path = fileUploadService.uploadFile(hinhAnhFile);
                request.setHinhAnh(path);
            } catch (Exception e) {
                Pageable pageable = PageRequest.of(page, PAGE_SIZE);
                Page<SanPhamDTO> pageResult = sanPhamService.searchPage(q, filterTrangThai, pageable);
                model.addAttribute("title", "Sản phẩm");
                model.addAttribute("content", "admin/sanpham-list");
                model.addAttribute("list", pageResult.getContent());
                model.addAttribute("page", pageResult);
                model.addAttribute("searchKeyword", q != null ? q : "");
                model.addAttribute("filterTrangThai", trangThai != null ? trangThai : "");
                model.addAttribute("sanPham", request);
                model.addAttribute("thuongHieuList", thuongHieuService.getAll());
                model.addAttribute("danhMucList", danhMucService.getAll());
                model.addAttribute("mauSacList", mauSacRepository.findAll());
                model.addAttribute("kichThuocList", kichThuocRepository.findAll());
                model.addAttribute("chatLieuDayList", chatLieuDayRepository.findAll());
                model.addAttribute("loaiMayList", loaiMayRepository.findAll());
                model.addAttribute("formAction", "/admin/san-pham");
                model.addAttribute("error", "Ảnh không hợp lệ: " + e.getMessage());
                return "layout/admin-layout";
            }
        }
        sanPhamService.createSanPham(request);
        redirect.addFlashAttribute("message", "Thêm sản phẩm thành công.");
        redirect.addAttribute("page", page);
        if (q != null && !q.isBlank()) redirect.addAttribute("q", q);
        if (trangThai != null && !trangThai.isBlank()) redirect.addAttribute("trangThai", trangThai);
        return "redirect:/admin/san-pham#listSanPham";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id,
                         @Valid @ModelAttribute("sanPham") SanPhamRequest request,
                         BindingResult result,
                         @RequestParam(value = "hinhAnhFile", required = false) MultipartFile hinhAnhFile,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(required = false) String q,
                         @RequestParam(required = false) String trangThai,
                         Model model, RedirectAttributes redirect) {
        Boolean filterTrangThai = parseTrangThai(trangThai);
        if (result.hasErrors()) {
            Pageable pageable = PageRequest.of(page, PAGE_SIZE);
            Page<SanPhamDTO> pageResult = sanPhamService.searchPage(q, filterTrangThai, pageable);
            model.addAttribute("title", "Sản phẩm");
            model.addAttribute("content", "admin/sanpham-list");
            model.addAttribute("list", pageResult.getContent());
            model.addAttribute("page", pageResult);
            model.addAttribute("searchKeyword", q != null ? q : "");
            model.addAttribute("filterTrangThai", trangThai != null ? trangThai : "");
            model.addAttribute("sanPham", request);
            model.addAttribute("sanPhamId", id);
            model.addAttribute("thuongHieuList", thuongHieuService.getAll());
            model.addAttribute("danhMucList", danhMucService.getAll());
            model.addAttribute("mauSacList", mauSacRepository.findAll());
            model.addAttribute("kichThuocList", kichThuocRepository.findAll());
            model.addAttribute("chatLieuDayList", chatLieuDayRepository.findAll());
            model.addAttribute("loaiMayList", loaiMayRepository.findAll());
            String formActionErr = "/admin/san-pham/" + id + "?page=" + page;
            if (q != null && !q.isBlank()) formActionErr += "&q=" + URLEncoder.encode(q, StandardCharsets.UTF_8);
            if (trangThai != null && !trangThai.isBlank()) formActionErr += "&trangThai=" + URLEncoder.encode(trangThai, StandardCharsets.UTF_8);
            model.addAttribute("formAction", formActionErr);
            return "layout/admin-layout";
        }
        if (hinhAnhFile != null && !hinhAnhFile.isEmpty()) {
            try {
                String path = fileUploadService.uploadFile(hinhAnhFile);
                request.setHinhAnh(path);
            } catch (Exception e) {
                Pageable pageable = PageRequest.of(page, PAGE_SIZE);
                Page<SanPhamDTO> pageResult = sanPhamService.searchPage(q, filterTrangThai, pageable);
                model.addAttribute("title", "Sản phẩm");
                model.addAttribute("content", "admin/sanpham-list");
                model.addAttribute("list", pageResult.getContent());
                model.addAttribute("page", pageResult);
                model.addAttribute("searchKeyword", q != null ? q : "");
                model.addAttribute("filterTrangThai", trangThai != null ? trangThai : "");
                model.addAttribute("sanPham", request);
                model.addAttribute("sanPhamId", id);
                model.addAttribute("thuongHieuList", thuongHieuService.getAll());
                model.addAttribute("danhMucList", danhMucService.getAll());
                model.addAttribute("mauSacList", mauSacRepository.findAll());
                model.addAttribute("kichThuocList", kichThuocRepository.findAll());
                model.addAttribute("chatLieuDayList", chatLieuDayRepository.findAll());
                model.addAttribute("loaiMayList", loaiMayRepository.findAll());
                String formActionErr2 = "/admin/san-pham/" + id + "?page=" + page;
                if (q != null && !q.isBlank()) formActionErr2 += "&q=" + URLEncoder.encode(q, StandardCharsets.UTF_8);
                if (trangThai != null && !trangThai.isBlank()) formActionErr2 += "&trangThai=" + URLEncoder.encode(trangThai, StandardCharsets.UTF_8);
                model.addAttribute("formAction", formActionErr2);
                model.addAttribute("error", "Ảnh không hợp lệ: " + e.getMessage());
                return "layout/admin-layout";
            }
        }
        sanPhamService.updateSanPham(id, request);
        redirect.addFlashAttribute("message", "Cập nhật sản phẩm thành công.");
        redirect.addAttribute("page", page);
        if (q != null && !q.isBlank()) redirect.addAttribute("q", q);
        if (trangThai != null && !trangThai.isBlank()) redirect.addAttribute("trangThai", trangThai);
        return "redirect:/admin/san-pham#listSanPham";
    }

    @PostMapping("/{id}/xoa")
    public String delete(@PathVariable Integer id,
                         @RequestParam(defaultValue = "0") int page,
                         @RequestParam(required = false) String q,
                         @RequestParam(required = false) String trangThai,
                         RedirectAttributes redirect) {
        sanPhamService.deleteSanPham(id);
        redirect.addFlashAttribute("message", "Xóa sản phẩm thành công.");
        redirect.addAttribute("page", page);
        if (q != null && !q.isBlank()) redirect.addAttribute("q", q);
        if (trangThai != null && !trangThai.isBlank()) redirect.addAttribute("trangThai", trangThai);
        return "redirect:/admin/san-pham#listSanPham";
    }

    @PostMapping("/{id}/trang-thai")
    public String toggleTrangThai(@PathVariable Integer id,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(required = false) String q,
                                  @RequestParam(required = false) String trangThai,
                                  RedirectAttributes redirect) {
        sanPhamService.toggleTrangThai(id);
        redirect.addFlashAttribute("message", "Đã cập nhật trạng thái sản phẩm.");
        redirect.addAttribute("page", page);
        if (q != null && !q.isBlank()) redirect.addAttribute("q", q);
        if (trangThai != null && !trangThai.isBlank()) redirect.addAttribute("trangThai", trangThai);
        return "redirect:/admin/san-pham#listSanPham";
    }

    @PostMapping("/upload")
    public String uploadImage(@RequestParam("file") MultipartFile file, RedirectAttributes redirect) {
        try {
            String filePath = fileUploadService.uploadFile(file);
            redirect.addFlashAttribute("message", "Upload thành công: " + filePath);
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/san-pham";
    }
}
