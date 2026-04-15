package com.example.watchaura.controller;

import com.example.watchaura.annotation.RequiresRole;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.watchaura.controller.AuthController.SESSION_CURRENT_USER_ID;

@Controller
@RequiredArgsConstructor
public class DonHangUserController {

    private final HoaDonService hoaDonService;
    private final GioHangService gioHangService;
    private final GioHangChiTietService gioHangChiTietService;
    private final DanhGiaService danhGiaService;

    /**
     * Trang theo dõi đơn hàng - KHÔNG yêu cầu đăng nhập
     * Người dùng nhập mã đơn hàng để xem thông tin
     */
    @GetMapping("/theo-doi-don-hang")
    public String theoDoiDonHang() {
        return "user/theo-doi-don-hang";
    }

    /**
     * API tìm kiếm đơn hàng theo mã - KHÔNG yêu cầu đăng nhập
     */
    @GetMapping("/theo-doi-don-hang/api")
    @ResponseBody
    public Map<String, Object> theoDoiDonHangApi(@RequestParam String maDon) {
        Map<String, Object> result = new HashMap<>();

        try {
            HoaDonDTO hoaDon = hoaDonService.getByMaDonHang(maDon.trim());

            result.put("success", true);
            result.put("id", hoaDon.getId());
            result.put("maDonHang", hoaDon.getMaDonHang());
            result.put("ngayDat", hoaDon.getNgayDat() != null ? hoaDon.getNgayDat().toString() : "");
            result.put("tenKhachHang", hoaDon.getTenKhachHang());
            result.put("sdtKhachHang", hoaDon.getSdtKhachHang());
            result.put("diaChi", hoaDon.getDiaChi());
            result.put("email", hoaDon.getEmail());
            result.put("tongTienTamTinh", hoaDon.getTongTienTamTinh());
            result.put("tongTienChuaGiam", hoaDon.getTongTienChuaGiam());
            result.put("tienGiam", hoaDon.getTienGiam());
            result.put("phiVanChuyen", hoaDon.getPhiVanChuyen());
            result.put("tongTienThanhToan", hoaDon.getTongTienThanhToan());
            result.put("trangThaiDonHang", hoaDon.getTrangThaiDonHang());
            result.put("ghiChu", hoaDon.getGhiChu());
            result.put("phuongThucThanhToan", hoaDon.getPhuongThucThanhToan());

            if (hoaDon.getItems() != null) {
                result.put("items", hoaDon.getItems().stream()
                        .map(item -> {
                            Map<String, Object> itemMap = new HashMap<>();
                            itemMap.put("tenSanPham", item.getTenSanPham());
                            itemMap.put("tenBienThe", item.getTenBienThe());
                            itemMap.put("soLuong", item.getSoLuong());
                            itemMap.put("donGia", item.getDonGia());
                            itemMap.put("giaGoc", item.getGiaGoc());
                            itemMap.put("thanhTien", item.getThanhTien());
                            return itemMap;
                        })
                        .collect(Collectors.toList()));
            }
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Không tìm thấy đơn hàng với mã: " + maDon);
        }

        return result;
    }

    /**
     * Trang đơn hàng của tôi - Có thể truy cập không cần đăng nhập
     * - Đã đăng nhập: Hiển thị danh sách đơn hàng của user + tìm kiếm theo mã
     * - Chưa đăng nhập: Chỉ hiển thị tìm kiếm theo mã đơn
     */
    @GetMapping("/don-hang")
    public String danhSachDonHang(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) String thanhToan,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngay,
            @RequestParam(required = false) String maDon,
            Model model,
            HttpSession session) {

        Integer userId = (Integer) session.getAttribute(SESSION_CURRENT_USER_ID);

        // Nếu có mã đơn, tìm kiếm đơn hàng theo mã (không cần đăng nhập)
        String maDonParam = (maDon != null && !maDon.isBlank()) ? maDon.trim() : null;

        if (userId == null) {
            // Chưa đăng nhập: tra cứu theo mã qua GET (form submit), không cần AJAX
            model.addAttribute("isLoggedIn", false);
            model.addAttribute("daDanhGiaMap", new HashMap<>());
            model.addAttribute("trangThai", trangThai);
            model.addAttribute("thanhToan", thanhToan);
            model.addAttribute("ngay", ngay);
            model.addAttribute("maDon", maDon);
            model.addAttribute("donHangLoadFailed", false);

            if (maDonParam != null) {
                try {
                    HoaDonDTO one = hoaDonService.getByMaDonHang(maDonParam);
                    Page<HoaDonDTO> guestPage =
                            new PageImpl<>(List.of(one), PageRequest.of(0, 6), 1);
                    model.addAttribute("pageHoaDon", guestPage);
                    model.addAttribute("guestLookupMessage", null);
                } catch (Exception e) {
                    model.addAttribute(
                            "pageHoaDon",
                            new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 6), 0));
                    model.addAttribute(
                            "guestLookupMessage",
                            "Không tìm thấy đơn hàng với mã: " + maDonParam);
                }
            } else {
                model.addAttribute("pageHoaDon", null);
                model.addAttribute("guestLookupMessage", null);
            }

            model.addAttribute("title", "Đơn hàng của tôi");
            model.addAttribute("content", "user/don-hang-user-list :: content");
            return "layout/user-layout";
        }

        // Đã đăng nhập - load đơn hàng của user
        try {
            String trangThaiParam = (trangThai != null && !trangThai.isBlank()) ? trangThai : null;
            String thanhToanParam = (thanhToan != null && !thanhToan.isBlank()) ? thanhToan : null;

            Pageable pageable = PageRequest.of(page, 6);

            Page<HoaDonDTO> pageHoaDon =
                    hoaDonService.filterDonHang(userId, trangThaiParam, thanhToanParam, ngay, maDonParam, pageable);

            // Batch check đánh giá - chỉ 1 query thay vì N+1
            Map<Integer, Boolean> daDanhGiaMap = new HashMap<>();
            if (pageHoaDon.hasContent()) {
                Set<Integer> allSpctIds = pageHoaDon.getContent().stream()
                        .filter(hd -> hd.getItems() != null)
                        .flatMap(hd -> hd.getItems().stream())
                        .map(item -> item.getSanPhamChiTietId())
                        .filter(id -> id != null)
                        .collect(Collectors.toSet());

                Set<Integer> reviewedIds = danhGiaService.getReviewedProductIds(allSpctIds, userId);

                for (HoaDonDTO hoaDon : pageHoaDon.getContent()) {
                    boolean daDanhGia = false;
                    if (hoaDon.getItems() != null) {
                        for (var item : hoaDon.getItems()) {
                            if (item.getSanPhamChiTietId() != null && reviewedIds.contains(item.getSanPhamChiTietId())) {
                                daDanhGia = true;
                                break;
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
            model.addAttribute("maDon", maDon);
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("donHangLoadFailed", false);
            model.addAttribute("guestLookupMessage", null);
        } catch (Exception e) {
            // Log lỗi nhưng vẫn hiển thị trang
            model.addAttribute("pageHoaDon", null);
            model.addAttribute("daDanhGiaMap", new HashMap<>());
            model.addAttribute("trangThai", trangThai);
            model.addAttribute("thanhToan", thanhToan);
            model.addAttribute("ngay", ngay);
            model.addAttribute("maDon", maDon);
            model.addAttribute("isLoggedIn", true);
            model.addAttribute("donHangLoadFailed", true);
            model.addAttribute("guestLookupMessage", null);
        }

        model.addAttribute("title", "Đơn hàng của tôi");
        model.addAttribute("content", "user/don-hang-user-list :: content");

        return "layout/user-layout";
    }

    /**
     * API lấy danh sách đơn hàng - Có thể truy cập không cần đăng nhập khi có mã đơn
     */
    @GetMapping("/don-hang/api")
    @ResponseBody
    public Map<String, Object> danhSachDonHangAjax(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) String thanhToan,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngay,
            @RequestParam(required = false) String maDon,
            HttpSession session) {

        Integer userId = (Integer) session.getAttribute(SESSION_CURRENT_USER_ID);
        Map<String, Object> result = new HashMap<>();

        try {
            // Nếu có mã đơn, cho phép tra cứu không cần đăng nhập
            String maDonParam = (maDon != null && !maDon.isBlank()) ? maDon.trim() : null;

            if (userId == null && maDonParam == null) {
                result.put("success", false);
                result.put("message", "Vui lòng đăng nhập hoặc nhập mã đơn hàng.");
                return result;
            }

            // Nếu không đăng nhập nhưng có mã đơn - tra cứu đơn hàng theo mã
            if (userId == null && maDonParam != null) {
                try {
                    HoaDonDTO hoaDon = hoaDonService.getByMaDonHang(maDonParam);
                    List<Map<String, Object>> items = new java.util.ArrayList<>();
                    if (hoaDon.getItems() != null) {
                        items = hoaDon.getItems().stream()
                                .map(item -> {
                                    Map<String, Object> itemMap = new HashMap<>();
                                    itemMap.put("tenSanPham", item.getTenSanPham());
                                    itemMap.put("tenBienThe", item.getTenBienThe());
                                    return itemMap;
                                })
                                .collect(Collectors.toList());
                    }

                    List<Map<String, Object>> orders = new java.util.ArrayList<>();
                    Map<String, Object> orderMap = new HashMap<>();
                    orderMap.put("id", hoaDon.getId());
                    orderMap.put("maDonHang", hoaDon.getMaDonHang());
                    orderMap.put("ngayDat", hoaDon.getNgayDat() != null ? hoaDon.getNgayDat().toString() : "");
                    orderMap.put("tongTienThanhToan", hoaDon.getTongTienThanhToan());
                    orderMap.put("trangThaiDonHang", hoaDon.getTrangThaiDonHang());
                    orderMap.put("ghiChu", hoaDon.getGhiChu());
                    orderMap.put("phuongThucThanhToan", hoaDon.getPhuongThucThanhToan());
                    orderMap.put("tenTrangThai", getTenTrangThai(hoaDon.getTrangThaiDonHang()));
                    orderMap.put("daDanhGia", false);
                    orderMap.put("items", items);
                    orders.add(orderMap);

                    result.put("success", true);
                    result.put("items", orders);
                    result.put("currentPage", 0);
                    result.put("totalPages", 1);
                    result.put("totalElements", 1);
                    result.put("hasNext", false);
                    result.put("hasPrevious", false);
                } catch (Exception e) {
                    result.put("success", false);
                    result.put("message", "Không tìm thấy đơn hàng với mã: " + maDonParam);
                    result.put("items", new java.util.ArrayList<>());
                }
                return result;
            }

            // Đã đăng nhập - lấy danh sách đơn hàng của user
            String trangThaiParam = (trangThai != null && !trangThai.isBlank()) ? trangThai : null;
            String thanhToanParam = (thanhToan != null && !thanhToan.isBlank()) ? thanhToan : null;

            Pageable pageable = PageRequest.of(page, 6);

            Page<HoaDonDTO> pageHoaDon =
                    hoaDonService.filterDonHang(userId, trangThaiParam, thanhToanParam, ngay, maDonParam, pageable);

            // Batch check đánh giá - chỉ 1 query thay vì N+1
            Map<Integer, Boolean> daDanhGiaMapApi = new HashMap<>();
            if (pageHoaDon.hasContent()) {
                Set<Integer> allSpctIds = pageHoaDon.getContent().stream()
                        .filter(hd -> hd.getItems() != null)
                        .flatMap(hd -> hd.getItems().stream())
                        .map(item -> item.getSanPhamChiTietId())
                        .filter(id -> id != null)
                        .collect(Collectors.toSet());

                Set<Integer> reviewedIds = danhGiaService.getReviewedProductIds(allSpctIds, userId);

                for (HoaDonDTO hoaDon : pageHoaDon.getContent()) {
                    boolean daDanhGia = false;
                    if (hoaDon.getItems() != null) {
                        for (var item : hoaDon.getItems()) {
                            if (item.getSanPhamChiTietId() != null && reviewedIds.contains(item.getSanPhamChiTietId())) {
                                daDanhGia = true;
                                break;
                            }
                        }
                    }
                    daDanhGiaMapApi.put(hoaDon.getId(), daDanhGia);
                }
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

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Có lỗi xảy ra: " + (e.getMessage() != null ? e.getMessage() : "Lỗi không xác định"));
            result.put("items", new java.util.ArrayList<>());
        }

        return result;
    }

    private Map<String, Object> convertHoaDonToMap(HoaDonDTO hoaDon, boolean daDanhGia) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", hoaDon.getId());
        map.put("maDonHang", hoaDon.getMaDonHang());
        map.put("ngayDat", hoaDon.getNgayDat() != null ? hoaDon.getNgayDat().toString() : "");
        map.put("tongTienThanhToan", hoaDon.getTongTienThanhToan());
        map.put("trangThaiDonHang", hoaDon.getTrangThaiDonHang());
        map.put("ghiChu", hoaDon.getGhiChu());
        map.put("phuongThucThanhToan", hoaDon.getPhuongThucThanhToan());
        map.put("tenTrangThai", getTenTrangThai(hoaDon.getTrangThaiDonHang()));
        map.put("daDanhGia", daDanhGia);
        if (hoaDon.getItems() != null) {
            map.put("items", hoaDon.getItems().stream()
                    .map(item -> {
                        Map<String, Object> itemMap = new HashMap<>();
                        itemMap.put("tenSanPham", item.getTenSanPham());
                        itemMap.put("tenBienThe", item.getTenBienThe());
                        return itemMap;
                    })
                    .collect(Collectors.toList()));
        }
        return map;
    }

    private String getTenTrangThai(String trangThai) {
        if (trangThai == null) return "";
        return switch (trangThai) {
            case "CAN_XU_LY" -> "Cần xử lý";
            case "CHO_THANH_TOAN" -> "Chờ thanh toán";
            case "CHO_XAC_NHAN" -> "Chờ xác nhận";
            case "DA_THANH_TOAN", "DA THANH TOAN" -> "Đã thanh toán";
            case "DA_XAC_NHAN" -> "Đã xác nhận";
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

    /** Trang chỉnh sửa đơn hàng (trạng thái Cần xử lý): hiển thị form sửa số lượng theo tồn kho. */
    @GetMapping("/don-hang/chinh-sua/{id}")
    public String chinhSuaDonHangPage(@PathVariable Integer id, HttpSession session, Model model, RedirectAttributes redirect) {
        Integer userId = (Integer) session.getAttribute(SESSION_CURRENT_USER_ID);
        if (userId == null) {
            redirect.addFlashAttribute("error", "Vui lòng đăng nhập.");
            return "redirect:/dang-nhap";
        }
        HoaDonDTO hoaDon = hoaDonService.getById(id);
        if (hoaDon.getKhachHangId() == null || !hoaDon.getKhachHangId().equals(userId)) {
            redirect.addFlashAttribute("error", "Bạn không có quyền chỉnh sửa đơn hàng này.");
            return "redirect:/don-hang";
        }
        if (!"CAN_XU_LY".equals(hoaDon.getTrangThaiDonHang())) {
            redirect.addFlashAttribute("error", "Chỉ có thể chỉnh sửa đơn hàng ở trạng thái Cần xử lý.");
            return "redirect:/don-hang";
        }
        if (hoaDon.getItems() == null || hoaDon.getItems().isEmpty()) {
            redirect.addFlashAttribute("error", "Đơn hàng không có sản phẩm.");
            return "redirect:/don-hang";
        }

        model.addAttribute("hoaDon", hoaDon);
        model.addAttribute("title", "Chỉnh sửa đơn hàng");
        model.addAttribute("content", "user/chinh-sua-don-hang");
        return "layout/user-layout";
    }

    /** Xử lý form chỉnh sửa đơn hàng: cập nhật số lượng + thanh toán lại. */
    @PostMapping("/don-hang/chinh-sua/{id}")
    public String chinhSuaDonHangSubmit(
            @PathVariable Integer id,
            @RequestParam String itemsData,
            HttpSession session,
            RedirectAttributes redirect) {

        Integer userId = (Integer) session.getAttribute(SESSION_CURRENT_USER_ID);
        if (userId == null) {
            redirect.addFlashAttribute("error", "Vui lòng đăng nhập.");
            return "redirect:/dang-nhap";
        }

        // Parse itemsData: JSON array [{id, spctId, soLuong, giaBan}, ...]
        Map<Integer, Integer> itemsMap = new HashMap<>();
        java.util.List<Map<String, Object>> parsedItems;
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            parsedItems = mapper.readValue(itemsData,
                    mapper.getTypeFactory().constructCollectionType(java.util.List.class, Map.class));
            for (Map<String, Object> item : parsedItems) {
                Object spctIdObj = item.get("spctId");
                Object soLuongObj = item.get("soLuong");
                if (spctIdObj != null && soLuongObj != null) {
                    Integer spctId = Integer.valueOf(spctIdObj.toString());
                    Integer soLuong = Integer.valueOf(soLuongObj.toString());
                    itemsMap.put(spctId, soLuong);
                }
            }
        } catch (Exception e) {
            redirect.addFlashAttribute("error", "Dữ liệu không hợp lệ.");
            return "redirect:/don-hang/chinh-sua/" + id;
        }

        // Có phần tử JSON nhưng không map được spctId -> tránh xóa nhầm toàn bộ chi tiết đơn (lỗi form cũ / thiếu data-spct-id)
        if (parsedItems != null && !parsedItems.isEmpty() && itemsMap.isEmpty()) {
            redirect.addFlashAttribute("error",
                    "Thiếu thông tin sản phẩm (mã biến thể). Vui lòng tải lại trang và thử lại.");
            return "redirect:/don-hang/chinh-sua/" + id;
        }

        // itemsMap rỗng khi user xóa hết dòng trên giao diện (mảng JSON []) -> service chuyển đơn sang Đã hủy
        try {
            HoaDonDTO updatedHoaDon = hoaDonService.editOrderItems(id, itemsMap);
            if ("DA_HUY".equals(updatedHoaDon.getTrangThaiDonHang())) {
                redirect.addFlashAttribute("success", "Đơn hàng " + updatedHoaDon.getMaDonHang() + " đã được hủy do không còn sản phẩm nào. Cảm ơn bạn đã sử dụng dịch vụ.");
            } else {
                redirect.addFlashAttribute("success", "Đơn hàng " + updatedHoaDon.getMaDonHang() + " đã được cập nhật và chuyển sang trạng thái chờ xác nhận. Vui lòng đợi xác nhận từ cửa hàng.");
            }
            return "redirect:/don-hang/chi-tiet/" + id;
        } catch (Exception e) {
            redirect.addFlashAttribute("error", e.getMessage() != null ? e.getMessage() : "Có lỗi xảy ra khi cập nhật đơn hàng.");
            return "redirect:/don-hang/chinh-sua/" + id;
        }
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
        result.put("loaiHoaDon", hoaDon.getLoaiHoaDon());
        result.put("tenKhachHang", hoaDon.getTenKhachHang());
        result.put("sdtKhachHang", hoaDon.getSdtKhachHang());
        result.put("diaChi", hoaDon.getDiaChi());
        result.put("email", hoaDon.getEmail());
        result.put("gioiTinh", hoaDon.getGioiTinh());
        result.put("ngaySinh", hoaDon.getNgaySinh() != null ? hoaDon.getNgaySinh().toString() : "");
        result.put("tongTienTamTinh", hoaDon.getTongTienTamTinh());
        result.put("tongTienChuaGiam", hoaDon.getTongTienChuaGiam());
        result.put("tienGiam", hoaDon.getTienGiam());
        result.put("phiVanChuyen", hoaDon.getPhiVanChuyen());
        result.put("tongTienThanhToan", hoaDon.getTongTienThanhToan());
        result.put("trangThaiDonHang", hoaDon.getTrangThaiDonHang());
        result.put("ghiChu", hoaDon.getGhiChu());
        result.put("tenTrangThai", getTenTrangThai(hoaDon.getTrangThaiDonHang()));
        result.put("phuongThucThanhToan", hoaDon.getPhuongThucThanhToan());
        result.put("tenPhuongThuc", hoaDon.getPhuongThucThanhToan() != null && hoaDon.getPhuongThucThanhToan().equals("COD") ? "Thanh toán khi nhận hàng (COD)" : "VNPay");

        if (hoaDon.getItems() != null) {
            result.put("items", hoaDon.getItems().stream()
                    .map(item -> {
                        Map<String, Object> itemMap = new HashMap<>();
                        itemMap.put("tenSanPham", item.getTenSanPham());
                        itemMap.put("tenBienThe", item.getTenBienThe());
                        itemMap.put("soLuong", item.getSoLuong());
                        itemMap.put("donGia", item.getDonGia());
                        itemMap.put("giaGoc", item.getGiaGoc());
                        itemMap.put("thanhTien", item.getThanhTien());
                        return itemMap;
                    })
                    .collect(Collectors.toList()));
        }

        return result;
    }
}
