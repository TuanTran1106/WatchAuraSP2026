package com.example.watchaura.controller;

import com.example.watchaura.dto.ChangePasswordRequest;
import com.example.watchaura.dto.DiaChiRequest;
import com.example.watchaura.entity.DiaChi;
import com.example.watchaura.service.DiaChiService;
import com.example.watchaura.service.KhachHangService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/nguoidung")
@RequiredArgsConstructor
public class UserAccountController {

    private final KhachHangService khachHangService;
    private final DiaChiService diaChiService;

    /** Trang tài khoản: thông tin cá nhân + đổi mật khẩu + địa chỉ mặc định. Chỉ dành cho user đã đăng nhập. */
    @GetMapping
    public String page(HttpSession session, Model model, RedirectAttributes redirect) {
        Object userId = session.getAttribute(AuthController.SESSION_CURRENT_USER_ID);
        if (!(userId instanceof Integer)) {
            redirect.addFlashAttribute("infoMessage", "Vui lòng đăng nhập để xem tài khoản.");
            return "redirect:/dang-nhap";
        }
        int khachHangId = (Integer) userId;

        model.addAttribute("title", "Tài khoản - WatchAura");
        model.addAttribute("content", "user/tai-khoan :: content");
        model.addAttribute("changePasswordRequest", new ChangePasswordRequest());
        model.addAttribute("passwordFormVisible", false);

        var defaultDiaChi = diaChiService.getDiaChiMacDinhByKhachHang(khachHangId);
        model.addAttribute("defaultDiaChi", defaultDiaChi.orElse(null));
        model.addAttribute("listDiaChi", diaChiService.getByKhachHang(khachHangId));
        model.addAttribute("diaChiThemRequest", new DiaChiRequest());

        return "layout/user-layout";
    }

    /** Thêm địa chỉ mới (cùng các ô input như form cập nhật). */
    @PostMapping("/dia-chi/them")
    public String themDiaChi(HttpSession session,
                             @Valid @ModelAttribute("diaChiThemRequest") DiaChiRequest request,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirect) {
        Object userId = session.getAttribute(AuthController.SESSION_CURRENT_USER_ID);
        if (!(userId instanceof Integer)) {
            redirect.addFlashAttribute("infoMessage", "Vui lòng đăng nhập.");
            return "redirect:/dang-nhap";
        }
        int khachHangId = (Integer) userId;

        if (bindingResult.hasErrors()) {
            model.addAttribute("title", "Tài khoản - WatchAura");
            model.addAttribute("content", "user/tai-khoan :: content");
            model.addAttribute("changePasswordRequest", new ChangePasswordRequest());
            model.addAttribute("passwordFormVisible", false);
            model.addAttribute("defaultDiaChi", diaChiService.getDiaChiMacDinhByKhachHang(khachHangId).orElse(null));
            model.addAttribute("listDiaChi", diaChiService.getByKhachHang(khachHangId));
            model.addAttribute("addressThemFormVisible", true);
            return "layout/user-layout";
        }

        var listDiaChi = diaChiService.getByKhachHang(khachHangId);
        DiaChi diaChi = new DiaChi();
        diaChi.setDiaChiCuThe(request.getDiaChiCuThe());
        diaChi.setPhuongXa(request.getPhuongXa());
        diaChi.setQuanHuyen(request.getQuanHuyen());
        diaChi.setTinhThanh(request.getTinhThanh());
        diaChi.setMacDinh(listDiaChi.isEmpty());
        diaChiService.create(khachHangId, diaChi);
        redirect.addFlashAttribute("success", "Đã thêm địa chỉ mới.");
        return "redirect:/nguoidung#dia-chi";
    }

    /** Đặt địa chỉ đã chọn làm mặc định (submit từ danh sách chọn). */
    @PostMapping("/dia-chi-mac-dinh")
    @ResponseBody
    public String setDiaChiMacDinh(HttpSession session,
                                 @RequestParam("diaChiId") Integer diaChiId,
                                 RedirectAttributes redirect) {
        Object userId = session.getAttribute(AuthController.SESSION_CURRENT_USER_ID);
        if (!(userId instanceof Integer)) {
            return "error:Vui lòng đăng nhập.";
        }
        int khachHangId = (Integer) userId;
        try {
            diaChiService.setDiaChiMacDinh(khachHangId, diaChiId);
            redirect.addFlashAttribute("success", "Đã đặt địa chỉ mặc định.");
            return "success:Đã đặt địa chỉ mặc định.";
        } catch (Exception e) {
            return "error:" + e.getMessage();
        }
    }

    /** Thêm địa chỉ mới (AJAX). */
    @PostMapping("/dia-chi/them-ajax")
    @ResponseBody
    public String themDiaChiAjax(HttpSession session,
                                 @Valid @ModelAttribute("diaChiThemRequest") DiaChiRequest request,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirect) {
        Object userId = session.getAttribute(AuthController.SESSION_CURRENT_USER_ID);
        if (!(userId instanceof Integer)) {
            return "error:Vui lòng đăng nhập.";
        }
        int khachHangId = (Integer) userId;

        if (bindingResult.hasErrors()) {
            return "error:Thông tin không hợp lệ.";
        }

        var listDiaChi = diaChiService.getByKhachHang(khachHangId);
        DiaChi diaChi = new DiaChi();
        diaChi.setDiaChiCuThe(request.getDiaChiCuThe());
        diaChi.setPhuongXa(request.getPhuongXa());
        diaChi.setQuanHuyen(request.getQuanHuyen());
        diaChi.setTinhThanh(request.getTinhThanh());
        diaChi.setMacDinh(listDiaChi.isEmpty());
        diaChiService.create(khachHangId, diaChi);
        redirect.addFlashAttribute("success", "Đã thêm địa chỉ mới.");
        return "success:Đã thêm địa chỉ mới.";
    }
}
