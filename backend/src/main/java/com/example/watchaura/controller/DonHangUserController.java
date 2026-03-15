package com.example.watchaura.controller;

import com.example.watchaura.dto.GioHangChiTietRequest;
import com.example.watchaura.dto.HoaDonDTO;
import com.example.watchaura.service.DanhGiaService;
import com.example.watchaura.service.GioHangChiTietService;
import com.example.watchaura.service.GioHangService;
import com.example.watchaura.service.HoaDonService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.watchaura.controller.AuthController.SESSION_CURRENT_USER_ID;

@Controller
@RequiredArgsConstructor
public class DonHangUserController {

    private final HoaDonService hoaDonService;
    private final GioHangService gioHangService;
    private final GioHangChiTietService gioHangChiTietService;
    private final DanhGiaService danhGiaService;

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

        String trangThaiParam = (trangThai != null && !trangThai.isBlank()) ? trangThai : null;
        String thanhToanParam = (thanhToan != null && !thanhToan.isBlank()) ? thanhToan : null;

        Pageable pageable = PageRequest.of(page, 6);

        Page<HoaDonDTO> pageHoaDon =
                hoaDonService.filterDonHang(userId, trangThaiParam, thanhToanParam, ngay, pageable);

        // Kiểm tra đánh giá cho từng đơn hàng
        Map<Integer, Boolean> daDanhGiaMap = new HashMap<>();
        if (pageHoaDon.hasContent()) {
            for (HoaDonDTO hoaDon : pageHoaDon.getContent()) {
                boolean daDanhGia = false;
                if (hoaDon.getItems() != null) {
                    for (var item : hoaDon.getItems()) {
                        if (item.getSanPhamChiTietId() != null) {
                            if (danhGiaService.hasUserReviewed(item.getSanPhamChiTietId(), userId)) {
                                daDanhGia = true;
                                break;
                            }
                        }
                    }
                }
                daDanhGiaMap.put(hoaDon.getId(), daDanhGia);
            }
        }

        model.addAttribute("pageHoaDon", pageHoaDon);
        model.addAttribute("daDanhGiaMap", daDanhGiaMap);
        model.addAttribute("trangThai", trangThai);
        model.addAttribute("thanhToan", thanhToan);
        model.addAttribute("ngay", ngay);

        model.addAttribute("title", "Đơn hàng của tôi");
        model.addAttribute("content", "user/don-hang-user-list :: content");

        return "layout/user-layout";
    }

    @GetMapping("/don-hang/api")
    @ResponseBody
    public Map<String, Object> danhSachDonHangAjax(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) String thanhToan,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngay,
            HttpSession session) {

        Integer userId = (Integer) session.getAttribute(SESSION_CURRENT_USER_ID);
        Map<String, Object> result = new HashMap<>();

        if (userId == null) {
            result.put("success", false);
            result.put("message", "Vui lòng đăng nhập.");
            return result;
        }

        String trangThaiParam = (trangThai != null && !trangThai.isBlank()) ? trangThai : null;
        String thanhToanParam = (thanhToan != null && !thanhToan.isBlank()) ? thanhToan : null;

        Pageable pageable = PageRequest.of(page, 6);

        Page<HoaDonDTO> pageHoaDon =
                hoaDonService.filterDonHang(userId, trangThaiParam, thanhToanParam, ngay, pageable);

        // Kiểm tra đánh giá cho từng đơn hàng (API)
        Map<Integer, Boolean> daDanhGiaMapApi = new HashMap<>();
        for (HoaDonDTO hoaDon : pageHoaDon.getContent()) {
            boolean daDanhGia = false;
            if (hoaDon.getItems() != null) {
                for (var item : hoaDon.getItems()) {
                    if (item.getSanPhamChiTietId() != null) {
                        if (danhGiaService.hasUserReviewed(item.getSanPhamChiTietId(), userId)) {
                            daDanhGia = true;
                            break;
                        }
                    }
                }
            }
            daDanhGiaMapApi.put(hoaDon.getId(), daDanhGia);
        }

        List<Map<String, Object>> items = pageHoaDon.getContent().stream()
                .map(hoaDon -> convertHoaDonToMap(hoaDon, daDanhGiaMapApi.get(hoaDon.getId())))
                .collect(Collectors.toList());

        result.put("success", true);
        result.put("items", items);
        result.put("currentPage", pageHoaDon.getNumber());
        result.put("totalPages", pageHoaDon.getTotalPages());
        result.put("totalElements", pageHoaDon.getTotalElements());
        result.put("hasNext", pageHoaDon.hasNext());
        result.put("hasPrevious", pageHoaDon.hasPrevious());

        return result;
    }

    private Map<String, Object> convertHoaDonToMap(HoaDonDTO hoaDon, boolean daDanhGia) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", hoaDon.getId());
        map.put("maDonHang", hoaDon.getMaDonHang());
        map.put("ngayDat", hoaDon.getNgayDat() != null ? hoaDon.getNgayDat().toString() : "");
        map.put("tongTienThanhToan", hoaDon.getTongTienThanhToan());
        map.put("trangThaiDonHang", hoaDon.getTrangThaiDonHang());
        map.put("phuongThucThanhToan", hoaDon.getPhuongThucThanhToan());
        map.put("tenTrangThai", getTenTrangThai(hoaDon.getTrangThaiDonHang()));
        map.put("daDanhGia", daDanhGia);
        if (hoaDon.getItems() != null) {
            map.put("items", hoaDon.getItems().stream()
                    .map(item -> {
                        Map<String, Object> itemMap = new HashMap<>();
                        itemMap.put("tenSanPham", item.getTenSanPham());
                        return itemMap;
                    })
                    .collect(Collectors.toList()));
        }
        return map;
    }

    private String getTenTrangThai(String trangThai) {
        if (trangThai == null) return "";
        return switch (trangThai) {
            case "CHO_XAC_NHAN" -> "Chờ xác nhận";
            case "DANG_XU_LY" -> "Đang xử lý";
            case "DANG_GIAO" -> "Đang giao";
            case "DA_GIAO" -> "Đã giao";
            case "DA_HUY" -> "Đã hủy";
            default -> trangThai;
        };
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

        if (hoaDon.getKhachHangId() == null || !hoaDon.getKhachHangId().equals(userId)) {
            return "redirect:/don-hang";
        }

        model.addAttribute("hoaDon", hoaDon);
        model.addAttribute("title", "Chi tiết đơn hàng");
        model.addAttribute("content", "user/don-hang-user-detail");

        return "layout/user-layout";
    }

    /** Mua lại: thêm tất cả sản phẩm trong đơn hàng vào giỏ hàng, rồi chuyển đến trang giỏ hàng. */
    @GetMapping("/mua-lai/{id}")
    public String muaLai(@PathVariable Integer id, HttpSession session, RedirectAttributes redirect) {
        Integer userId = (Integer) session.getAttribute(SESSION_CURRENT_USER_ID);
        if (userId == null) {
            redirect.addFlashAttribute("error", "Vui lòng đăng nhập.");
            return "redirect:/dang-nhap";
        }
        HoaDonDTO hoaDon = hoaDonService.getById(id);
        if (hoaDon.getKhachHangId() == null || !hoaDon.getKhachHangId().equals(userId)) {
            redirect.addFlashAttribute("error", "Bạn không có quyền mua lại đơn hàng này.");
            return "redirect:/don-hang";
        }
        if (hoaDon.getItems() == null || hoaDon.getItems().isEmpty()) {
            redirect.addFlashAttribute("error", "Đơn hàng không có sản phẩm.");
            return "redirect:/don-hang";
        }
        try {
            var cart = gioHangService.getOrCreateCart(userId);
            int added = 0;
            StringBuilder errors = new StringBuilder();
            for (var item : hoaDon.getItems()) {
                if (item.getSanPhamChiTietId() == null || item.getSoLuong() == null || item.getSoLuong() < 1) continue;
                try {
                    GioHangChiTietRequest req = new GioHangChiTietRequest();
                    req.setGioHangId(cart.getId());
                    req.setSanPhamChiTietId(item.getSanPhamChiTietId());
                    req.setSoLuong(item.getSoLuong());
                    gioHangChiTietService.create(req);
                    added++;
                } catch (Exception e) {
                    if (errors.length() > 0) errors.append(" ");
                    errors.append(item.getTenSanPham() != null ? item.getTenSanPham() : "Sản phẩm").append(": ").append(e.getMessage());
                }
            }
            if (added > 0) {
                redirect.addFlashAttribute("success", added == hoaDon.getItems().size()
                        ? "Đã thêm toàn bộ sản phẩm vào giỏ hàng."
                        : "Đã thêm " + added + " sản phẩm vào giỏ hàng." + (errors.length() > 0 ? " Một số sản phẩm không thêm được: " + errors : ""));
            }
            if (errors.length() > 0 && added == 0) {
                redirect.addFlashAttribute("error", "Không thêm được sản phẩm vào giỏ: " + errors);
            }
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage() != null ? e.getMessage() : "Không thể thêm vào giỏ hàng.");
        }
        return "redirect:/gio-hang";
    }

    @GetMapping("/don-hang/chi-tiet/api/{id}")
    @ResponseBody
    public Map<String, Object> chiTietDonHangAjax(@PathVariable Integer id, HttpSession session) {
        Integer userId = (Integer) session.getAttribute(SESSION_CURRENT_USER_ID);
        Map<String, Object> result = new HashMap<>();

        if (userId == null) {
            result.put("success", false);
            result.put("message", "Vui lòng đăng nhập.");
            return result;
        }

        HoaDonDTO hoaDon = hoaDonService.getById(id);

        if (hoaDon.getKhachHangId() == null || !hoaDon.getKhachHangId().equals(userId)) {
            result.put("success", false);
            result.put("message", "Không có quyền xem đơn hàng này.");
            return result;
        }

        result.put("success", true);
        result.put("id", hoaDon.getId());
        result.put("maDonHang", hoaDon.getMaDonHang());
        result.put("ngayDat", hoaDon.getNgayDat() != null ? hoaDon.getNgayDat().toString() : "");
        result.put("tenKhachHang", hoaDon.getTenKhachHang());
        result.put("sdtKhachHang", hoaDon.getSdtKhachHang());
        result.put("diaChi", hoaDon.getDiaChi());
        result.put("tongTienTamTinh", hoaDon.getTongTienTamTinh());
        result.put("tienGiam", hoaDon.getTienGiam());
        result.put("tongTienThanhToan", hoaDon.getTongTienThanhToan());
        result.put("trangThaiDonHang", hoaDon.getTrangThaiDonHang());
        result.put("tenTrangThai", getTenTrangThai(hoaDon.getTrangThaiDonHang()));
        result.put("phuongThucThanhToan", hoaDon.getPhuongThucThanhToan());
        result.put("tenPhuongThuc", hoaDon.getPhuongThucThanhToan() != null && hoaDon.getPhuongThucThanhToan().equals("COD") ? "Thanh toán khi nhận hàng (COD)" : "VNPay");

        if (hoaDon.getItems() != null) {
            result.put("items", hoaDon.getItems().stream()
                    .map(item -> {
                        Map<String, Object> itemMap = new HashMap<>();
                        itemMap.put("tenSanPham", item.getTenSanPham());
                        itemMap.put("soLuong", item.getSoLuong());
                        itemMap.put("donGia", item.getDonGia());
                        itemMap.put("thanhTien", item.getThanhTien());
                        return itemMap;
                    })
                    .collect(Collectors.toList()));
        }

        return result;
    }
}
