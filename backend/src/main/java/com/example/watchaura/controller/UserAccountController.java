package com.example.watchaura.controller;

import com.example.watchaura.annotation.RequiresRole;
import com.example.watchaura.dto.ChangePasswordRequest;
import com.example.watchaura.dto.DiaChiRequest;
import com.example.watchaura.entity.DiaChi;
import com.example.watchaura.service.DiaChiService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/nguoidung")
@RequiredArgsConstructor
@RequiresRole(value = {}, requireAuth = true)
public class UserAccountController {

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
        model.addAttribute("listDiaChi", getActiveAddresses(khachHangId));
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
            model.addAttribute("listDiaChi", getActiveAddresses(khachHangId));
            model.addAttribute("addressThemFormVisible", true);
            return "layout/user-layout";
        }

        var listDiaChi = getActiveAddresses(khachHangId);
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

        var listDiaChi = getActiveAddresses(khachHangId);
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

    @GetMapping("/dia-chi")
    @ResponseBody
    public List<DiaChi> listDiaChi(HttpSession session) {
        int userId = requireCurrentUserId(session);
        return getActiveAddresses(userId);
    }

    @PostMapping("/dia-chi")
    @ResponseBody
    public DiaChi createDiaChi(HttpSession session, @RequestBody DiaChi payload) {
        int userId = requireCurrentUserId(session);
        validateAddressPayload(payload);
        payload.setId(null);
        payload.setIsDeleted(false);
        payload.setMacDinh(getActiveAddresses(userId).isEmpty());
        return diaChiService.create(userId, payload);
    }

    @PutMapping("/dia-chi/{id}")
    @ResponseBody
    public DiaChi updateDiaChi(HttpSession session, @org.springframework.web.bind.annotation.PathVariable("id") Integer id, @RequestBody DiaChi payload) {
        int userId = requireCurrentUserId(session);
        DiaChi existing = requireOwnedAddress(userId, id);
        validateAddressPayload(payload);

        existing.setDiaChiCuThe(payload.getDiaChiCuThe());
        existing.setPhuongXa(payload.getPhuongXa());
        existing.setQuanHuyen(payload.getQuanHuyen());
        existing.setTinhThanh(payload.getTinhThanh());
        existing.setTenNguoiNhan(payload.getTenNguoiNhan());
        existing.setSdtNguoiNhan(payload.getSdtNguoiNhan());
        existing.setGhnProvinceId(payload.getGhnProvinceId());
        existing.setGhnDistrictId(payload.getGhnDistrictId());
        existing.setGhnWardCode(payload.getGhnWardCode());
        existing.setMacDinh(Boolean.TRUE.equals(payload.getMacDinh()));
        return diaChiService.update(id, existing);
    }

    @DeleteMapping("/dia-chi/{id}")
    @ResponseBody
    public Map<String, Object> deleteDiaChi(HttpSession session, @org.springframework.web.bind.annotation.PathVariable("id") Integer id) {
        int userId = requireCurrentUserId(session);
        DiaChi existing = requireOwnedAddress(userId, id);
        existing.setIsDeleted(true);
        existing.setMacDinh(false);
        diaChiService.update(id, existing);

        List<DiaChi> remaining = getActiveAddresses(userId);
        if (!remaining.isEmpty() && remaining.stream().noneMatch(x -> Boolean.TRUE.equals(x.getMacDinh()))) {
            diaChiService.setDiaChiMacDinh(userId, remaining.get(0).getId());
        }
        return Map.of("success", true);
    }

    @PutMapping("/dia-chi/{id}/mac-dinh")
    @ResponseBody
    public Map<String, Object> setDefaultDiaChiJson(HttpSession session, @org.springframework.web.bind.annotation.PathVariable("id") Integer id) {
        int userId = requireCurrentUserId(session);
        requireOwnedAddress(userId, id);
        diaChiService.setDiaChiMacDinh(userId, id);
        return Map.of("success", true);
    }

    private int requireCurrentUserId(HttpSession session) {
        Object userId = session.getAttribute(AuthController.SESSION_CURRENT_USER_ID);
        if (!(userId instanceof Integer)) {
            throw new RuntimeException("Vui lòng đăng nhập.");
        }
        return (Integer) userId;
    }

    private DiaChi requireOwnedAddress(Integer userId, Integer addressId) {
        return getActiveAddresses(userId).stream()
                .filter(x -> x.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại hoặc không thuộc tài khoản của bạn."));
    }

    private List<DiaChi> getActiveAddresses(Integer khachHangId) {
        return diaChiService.getByKhachHang(khachHangId).stream()
                .filter(x -> !Boolean.TRUE.equals(x.getIsDeleted()))
                .collect(Collectors.toList());
    }

    private void validateAddressPayload(DiaChi payload) {
        if (payload == null) throw new RuntimeException("Dữ liệu địa chỉ không hợp lệ.");
        if (payload.getDiaChiCuThe() == null || payload.getDiaChiCuThe().isBlank()) {
            throw new RuntimeException("Địa chỉ cụ thể không được để trống.");
        }
        if (payload.getPhuongXa() == null || payload.getPhuongXa().isBlank()) {
            throw new RuntimeException("Phường/Xã không được để trống.");
        }
        if (payload.getQuanHuyen() == null || payload.getQuanHuyen().isBlank()) {
            throw new RuntimeException("Quận/Huyện không được để trống.");
        }
        if (payload.getTinhThanh() == null || payload.getTinhThanh().isBlank()) {
            throw new RuntimeException("Tỉnh/Thành không được để trống.");
        }
    }
}
