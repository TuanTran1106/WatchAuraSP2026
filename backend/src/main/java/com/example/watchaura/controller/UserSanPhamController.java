package com.example.watchaura.controller;

import com.example.watchaura.dto.SanPhamChiTietDTO;
import com.example.watchaura.dto.SanPhamDTO;
import com.example.watchaura.service.DanhMucService;
import com.example.watchaura.service.SanPhamChiTietService;
import com.example.watchaura.service.SanPhamService;
import com.example.watchaura.service.ThuongHieuService;
import lombok.RequiredArgsConstructor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/san-pham")
@RequiredArgsConstructor
public class UserSanPhamController {

    private static final int PAGE_SIZE = 12;

    private final SanPhamService sanPhamService;
    private final SanPhamChiTietService sanPhamChiTietService;
    private final DanhMucService danhMucService;
    private final ThuongHieuService thuongHieuService;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Integer danhMucId,
                       @RequestParam(required = false) Integer thuongHieuId,
                       Model model) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<SanPhamDTO> pageResult;
        if (danhMucId != null || thuongHieuId != null) {
            String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim().toLowerCase() : null;
            List<SanPhamDTO> all = sanPhamService.getAllSanPham().stream()
                    .filter(sp -> Boolean.TRUE.equals(sp.getTrangThai()))
                    .filter(sp -> kw == null || (sp.getTenSanPham() != null && sp.getTenSanPham().toLowerCase().contains(kw)))
                    .filter(sp -> danhMucId == null || (sp.getIdDanhMuc() != null && sp.getIdDanhMuc().equals(danhMucId)))
                    .filter(sp -> thuongHieuId == null || (sp.getIdThuongHieu() != null && sp.getIdThuongHieu().equals(thuongHieuId)))
                    .collect(java.util.stream.Collectors.toList());
            int start = page * PAGE_SIZE;
            int end = Math.min(start + PAGE_SIZE, all.size());
            List<SanPhamDTO> content = start < all.size() ? all.subList(start, end) : List.of();
            pageResult = new org.springframework.data.domain.PageImpl<>(content, pageable, all.size());
        } else {
            pageResult = sanPhamService.searchPage(keyword, true, pageable);
        }

        List<SanPhamDTO> content = pageResult.getContent();
        Map<Integer, Integer> firstVariantIdByProductId = new HashMap<>();
        for (SanPhamDTO sp : content) {
            if (sp.getId() != null) {
                sanPhamChiTietService.getSanPhamChiTietBySanPhamId(sp.getId()).stream()
                        .filter(v -> Boolean.TRUE.equals(v.getTrangThai()) && v.getSoLuongTon() != null && v.getSoLuongTon() > 0)
                        .findFirst()
                        .ifPresent(v -> firstVariantIdByProductId.put(sp.getId(), v.getId()));
            }
        }

        model.addAttribute("title", "Sản phẩm - WatchAura");
        model.addAttribute("content", "user/sanpham-list :: content");
        model.addAttribute("list", content);
        model.addAttribute("pageResult", pageResult);
        model.addAttribute("firstVariantIdByProductId", firstVariantIdByProductId);
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("danhMucId", danhMucId);
        model.addAttribute("thuongHieuId", thuongHieuId);
        model.addAttribute("danhMucList", danhMucService.getAll());
        model.addAttribute("thuongHieuList", thuongHieuService.getAll());
        return "layout/user-layout";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        SanPhamDTO sp = sanPhamService.getSanPhamById(id);
        if (sp.getTrangThai() == null || !sp.getTrangThai()) {
            return "redirect:/san-pham";
        }
        List<SanPhamChiTietDTO> variants = sanPhamChiTietService.getSanPhamChiTietBySanPhamId(id).stream()
                .filter(v -> Boolean.TRUE.equals(v.getTrangThai()) && v.getSoLuongTon() != null && v.getSoLuongTon() > 0)
                .collect(Collectors.toList());
        model.addAttribute("title", sp.getTenSanPham() + " - WatchAura");
        model.addAttribute("content", "user/sanpham-detail :: content");
        model.addAttribute("sp", sp);
        model.addAttribute("variants", variants);
        model.addAttribute("danhMucList", danhMucService.getAll());
        model.addAttribute("thuongHieuList", thuongHieuService.getAll());
        return "layout/user-layout";
    }
}
