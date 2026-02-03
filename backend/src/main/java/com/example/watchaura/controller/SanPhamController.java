package com.example.watchaura.controller;

import com.example.watchaura.dto.SanPhamDTO;
import com.example.watchaura.dto.SanPhamRequest;
import com.example.watchaura.service.DanhMucService;
import com.example.watchaura.service.FileUploadService;
import com.example.watchaura.service.SanPhamService;
import com.example.watchaura.service.ThuongHieuService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/san-pham")
@RequiredArgsConstructor
public class SanPhamController {

    private final SanPhamService sanPhamService;
    private final FileUploadService fileUploadService;
    private final ThuongHieuService thuongHieuService;
    private final DanhMucService danhMucService;

    @GetMapping
    public String list(Model model) {
        List<SanPhamDTO> list = sanPhamService.getAllSanPham();
        model.addAttribute("title", "Sản phẩm");
        model.addAttribute("content", "admin/sanpham-list");
        model.addAttribute("list", list);
        return "layout/admin-layout";
    }

    @GetMapping("/them")
    public String formCreate(Model model) {
        model.addAttribute("title", "Thêm sản phẩm");
        model.addAttribute("content", "admin/sanpham-form");
        model.addAttribute("sanPham", new SanPhamRequest());
        model.addAttribute("thuongHieuList", thuongHieuService.getAll());
        model.addAttribute("danhMucList", danhMucService.getAll());
        model.addAttribute("formAction", "/admin/san-pham");
        return "layout/admin-layout";
    }

    @GetMapping("/{id}/sua")
    public String formEdit(@PathVariable Integer id, Model model) {
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
        model.addAttribute("title", "Sửa sản phẩm");
        model.addAttribute("content", "admin/sanpham-form");
        model.addAttribute("sanPham", sanPham);
        model.addAttribute("sanPhamId", id);
        model.addAttribute("thuongHieuList", thuongHieuService.getAll());
        model.addAttribute("danhMucList", danhMucService.getAll());
        model.addAttribute("formAction", "/admin/san-pham/" + id);
        return "layout/admin-layout";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("sanPham") SanPhamRequest request, RedirectAttributes redirect) {
        sanPhamService.createSanPham(request);
        redirect.addFlashAttribute("message", "Thêm sản phẩm thành công.");
        return "redirect:/admin/san-pham";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id, @Valid @ModelAttribute("sanPham") SanPhamRequest request, RedirectAttributes redirect) {
        sanPhamService.updateSanPham(id, request);
        redirect.addFlashAttribute("message", "Cập nhật sản phẩm thành công.");
        return "redirect:/admin/san-pham";
    }

    @PostMapping("/{id}/xoa")
    public String delete(@PathVariable Integer id, RedirectAttributes redirect) {
        sanPhamService.deleteSanPham(id);
        redirect.addFlashAttribute("message", "Xóa sản phẩm thành công.");
        return "redirect:/admin/san-pham";
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
