package com.example.watchaura.config;

import com.example.watchaura.controller.AuthController;
import com.example.watchaura.entity.DanhMuc;
import com.example.watchaura.entity.KhachHang;
import com.example.watchaura.service.DanhMucService;
import com.example.watchaura.service.GioHangService;
import com.example.watchaura.service.KhachHangService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private static final Logger logger = LoggerFactory.getLogger(GlobalControllerAdvice.class);

    private final KhachHangService khachHangService;
    private final GioHangService gioHangService;
    private final DanhMucService danhMucService;

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Void> handleNoResourceFoundException(NoResourceFoundException e) {
        // Ignore favicon and other common resource not found errors
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        logger.error("Lỗi xảy ra: ", e);
        model.addAttribute("errorMessage", e.getMessage());
        return "error";
    }

    @ModelAttribute
    public void addCurrentUser(HttpSession session, Model model) {
        Object userId = session.getAttribute(AuthController.SESSION_CURRENT_USER_ID);
        if (userId instanceof Integer) {
            try {
                KhachHang kh = khachHangService.getByIdForView((Integer) userId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
                model.addAttribute("currentUser", kh);
                model.addAttribute("soLuongGioHang", gioHangService.getSoLuongGioHang((Integer) userId));
                
                // Thêm thông tin vai trò để phân quyền menu
                String tenChucVu = "";
                if (kh.getChucVu() != null && kh.getChucVu().getTenChucVu() != null) {
                    tenChucVu = kh.getChucVu().getTenChucVu();
                }
                model.addAttribute("vaiTro", tenChucVu);
                model.addAttribute("laQuanLy", tenChucVu.equalsIgnoreCase("Admin") || tenChucVu.equalsIgnoreCase("Quản lý"));
                model.addAttribute("laNhanVien", tenChucVu.equalsIgnoreCase("Nhân viên"));
            } catch (Exception ignored) {
                session.removeAttribute(AuthController.SESSION_CURRENT_USER_ID);
                model.addAttribute("currentUser", null);
                model.addAttribute("soLuongGioHang", 0);
                model.addAttribute("vaiTro", "");
                model.addAttribute("laQuanLy", false);
                model.addAttribute("laNhanVien", false);
            }
        } else {
            model.addAttribute("currentUser", null);
            model.addAttribute("vaiTro", "");
            model.addAttribute("laQuanLy", false);
            model.addAttribute("laNhanVien", false);
            int guestCount = 0;
            Object cartAttr = session.getAttribute("cart");
            if (cartAttr instanceof java.util.Map) {
                java.util.Map<?, ?> m = (java.util.Map<?, ?>) cartAttr;
                for (Object v : m.values()) {
                    if (v instanceof Integer) {
                        guestCount += ((Integer) v).intValue();
                    }
                }
            }
            model.addAttribute("soLuongGioHang", guestCount);
        }
    }

    @ModelAttribute("danhMucList")
    public List<DanhMuc> addDanhMucList() {
        return danhMucService.getAll();
    }
}
