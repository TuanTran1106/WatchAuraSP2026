package com.example.watchaura.controller;

import com.example.watchaura.dto.SanPhamChiTietDTO;
import com.example.watchaura.dto.SanPhamChiTietRequest;
import com.example.watchaura.repository.ChatLieuDayRepository;
import com.example.watchaura.repository.KichThuocRepository;
import com.example.watchaura.repository.LoaiMayRepository;
import com.example.watchaura.repository.MauSacRepository;
import com.example.watchaura.service.SanPhamChiTietService;
import com.example.watchaura.service.SanPhamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/san-pham-chi-tiet")
@RequiredArgsConstructor
public class SanPhamChiTietController {

    private final SanPhamChiTietService sanPhamChiTietService;
    private final SanPhamService sanPhamService;
    private final MauSacRepository mauSacRepository;
    private final KichThuocRepository kichThuocRepository;
    private final ChatLieuDayRepository chatLieuDayRepository;
    private final LoaiMayRepository loaiMayRepository;

    @GetMapping
    public String list(Model model) {
        List<SanPhamChiTietDTO> list = sanPhamChiTietService.getAllSanPhamChiTiet();
        model.addAttribute("title", "Sản phẩm chi tiết");
        model.addAttribute("content", "admin/sanphamchitiet-list");
        model.addAttribute("list", list);
        return "layout/admin-layout";
    }

    @GetMapping("/them")
    public String formCreate(Model model) {
        model.addAttribute("title", "Thêm sản phẩm chi tiết");
        model.addAttribute("content", "admin/sanphamchitiet-form");
        model.addAttribute("spct", new SanPhamChiTietRequest());
        model.addAttribute("sanPhamList", sanPhamService.getAllSanPham());
        model.addAttribute("mauSacList", mauSacRepository.findAll());
        model.addAttribute("kichThuocList", kichThuocRepository.findAll());
        model.addAttribute("chatLieuDayList", chatLieuDayRepository.findAll());
        model.addAttribute("loaiMayList", loaiMayRepository.findAll());
        model.addAttribute("formAction", "/admin/san-pham-chi-tiet");
        return "layout/admin-layout";
    }

    @GetMapping("/{id}/sua")
    public String formEdit(@PathVariable Integer id, Model model) {
        SanPhamChiTietDTO dto = sanPhamChiTietService.getSanPhamChiTietById(id);
        SanPhamChiTietRequest spct = new SanPhamChiTietRequest();
        spct.setIdSanPham(dto.getIdSanPham());
        spct.setIdMauSac(dto.getIdMauSac());
        spct.setIdKichThuoc(dto.getIdKichThuoc());
        spct.setIdChatLieuDay(dto.getIdChatLieuDay());
        spct.setIdLoaiMay(dto.getIdLoaiMay());
        spct.setSoLuongTon(dto.getSoLuongTon());
        spct.setGiaBan(dto.getGiaBan());
        spct.setDuongKinh(dto.getDuongKinh());
        spct.setDoChiuNuoc(dto.getDoChiuNuoc());
        spct.setBeRongDay(dto.getBeRongDay());
        spct.setTrongLuong(dto.getTrongLuong());
        spct.setTrangThai(dto.getTrangThai());
        model.addAttribute("title", "Sửa sản phẩm chi tiết");
        model.addAttribute("content", "admin/sanphamchitiet-form");
        model.addAttribute("spct", spct);
        model.addAttribute("spctId", id);
        model.addAttribute("sanPhamList", sanPhamService.getAllSanPham());
        model.addAttribute("mauSacList", mauSacRepository.findAll());
        model.addAttribute("kichThuocList", kichThuocRepository.findAll());
        model.addAttribute("chatLieuDayList", chatLieuDayRepository.findAll());
        model.addAttribute("loaiMayList", loaiMayRepository.findAll());
        model.addAttribute("formAction", "/admin/san-pham-chi-tiet/" + id);
        return "layout/admin-layout";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("spct") SanPhamChiTietRequest request, RedirectAttributes redirect) {
        sanPhamChiTietService.createSanPhamChiTiet(request);
        redirect.addFlashAttribute("message", "Thêm sản phẩm chi tiết thành công.");
        return "redirect:/admin/san-pham-chi-tiet";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id, @Valid @ModelAttribute("spct") SanPhamChiTietRequest request, RedirectAttributes redirect) {
        sanPhamChiTietService.updateSanPhamChiTiet(id, request);
        redirect.addFlashAttribute("message", "Cập nhật sản phẩm chi tiết thành công.");
        return "redirect:/admin/san-pham-chi-tiet";
    }

    @PostMapping("/{id}/xoa")
    public String delete(@PathVariable Integer id, RedirectAttributes redirect) {
        sanPhamChiTietService.deleteSanPhamChiTiet(id);
        redirect.addFlashAttribute("message", "Xóa sản phẩm chi tiết thành công.");
        return "redirect:/admin/san-pham-chi-tiet";
    }
}
