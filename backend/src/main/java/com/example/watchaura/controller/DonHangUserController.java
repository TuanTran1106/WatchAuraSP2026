package com.example.watchaura.controller;

import com.example.watchaura.dto.HoaDonDTO;
import com.example.watchaura.service.HoaDonService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

import static com.example.watchaura.controller.AuthController.SESSION_CURRENT_USER_ID;

@Controller
@RequiredArgsConstructor
public class DonHangUserController {

    private final HoaDonService hoaDonService;

    @GetMapping("/don-hang")
    public String danhSachDonHang(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) String thanhToan,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngay,
            Model model,
            HttpSession session) {

        Integer userId = (Integer) session.getAttribute(SESSION_CURRENT_USER_ID);

        if (userId == null) {
            return "redirect:/dang-nhap";
        }

        // Chuẩn hóa: chuỗi rỗng coi như không lọc (null) để JPQL IS NULL đúng
        String trangThaiParam = (trangThai != null && !trangThai.isBlank()) ? trangThai : null;
        String thanhToanParam = (thanhToan != null && !thanhToan.isBlank()) ? thanhToan : null;

        Pageable pageable = PageRequest.of(page, 6);

        Page<HoaDonDTO> pageHoaDon =
                hoaDonService.filterDonHang(userId, trangThaiParam, thanhToanParam, ngay, pageable);

        model.addAttribute("pageHoaDon", pageHoaDon);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("thanhToan", thanhToan);
        model.addAttribute("ngay", ngay);

        model.addAttribute("title", "Đơn hàng của tôi");
        model.addAttribute("content", "user/don-hang-user-list :: content");

        return "layout/user-layout";
    }

    @GetMapping("/don-hang/chi-tiet/{id}")
    public String chiTietDonHang(@PathVariable Integer id,
                                 HttpSession session,
                                 Model model) {

        Integer userId = (Integer) session.getAttribute(SESSION_CURRENT_USER_ID);

        if (userId == null) {
            return "redirect:/dang-nhap";
        }

        HoaDonDTO hoaDon = hoaDonService.getById(id);

        // Bảo mật: chỉ cho xem đơn của chính mình
        if (hoaDon.getKhachHangId() == null || !hoaDon.getKhachHangId().equals(userId)) {
            return "redirect:/don-hang";
        }

        model.addAttribute("hoaDon", hoaDon);
        model.addAttribute("title", "Chi tiết đơn hàng");
        model.addAttribute("content", "user/don-hang-user-detail");

        return "layout/user-layout";
    }
}
