package com.example.watchaura.controller;

import com.example.watchaura.annotation.RequiresRole;
import com.example.watchaura.dto.ChangePasswordRequest;
import com.example.watchaura.dto.DiaChiRequest;
import com.example.watchaura.entity.DiaChi;
import com.example.watchaura.entity.KhachHang;
import com.example.watchaura.service.DiaChiService;
import com.example.watchaura.service.KhachHangService;
import com.example.watchaura.dto.ghn.ProvinceDTO;
import com.example.watchaura.service.ghn.ProvinceService;
import com.example.watchaura.util.PhoneUtils;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Controller
@RequestMapping("/nguoidung")
@RequiredArgsConstructor
@RequiresRole(value = {}, requireAuth = true)
public class UserAccountController {

    private final DiaChiService diaChiService;
    private final KhachHangService khachHangService;
    private final ProvinceService provinceService;

    /** Trang tài khoản: thông tin cá nhân + đổi mật khẩu + địa chỉ mặc định. Chỉ dành cho user đã đăng nhập. */
    @GetMapping
    public String page(HttpSession session, Model model, RedirectAttributes redirect,
                       @RequestParam(required = false) String fragment) {
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
        model.addAttribute("listDiaChi", sortedDiaChiForAccount(khachHangId));
        model.addAttribute("diaChiThemRequest", new DiaChiRequest());
        List<ProvinceDTO> provinces = provinceService.getProvinces();
        model.addAttribute("provinces", provinces);

        if ("dia-chi".equals(fragment)) {
            return "user/tai-khoan :: diaChiAjaxTarget";
        }
        return "layout/user-layout";
    }

    private List<DiaChi> sortedDiaChiForAccount(int khachHangId) {
        Integer defaultId = diaChiService.getDiaChiMacDinhByKhachHang(khachHangId).map(DiaChi::getId).orElse(null);
        List<DiaChi> list = new ArrayList<>(diaChiService.getByKhachHang(khachHangId));
        list.sort((a, b) -> {
            boolean ad = defaultId != null && defaultId.equals(a.getId());
            boolean bd = defaultId != null && defaultId.equals(b.getId());
            if (ad != bd) {
                return ad ? -1 : 1;
            }
            int ida = a.getId() != null ? a.getId() : 0;
            int idb = b.getId() != null ? b.getId() : 0;
            return Integer.compare(idb, ida);
        });
        return list;
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
            model.addAttribute("listDiaChi", sortedDiaChiForAccount(khachHangId));
            model.addAttribute("addressThemFormVisible", true);
            model.addAttribute("provinces", provinceService.getProvinces());
            return "layout/user-layout";
        }

        var listDiaChi = diaChiService.getByKhachHang(khachHangId);
        DiaChi diaChi = new DiaChi();
        diaChi.setTenNguoiNhan(trimOrNull(request.getTenNguoiNhan()));
        diaChi.setSdtNguoiNhan(trimOrNull(request.getSdtNguoiNhan()));
        diaChi.setDiaChiCuThe(request.getDiaChiCuThe());
        diaChi.setPhuongXa(request.getPhuongXa());
        diaChi.setQuanHuyen(request.getQuanHuyen());
        diaChi.setTinhThanh(request.getTinhThanh());
        diaChi.setGhnProvinceId(request.getGhnProvinceId());
        diaChi.setGhnDistrictId(request.getGhnDistrictId());
        diaChi.setGhnWardCode(request.getGhnWardCode());
        diaChi.setMacDinh(listDiaChi.isEmpty());
        diaChiService.create(khachHangId, diaChi);
        redirect.addFlashAttribute("success", "Đã thêm địa chỉ mới.");
        return "redirect:/nguoidung#dia-chi";
    }

    /** Đặt địa chỉ đã chọn làm mặc định (AJAX / radio). */
    @PostMapping("/dia-chi-mac-dinh")
    @ResponseBody
    public String setDiaChiMacDinh(HttpSession session,
                                   @RequestParam("diaChiId") Integer diaChiId) {
        Object userId = session.getAttribute(AuthController.SESSION_CURRENT_USER_ID);
        if (!(userId instanceof Integer)) {
            return "error:Vui lòng đăng nhập.";
        }
        int khachHangId = (Integer) userId;
        try {
            diaChiService.setDiaChiMacDinh(khachHangId, diaChiId);
            return "success:Đã cập nhật địa chỉ mặc định thành công.";
        } catch (Exception e) {
            return "error:" + e.getMessage();
        }
    }

    /** Thêm địa chỉ mới (AJAX). */
    @PostMapping("/dia-chi/them-ajax")
    @ResponseBody
    public String themDiaChiAjax(HttpSession session,
                                 @Valid @ModelAttribute("diaChiThemRequest") DiaChiRequest request,
                                 BindingResult bindingResult) {
        Object userId = session.getAttribute(AuthController.SESSION_CURRENT_USER_ID);
        if (!(userId instanceof Integer)) {
            return "error:Vui lòng đăng nhập.";
        }
        int khachHangId = (Integer) userId;

        if (bindingResult.hasErrors()) {
            return "error:" + formatBindingErrors(bindingResult);
        }

        var listDiaChi = diaChiService.getByKhachHang(khachHangId);
        DiaChi diaChi = new DiaChi();
        diaChi.setTenNguoiNhan(trimOrNull(request.getTenNguoiNhan()));
        diaChi.setSdtNguoiNhan(trimOrNull(request.getSdtNguoiNhan()));
        diaChi.setDiaChiCuThe(request.getDiaChiCuThe());
        diaChi.setPhuongXa(request.getPhuongXa());
        diaChi.setQuanHuyen(request.getQuanHuyen());
        diaChi.setTinhThanh(request.getTinhThanh());
        diaChi.setGhnProvinceId(request.getGhnProvinceId());
        diaChi.setGhnDistrictId(request.getGhnDistrictId());
        diaChi.setGhnWardCode(request.getGhnWardCode());
        diaChi.setMacDinh(listDiaChi.isEmpty());
        diaChiService.create(khachHangId, diaChi);
        return "success:Đã thêm địa chỉ mới.";
    }

    /** Cập nhật họ tên, email, SĐT, ngày sinh, giới tính (JSON). */
    @PostMapping("/cap-nhat-ho-so")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> capNhatHoSo(HttpSession session,
            @RequestParam("tenNguoiDung") String tenNguoiDung,
            @RequestParam("email") String email,
            @RequestParam("sdt") String sdt,
            @RequestParam(value = "ngaySinh", required = false) String ngaySinhRaw,
            @RequestParam(value = "gioiTinh", required = false) String gioiTinhRaw) {
        Object userId = session.getAttribute(AuthController.SESSION_CURRENT_USER_ID);
        if (!(userId instanceof Integer)) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Vui lòng đăng nhập."));
        }
        int khachHangId = (Integer) userId;

        String ten = tenNguoiDung != null ? tenNguoiDung.trim() : "";
        if (ten.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Họ và tên không được để trống."));
        }
        if (ten.length() > 100) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Họ và tên tối đa 100 ký tự."));
        }

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Email không được để trống."));
        }
        String emailTrim = email.trim();
        if (emailTrim.length() > 100) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Email tối đa 100 ký tự."));
        }
        if (!emailTrim.matches("^[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}$")) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Email không đúng định dạng."));
        }
        if (khachHangService.existsEmailTakenByOther(emailTrim, khachHangId)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Email đã được sử dụng bởi tài khoản khác."));
        }

        String sdtNorm = PhoneUtils.normalizeVnMobile(sdt);
        if (!PhoneUtils.isValidVnMobile(sdtNorm)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Số điện thoại không hợp lệ (10 số, ví dụ 0912345678)."));
        }
        if (khachHangService.existsSdtTakenByOther(sdtNorm, khachHangId)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Số điện thoại đã được sử dụng bởi tài khoản khác."));
        }

        LocalDate ngaySinh = null;
        if (ngaySinhRaw != null && !ngaySinhRaw.isBlank()) {
            try {
                ngaySinh = LocalDate.parse(ngaySinhRaw.trim());
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Ngày sinh không hợp lệ."));
            }
            if (ngaySinh.isAfter(LocalDate.now())) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Ngày sinh không được ở tương lai."));
            }
        }

        String gioiTinh = gioiTinhRaw != null ? gioiTinhRaw.trim() : "";
        if (gioiTinh.length() > 10) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Giới tính không hợp lệ."));
        }
        if (gioiTinh.isEmpty()) {
            gioiTinh = null;
        }

        KhachHang patch = new KhachHang();
        patch.setTenNguoiDung(ten);
        patch.setEmail(emailTrim);
        patch.setSdt(sdtNorm);
        patch.setNgaySinh(ngaySinh);
        patch.setGioiTinh(gioiTinh);
        patch.setMatKhau(null);

        try {
            khachHangService.update(khachHangId, patch);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage() != null ? e.getMessage() : "Không cập nhật được hồ sơ."));
        }

        Map<String, Object> ok = new HashMap<>();
        ok.put("success", true);
        ok.put("message", "Đã cập nhật thông tin tài khoản.");
        return ResponseEntity.ok(ok);
    }

    private static String trimOrNull(String s) {
        if (s == null) {
            return null;
        }
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    /** Nối thông báo lỗi validation (AJAX) để toast hiển thị rõ, ví dụ SĐT không hợp lệ. */
    private static String formatBindingErrors(BindingResult br) {
        Set<String> msgs = new LinkedHashSet<>();
        for (FieldError fe : br.getFieldErrors()) {
            String m = fe.getDefaultMessage();
            if (m != null && !m.isBlank()) {
                msgs.add(m.trim());
            }
        }
        for (ObjectError ge : br.getGlobalErrors()) {
            String m = ge.getDefaultMessage();
            if (m != null && !m.isBlank()) {
                msgs.add(m.trim());
            }
        }
        if (msgs.isEmpty()) {
            return "Thông tin không hợp lệ.";
        }
        return String.join(" · ", msgs);
    }
}
