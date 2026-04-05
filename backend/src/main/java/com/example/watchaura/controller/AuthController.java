package com.example.watchaura.controller;

import com.example.watchaura.dto.ChangePasswordRequest;
import com.example.watchaura.dto.RegisterRequest;
import com.example.watchaura.entity.KhachHang;
import com.example.watchaura.repository.HoaDonRepository;
import com.example.watchaura.service.KhachHangService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class AuthController {

    public static final String SESSION_CURRENT_USER_ID = "currentUserId";

    private final KhachHangService khachHangService;
    private final PasswordEncoder passwordEncoder;
    private final HoaDonRepository hoaDonRepository;

    @GetMapping("/dang-nhap")
    public String dangNhapPage(
            @RequestParam(value = "error", required = false) String error,
            Model model) {
        model.addAttribute("title", "Đăng nhập - WatchAura");
        model.addAttribute("content", "user/dang-nhap :: content");
        if (error != null) model.addAttribute("errorMessage", error);
        return "layout/user-layout";
    }

    @PostMapping("/dang-nhap")
    public String dangNhap(
            @RequestParam("email") String email,
            @RequestParam("matKhau") String matKhau,
            HttpSession session,
            RedirectAttributes redirect) {
        if (email == null || email.isBlank()) {
            redirect.addAttribute("error", "Vui lòng nhập email.");
            return "redirect:/dang-nhap";
        }
        Optional<KhachHang> opt = khachHangService.findByEmail(email.trim());
        if (opt.isEmpty()) {
            redirect.addAttribute("error", "Email hoặc mật khẩu không đúng.");
            return "redirect:/dang-nhap";
        }
        KhachHang kh = opt.get();
        if (Boolean.FALSE.equals(kh.getTrangThai())) {
            redirect.addAttribute("error", "Tài khoản đã bị khóa.");
            return "redirect:/dang-nhap";
        }
        if (matKhau == null || !passwordEncoder.matches(matKhau, kh.getMatKhau())) {
            redirect.addAttribute("error", "Email hoặc mật khẩu không đúng.");
            return "redirect:/dang-nhap";
        }
        session.setAttribute(SESSION_CURRENT_USER_ID, kh.getId());

        
        // Kiểm tra chức vụ để chuyển hướng
        String redirectUrl = "/";
        if (kh.getChucVu() != null) {
            String tenChucVu = kh.getChucVu().getTenChucVu();
            if (tenChucVu != null) {
                if (tenChucVu.equalsIgnoreCase("Nhân viên") || tenChucVu.equalsIgnoreCase("nhanvien")) {
                    redirectUrl = "/ban-hang";
                } else if (tenChucVu.equalsIgnoreCase("Admin") || tenChucVu.equalsIgnoreCase("Quản lý") 
                        || tenChucVu.equalsIgnoreCase("admin") || tenChucVu.equalsIgnoreCase("quanly")) {
                    redirectUrl = "/admin/san-pham";
                }
            }
        }
        
        redirect.addFlashAttribute("welcomeMessage", "Xin chào, " + kh.getTenNguoiDung() + "!");
        return "redirect:" + redirectUrl;
    }

    @GetMapping("/dang-xuat")
    public String dangXuat(HttpSession session, RedirectAttributes redirect) {
        session.invalidate();
        redirect.addFlashAttribute("infoMessage", "Bạn đã đăng xuất.");
        return "redirect:/home";
    }

    @GetMapping("/dang-ky")
    public String dangKyPage(
            @RequestParam(value = "ten", required = false) String ten,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "sdt", required = false) String sdt,
            Model model) {
        RegisterRequest registerRequest = new RegisterRequest();
        if (ten != null && !ten.isBlank()) registerRequest.setTenNguoiDung(ten);
        if (email != null && !email.isBlank()) registerRequest.setEmail(email);
        if (sdt != null && !sdt.isBlank()) registerRequest.setSdt(sdt);
        model.addAttribute("registerRequest", registerRequest);
        model.addAttribute("title", "Đăng ký - WatchAura");
        model.addAttribute("content", "user/dang-ky :: content");
        return "layout/user-layout";
    }

    @PostMapping("/dang-ky")
    @Transactional
    public String dangKy(
            @Valid @ModelAttribute("registerRequest") RegisterRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("title", "Đăng ký - WatchAura");
            model.addAttribute("content", "user/dang-ky :: content");
            return "layout/user-layout";
        }
        if (!request.getMatKhau().equals(request.getXacNhanMatKhau())) {
            model.addAttribute("title", "Đăng ký - WatchAura");
            model.addAttribute("content", "user/dang-ky :: content");
            model.addAttribute("passwordMismatch", "Mật khẩu và xác nhận mật khẩu không khớp.");
            return "layout/user-layout";
        }
        KhachHang khachHang = null;
        try {
            khachHang = khachHangService.registerKhachHang(
                    request.getTenNguoiDung(),
                    request.getEmail().trim(),
                    request.getSdt(),
                    request.getMatKhau(),
                    request.getNgaySinh(),
                    request.getGioiTinh()
            );
        } catch (RuntimeException e) {
            model.addAttribute("title", "Đăng ký - WatchAura");
            model.addAttribute("content", "user/dang-ky :: content");
            model.addAttribute("registerError", e.getMessage());
            return "layout/user-layout";
        }

        // Liên kết đơn hàng có cùng email với tài khoản mới
        if (khachHang != null) {
            int linked = hoaDonRepository.linkOrdersToCustomerByEmail(khachHang.getEmail(), khachHang.getId());
            if (linked > 0) {
                redirect.addFlashAttribute("infoMessage", "Đã liên kết " + linked + " đơn hàng với tài khoản của bạn.");
            }
        }

        redirect.addFlashAttribute("successMessage", "Đăng ký thành công. Vui lòng đăng nhập.");
        return "redirect:/dang-nhap";
    }

    /** Đổi mật khẩu đã gộp vào trang tài khoản: redirect về /nguoidung (khi đã đăng nhập) hoặc đăng nhập. */
    @GetMapping("/doi-mat-khau")
    public String doiMatKhauPage(HttpSession session, RedirectAttributes redirect) {
        Object userId = session.getAttribute(SESSION_CURRENT_USER_ID);
        if (!(userId instanceof Integer)) {
            redirect.addFlashAttribute("infoMessage", "Vui lòng đăng nhập để đổi mật khẩu.");
            return "redirect:/dang-nhap";
        }
        return "redirect:/nguoidung#doi-mat-khau";
    }

    @PostMapping("/doi-mat-khau")
    public String doiMatKhau(
            @Valid @ModelAttribute("changePasswordRequest") ChangePasswordRequest request,
            BindingResult result,
            HttpSession session,
            Model model,
            RedirectAttributes redirect) {
        Object userIdObj = session.getAttribute(SESSION_CURRENT_USER_ID);
        if (!(userIdObj instanceof Integer)) {
            redirect.addFlashAttribute("infoMessage", "Vui lòng đăng nhập để đổi mật khẩu.");
            return "redirect:/dang-nhap";
        }
        Integer userId = (Integer) userIdObj;

        if (result.hasErrors()) {
            model.addAttribute("title", "Tài khoản - WatchAura");
            model.addAttribute("content", "user/tai-khoan :: content");
            model.addAttribute("passwordFormVisible", true);
            return "layout/user-layout";
        }
        if (!request.getMatKhauMoi().equals(request.getXacNhanMatKhauMoi())) {
            model.addAttribute("title", "Tài khoản - WatchAura");
            model.addAttribute("content", "user/tai-khoan :: content");
            model.addAttribute("passwordFormVisible", true);
            model.addAttribute("passwordMismatch", "Mật khẩu mới và xác nhận không khớp.");
            return "layout/user-layout";
        }
        try {
            khachHangService.changePassword(userId, request.getMatKhauHienTai(), request.getMatKhauMoi());
        } catch (RuntimeException e) {
            model.addAttribute("title", "Tài khoản - WatchAura");
            model.addAttribute("content", "user/tai-khoan :: content");
            model.addAttribute("passwordFormVisible", true);
            model.addAttribute("changePasswordError", e.getMessage());
            return "layout/user-layout";
        }
        redirect.addFlashAttribute("successMessage", "Đổi mật khẩu thành công. Lần đăng nhập sau hãy dùng mật khẩu mới.");
        return "redirect:/nguoidung";
    }

}
