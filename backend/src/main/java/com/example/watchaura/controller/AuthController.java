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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
            @RequestParam(value = "success", required = false) String success,
            HttpSession session,
            Model model) {
        // Nếu đã đăng nhập rồi thì chuyển hướng về trang chủ để mua hàng
        Integer userId = (Integer) session.getAttribute(SESSION_CURRENT_USER_ID);
        if (userId != null) {
            Optional<KhachHang> opt = khachHangService.getByIdForView(userId);
            if (opt.isPresent()) {
                return "redirect:/home";
            }
        }

        model.addAttribute("title", "Đăng nhập - WatchAura");
        if (error != null) model.addAttribute("errorMessage", error);
        if (success != null) model.addAttribute("successMessage", success);
        return "user/dang-nhap";
    }

    /** Trang đăng nhập dành cho Admin / Nhân viên (cùng form, không chọn vai trò) */
    @GetMapping("/admin/login")
    public String adminLoginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "success", required = false) String success,
            Model model) {
        model.addAttribute("title", "Đăng nhập - WatchAura");
        if (error != null) model.addAttribute("errorMessage", error);
        if (success != null) model.addAttribute("successMessage", success);
        return "admin/login";
    }

    /** Đăng nhập khu vực quản trị — tự nhận chức vụ: Admin/Quản lý → /admin, Nhân viên → /ban-hang */
    @PostMapping("/admin/login")
    public String adminLogin(
            @RequestParam("email") String email,
            @RequestParam("matKhau") String matKhau,
            HttpSession session,
            RedirectAttributes redirect) {
        if (email == null || email.isBlank()) {
            redirect.addAttribute("error", "Vui lòng nhập email.");
            return "redirect:/admin/login";
        }
        Optional<KhachHang> opt = khachHangService.findByEmail(email.trim());
        if (opt.isEmpty()) {
            redirect.addAttribute("error", "Email hoặc mật khẩu không đúng.");
            return "redirect:/admin/login";
        }
        KhachHang kh = opt.get();
        if (Boolean.FALSE.equals(kh.getTrangThai())) {
            redirect.addAttribute("error", "Tài khoản đã bị khóa.");
            return "redirect:/admin/login";
        }
        if (matKhau == null || !passwordEncoder.matches(matKhau, kh.getMatKhau())) {
            redirect.addAttribute("error", "Email hoặc mật khẩu không đúng.");
            return "redirect:/admin/login";
        }

        // Kiểm tra vai trò và chuyển hướng phù hợp
        String redirectUrl = null;
        boolean isAdmin = false;
        boolean isNhanVien = false;

        if (kh.getChucVu() != null) {
            String tenChucVu = kh.getChucVu().getTenChucVu();
            if (tenChucVu != null) {
                String normalizedRole = normalizeRole(tenChucVu);
                // Admin hoặc Quản lý -> vào /admin
                if (normalizedRole.contains("admin") || normalizedRole.contains("quanly")) {
                    redirectUrl = "/admin";
                    isAdmin = true;
                }
                // Nhân viên -> vào /ban-hang
                else if (normalizedRole.contains("nhanvien")) {
                    redirectUrl = "/ban-hang";
                    isNhanVien = true;
                }
            }
        }

        if (redirectUrl == null) {
            redirect.addAttribute("error", "Bạn không có quyền truy cập khu vực quản trị.");
            return "redirect:/admin/login";
        }

        session.setAttribute(SESSION_CURRENT_USER_ID, kh.getId());
        if (isAdmin) {
            redirect.addFlashAttribute("successMessage", "Xin chào Admin " + kh.getTenNguoiDung() + "!");
        } else {
            redirect.addFlashAttribute("successMessage", "Xin chào, " + kh.getTenNguoiDung() + "! Vui lòng vào khu vực bán hàng.");
        }
        return "redirect:" + redirectUrl;
    }

    /**
     * Chuẩn hóa tên chức vụ để so sánh
     */
    private String normalizeRole(String role) {
        if (role == null) return "";
        return role.toLowerCase().trim()
                .replace("á", "a").replace("à", "a").replace("ả", "a").replace("ã", "a").replace("ạ", "a")
                .replace("ắ", "a").replace("ằ", "a").replace("ẳ", "a").replace("ẵ", "a")
                .replace("â", "a").replace("ấ", "a").replace("ầ", "a").replace("ẩ", "a").replace("ẫ", "a")
                .replace("é", "e").replace("è", "e").replace("ẻ", "e").replace("ẽ", "e").replace("ẹ", "e")
                .replace("ê", "e").replace("ế", "e").replace("ề", "e").replace("ể", "e").replace("ễ", "e")
                .replace("í", "i").replace("ì", "i").replace("ỉ", "i").replace("ĩ", "i").replace("ị", "i")
                .replace("ó", "o").replace("ò", "o").replace("ỏ", "o").replace("õ", "o").replace("ọ", "o")
                .replace("ô", "o").replace("ố", "o").replace("ồ", "o").replace("ổ", "o").replace("ỗ", "o")
                .replace("ơ", "o").replace("ớ", "o").replace("ờ", "o").replace("ở", "o").replace("ỡ", "o")
                .replace("ú", "u").replace("ù", "u").replace("ủ", "u").replace("ũ", "u").replace("ụ", "u")
                .replace("ư", "u").replace("ứ", "u").replace("ừ", "u").replace("ử", "u").replace("ữ", "u")
                .replace("ý", "y").replace("ỳ", "y").replace("ỷ", "y").replace("ỹ", "y").replace("ỵ", "y")
                .replace("đ", "d");
    }

    /** Trang đăng nhập nhân viên (user layout) */
    @GetMapping("/nhan-vien/login")
    public String nhanVienLoginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "success", required = false) String success,
            Model model) {
        model.addAttribute("title", "Đăng nhập nhân viên - WatchAura");
        if (error != null) model.addAttribute("errorMessage", error);
        if (success != null) model.addAttribute("successMessage", success);
        return "user/nhan-vien-login";
    }

    /** Xử lý đăng nhập nhân viên - chỉ nhân viên được đăng nhập, chuyển hướng đến /ban-hang */
    @PostMapping("/nhan-vien/login")
    public String nhanVienLogin(
            @RequestParam("email") String email,
            @RequestParam("matKhau") String matKhau,
            HttpSession session,
            RedirectAttributes redirect) {
        if (email == null || email.isBlank()) {
            redirect.addAttribute("error", "Vui lòng nhập email.");
            return "redirect:/nhan-vien/login";
        }
        Optional<KhachHang> opt = khachHangService.findByEmail(email.trim());
        if (opt.isEmpty()) {
            redirect.addAttribute("error", "Email hoặc mật khẩu không đúng.");
            return "redirect:/nhan-vien/login";
        }
        KhachHang kh = opt.get();
        if (Boolean.FALSE.equals(kh.getTrangThai())) {
            redirect.addAttribute("error", "Tài khoản đã bị khóa.");
            return "redirect:/nhan-vien/login";
        }
        if (matKhau == null || !passwordEncoder.matches(matKhau, kh.getMatKhau())) {
            redirect.addAttribute("error", "Email hoặc mật khẩu không đúng.");
            return "redirect:/nhan-vien/login";
        }

        // Kiểm tra có phải là nhân viên không
        boolean laNhanVien = false;
        if (kh.getChucVu() != null) {
            String tenChucVu = kh.getChucVu().getTenChucVu();
            if (tenChucVu != null) {
                String t = tenChucVu.trim();
                if (t.equalsIgnoreCase("Nhân viên") || t.equalsIgnoreCase("nhanvien")) {
                    laNhanVien = true;
                }
            }
        }

        if (!laNhanVien) {
            redirect.addFlashAttribute("errorMessage",
                    "Tài khoản này không phải là tài khoản nhân viên.");
            return "redirect:/nhan-vien/login";
        }

        session.setAttribute(SESSION_CURRENT_USER_ID, kh.getId());
        redirect.addFlashAttribute("successMessage", "Xin chào, " + kh.getTenNguoiDung() + "!");
        return "redirect:/ban-hang";
    }

    /** Đăng xuất nhân viên - chuyển về trang đăng nhập nhân viên */
    @GetMapping("/nhan-vien/dang-xuat")
    public String nhanVienDangXuat(HttpSession session, RedirectAttributes redirect) {
        session.invalidate();
        redirect.addFlashAttribute("successMessage", "Bạn đã đăng xuất.");
        return "redirect:/nhan-vien/login";
    }

    /** Trang đăng nhập riêng cho Admin (cũ) - chuyển hướng về /admin/login */
    @GetMapping("/admin/dang-nhap")
    public String adminDangNhapPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "success", required = false) String success) {
        StringBuilder url = new StringBuilder("redirect:/admin/login");
        String sep = "?";
        if (error != null && !error.isBlank()) {
            url.append(sep).append("error=").append(URLEncoder.encode(error, StandardCharsets.UTF_8));
            sep = "&";
        }
        if (success != null && !success.isBlank()) {
            url.append(sep).append("success=").append(URLEncoder.encode(success, StandardCharsets.UTF_8));
        }
        return url.toString();
    }

    /** Admin / Quản lý / Nhân viên — chỉ đăng nhập qua /admin/login, không dùng form khách hàng */
    private static boolean isTaiKhoanNoiBo(KhachHang kh) {
        if (kh.getChucVu() == null) {
            return false;
        }
        String ten = kh.getChucVu().getTenChucVu();
        if (ten == null || ten.isBlank()) {
            return false;
        }
        String t = ten.trim();
        return t.equalsIgnoreCase("Nhân viên") || t.equalsIgnoreCase("nhanvien")
                || t.equalsIgnoreCase("Admin") || t.equalsIgnoreCase("Quản lý")
                || t.equalsIgnoreCase("admin") || t.equalsIgnoreCase("quanly");
    }

    /** Đăng nhập — mọi tài khoản (khách hàng, admin, nhân viên) đều vào /home để mua hàng */
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
        redirect.addFlashAttribute("welcomeMessage", "Xin chào, " + kh.getTenNguoiDung() + "!");
        return "redirect:/home";
    }

    /** Xử lý đăng nhập Admin (cũ) - chuyển hướng về /admin/login */
    @PostMapping("/admin/dang-nhap")
    public String adminDangNhapOld() {
        return "redirect:/admin/login";
    }

    @GetMapping("/dang-xuat")
    public String dangXuat(HttpSession session, RedirectAttributes redirect) {
        session.invalidate();
        redirect.addFlashAttribute("infoMessage", "Bạn đã đăng xuất.");
        return "redirect:/home";
    }

    /** Đăng xuất Admin - chuyển về trang đăng nhập admin */
    @GetMapping("/admin/dang-xuat")
    public String adminDangXuat(HttpSession session, RedirectAttributes redirect) {
        session.invalidate();
        redirect.addFlashAttribute("successMessage", "Bạn đã đăng xuất.");
        return "redirect:/admin/login";
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
        return "user/dang-ky";
    }

    @PostMapping("/dang-ky")
    @Transactional
    public String dangKy(
            @Valid @ModelAttribute("registerRequest") RegisterRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "user/dang-ky";
        }
        if (!request.getMatKhau().equals(request.getXacNhanMatKhau())) {
            model.addAttribute("passwordMismatch", "Mật khẩu và xác nhận mật khẩu không khớp.");
            return "user/dang-ky";
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
            model.addAttribute("registerError", e.getMessage());
            return "user/dang-ky";
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
