package com.example.watchaura.controller;

import com.example.watchaura.dto.SanPhamDTO;
import com.example.watchaura.service.BlogService;
import com.example.watchaura.service.DanhMucService;
import com.example.watchaura.service.KhuyenMaiService;
import com.example.watchaura.service.SanPhamService;
import com.example.watchaura.service.ThuongHieuService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class UsersController {

    private final SanPhamService sanPhamService;
    private final DanhMucService danhMucService;
    private final ThuongHieuService thuongHieuService;
    private final KhuyenMaiService khuyenMaiService;
    private final BlogService blogService;

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("title", "Trang chủ - WatchAura");
        model.addAttribute("content", "user/home :: content");

        List<SanPhamDTO> sanPhamTrangChu = sanPhamService.getSanPhamTrangChu(8);
        model.addAttribute("danhMucList", danhMucService.getAll());
        model.addAttribute("thuongHieuList", thuongHieuService.getAll());
        model.addAttribute("sanPhamTrangChu", sanPhamTrangChu);
        model.addAttribute("khuyenMaiDangChay", khuyenMaiService.getActivePromotions(LocalDateTime.now()));
        model.addAttribute("blogMoi", blogService.getRecentBlogs(3));

        return "layout/user-layout";
    }

    @GetMapping("/gio-hang")
    public String gioHang(Model model) {
        model.addAttribute("title", "Giỏ hàng - WatchAura");
        model.addAttribute("content", "user/gio-hang :: content");
        return "layout/user-layout";
    }

    @GetMapping("/tin-tuc")
    public String tinTuc(Model model) {
        model.addAttribute("title", "Tin tức - WatchAura");
        model.addAttribute("content", "user/tin-tuc :: content");
        model.addAttribute("blogMoi", blogService.getPage(PageRequest.of(0, 10, Sort.by("ngayDang").descending())).getContent());
        return "layout/user-layout";
    }

    @GetMapping("/lien-he")
    public String lienHe(Model model) {
        model.addAttribute("title", "Liên hệ - WatchAura");
        model.addAttribute("content", "user/lien-he :: content");
        return "layout/user-layout";
    }

    @GetMapping("/tin-tuc/{id}")
    public String tinTucDetail(@PathVariable Integer id, Model model) {
        model.addAttribute("blog", blogService.getById(id));
        model.addAttribute("title", "Tin tức - WatchAura");
        model.addAttribute("content", "user/tin-tuc-detail :: content");
        return "layout/user-layout";
    }

    @GetMapping("/test-header")
    public String testHeader() {
        return "fragments/header";
    }
}
