package com.example.watchaura.controller;

import com.example.watchaura.dto.DanhGiaDTO;
import com.example.watchaura.dto.HoaDonDTO;
import com.example.watchaura.entity.DanhGia;
import com.example.watchaura.entity.KhachHang;
import com.example.watchaura.repository.DanhGiaRepository;
import com.example.watchaura.repository.KhachHangRepository;
import com.example.watchaura.service.DanhGiaService;
import com.example.watchaura.service.HoaDonService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class DanhGiaController {

    private final DanhGiaRepository danhGiaRepository;
    private final KhachHangRepository khachHangRepository;
    private final HoaDonService hoaDonService;
    private final DanhGiaService danhGiaService;

    /** Hiển trang đánh giá sản phẩm từ đơn hàng (mỗi sản phẩm trong đơn) */
    @GetMapping("/danh-gia/{hoaDonId}")
    public String trangDanhGia(@PathVariable Integer hoaDonId,
                               Model model,
                               HttpSession session) {
        Integer userId = (Integer) session.getAttribute("currentUserId");
        if (userId == null) {
            return "redirect:/dang-nhap";
        }

        HoaDonDTO hoaDon = hoaDonService.getById(hoaDonId);
        if (hoaDon == null || hoaDon.getKhachHangId() == null || !hoaDon.getKhachHangId().equals(userId)) {
            return "redirect:/don-hang";
        }

        // Lấy danh sách đánh giá của user cho các sản phẩm trong đơn hàng
        Map<Integer, DanhGiaDTO> userReviewsMap = new HashMap<>();
        if (hoaDon.getItems() != null) {
            for (var item : hoaDon.getItems()) {
                if (item.getSanPhamChiTietId() != null) {
                    DanhGiaDTO existingReview = danhGiaService.getUserReview(item.getSanPhamChiTietId(), userId);
                    if (existingReview != null) {
                        userReviewsMap.put(item.getSanPhamChiTietId(), existingReview);
                    }
                }
            }
        }

        model.addAttribute("hoaDon", hoaDon);
        model.addAttribute("userReviewsMap", userReviewsMap);
        model.addAttribute("title", "Đánh giá sản phẩm");
        model.addAttribute("content", "user/danh-gia :: content");
        return "layout/user-layout";
    }

    @PostMapping("/danh-gia")
    public String danhGia(
            @RequestParam Integer sanPhamChiTietId,
            @RequestParam Integer soSao,
            @RequestParam String noiDung,
            @RequestParam(required = false) Integer hoaDonId,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {

        Integer userId = (Integer) session.getAttribute("currentUserId");
        if (userId == null) {
            redirectAttributes.addFlashAttribute("error", "Vui lòng đăng nhập.");
            return "redirect:/dang-nhap";
        }

        // Kiểm tra user đã đánh giá sản phẩm này chưa
        if (danhGiaService.hasUserReviewed(sanPhamChiTietId, userId)) {
            redirectAttributes.addFlashAttribute("error", "Bạn đã đánh giá sản phẩm này rồi!");
            if (hoaDonId != null) {
                return "redirect:/danh-gia/" + hoaDonId;
            }
            return "redirect:/don-hang";
        }

        KhachHang kh = khachHangRepository.findById(userId).orElse(null);

        DanhGia dg = new DanhGia();
        dg.setKhachHang(kh);
        dg.setSoSao(soSao);
        dg.setNoiDung(noiDung);
        dg.setNgayDanhGia(LocalDateTime.now());
        dg.setIdSanPhamChiTiet(sanPhamChiTietId);
        danhGiaRepository.save(dg);

        redirectAttributes.addFlashAttribute("success", "Đánh giá sản phẩm thành công!");

        if (hoaDonId != null) {
            return "redirect:/danh-gia/" + hoaDonId;
        }
        return "redirect:/don-hang";
    }
}