package com.example.watchaura.controller;

import com.example.watchaura.dto.HoanTraDTO;
import com.example.watchaura.dto.HoanTraRequest;
import com.example.watchaura.service.HoanTraService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.watchaura.controller.AuthController.SESSION_CURRENT_USER_ID;

@Controller
@RequestMapping("/khach-hang")
@RequiredArgsConstructor
public class UserHoanTraController {

    private final HoanTraService hoanTraService;

    @GetMapping("/hoan-tra")
    public String listHoanTra(Model model, HttpSession session) {
        Integer userId = (Integer) session.getAttribute(SESSION_CURRENT_USER_ID);
        if (userId == null) {
            return "redirect:/dang-nhap";
        }

        List<HoanTraDTO> list = hoanTraService.getHoanTraByKhachHangId(userId);
        model.addAttribute("listHoanTra", list);
        model.addAttribute("title", "Yêu cầu hoàn trả");
        model.addAttribute("content", "user/hoan-tra-list :: content");
        return "layout/user-layout";
    }

    @GetMapping("/hoan-tra/{id}")
    public String hoanTraDetail(@PathVariable Integer id, Model model, HttpSession session) {
        Integer userId = (Integer) session.getAttribute(SESSION_CURRENT_USER_ID);
        if (userId == null) {
            return "redirect:/dang-nhap";
        }

        HoanTraDTO hoanTra = hoanTraService.getHoanTraById(id);
        
        if (hoanTra.getIdKhachHang() == null || !hoanTra.getIdKhachHang().equals(userId)) {
            return "redirect:/khach-hang/hoan-tra";
        }

        model.addAttribute("hoanTra", hoanTra);
        model.addAttribute("title", "Chi tiết hoàn trả");
        model.addAttribute("content", "user/hoan-tra-detail :: content");
        return "layout/user-layout";
    }

    @GetMapping("/hoan-tra/tao-moi/{hoaDonId}")
    public String createHoanTraForm(@PathVariable Integer hoaDonId, Model model, HttpSession session) {
        Integer userId = (Integer) session.getAttribute(SESSION_CURRENT_USER_ID);
        if (userId == null) {
            return "redirect:/dang-nhap";
        }

        try {
            Map<String, Object> hoaDonInfo = hoanTraService.getHoaDonChoKhachHangTra(hoaDonId, userId);
            if (Boolean.FALSE.equals(hoaDonInfo.get("success"))) {
                return "redirect:/don-hang";
            }
            model.addAttribute("hoaDonInfo", hoaDonInfo);
            model.addAttribute("title", "Tạo yêu cầu hoàn trả");
            model.addAttribute("content", "user/hoan-tra-create :: content");
            return "layout/user-layout";
        } catch (Exception e) {
            return "redirect:/don-hang";
        }
    }

    @PostMapping("/hoan-tra/tao-moi")
    public String submitHoanTra(
            @RequestParam Integer idHoaDon,
            @RequestParam String lyDo,
            @RequestParam(required = false) String chiTietJson,
            @RequestParam(required = false, defaultValue = "TRA_HANG") String loaiHoanTra,
            @RequestParam(required = false) String hinhAnhJson,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        Integer userId = (Integer) session.getAttribute(SESSION_CURRENT_USER_ID);
        if (userId == null) {
            return "redirect:/dang-nhap";
        }

        try {
            HoanTraRequest request = new HoanTraRequest();
            request.setIdHoaDon(idHoaDon);
            request.setLyDo(lyDo);
            request.setLoaiHoanTra(loaiHoanTra);

            if (chiTietJson != null && !chiTietJson.isEmpty()) {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                java.util.List<Map<String, Object>> chiTietData = mapper.readValue(
                        chiTietJson,
                        mapper.getTypeFactory().constructCollectionType(java.util.List.class, Map.class)
                );

                java.util.List<HoanTraRequest.HoanTraChiTietRequest> chiTietList = new java.util.ArrayList<>();
                for (Map<String, Object> item : chiTietData) {
                    HoanTraRequest.HoanTraChiTietRequest ct = new HoanTraRequest.HoanTraChiTietRequest();
                    ct.setIdHoaDonChiTiet(Integer.valueOf(item.get("idHoaDonChiTiet").toString()));
                    ct.setIdSanPhamChiTiet(Integer.valueOf(item.get("idSanPhamChiTiet").toString()));
                    ct.setSoLuongHoanTra(Integer.valueOf(item.get("soLuongHoanTra").toString()));

                    if (item.get("donGia") != null) {
                        ct.setDonGiaTaiThoiDiemMua(new java.math.BigDecimal(item.get("donGia").toString()));
                    }

                    if (item.get("serials") != null) {
                        @SuppressWarnings("unchecked")
                        List<String> serials = (List<String>) item.get("serials");
                        ct.setSerialsHoanTra(serials);
                    }

                    if (item.get("hinhAnh") != null) {
                        ct.setHinhAnh((String) item.get("hinhAnh"));
                    }

                    chiTietList.add(ct);
                }
                request.setChiTietList(chiTietList);
            }

            HoanTraDTO created = hoanTraService.createHoanTraKhachHang(request, userId);
            redirectAttributes.addFlashAttribute("success", "Yêu cầu hoàn trả '" + created.getMaHoanTra() + "' đã được gửi thành công!");
            return "redirect:/khach-hang/hoan-tra/" + created.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + (e.getMessage() != null ? e.getMessage() : "Không thể tạo yêu cầu hoàn trả"));
            return "redirect:/khach-hang/hoan-tra/tao-moi/" + idHoaDon;
        }
    }

    @GetMapping("/hoan-tra/api/{id}")
    @ResponseBody
    public Map<String, Object> hoanTraDetailApi(@PathVariable Integer id, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        Integer userId = (Integer) session.getAttribute(SESSION_CURRENT_USER_ID);
        
        if (userId == null) {
            result.put("success", false);
            result.put("message", "Vui lòng đăng nhập");
            return result;
        }

        try {
            HoanTraDTO hoanTra = hoanTraService.getHoanTraById(id);
            
            if (hoanTra.getIdKhachHang() == null || !hoanTra.getIdKhachHang().equals(userId)) {
                result.put("success", false);
                result.put("message", "Bạn không có quyền xem yêu cầu hoàn trả này");
                return result;
            }

            result.put("success", true);
            result.put("id", hoanTra.getId());
            result.put("maHoanTra", hoanTra.getMaHoanTra());
            result.put("idHoaDon", hoanTra.getIdHoaDon());
            result.put("maDonHang", hoanTra.getMaDonHang());
            result.put("lyDo", hoanTra.getLyDo());
            result.put("soTienHoanTra", hoanTra.getSoTienHoanTra());
            result.put("trangThai", hoanTra.getTrangThai());
            result.put("trangThaiHienThi", hoanTra.getTrangThaiHienThi());
            result.put("ghiChuXuLy", hoanTra.getGhiChuXuLy());
            result.put("ngayYeuCau", hoanTra.getNgayYeuCau());
            result.put("ngayXuLy", hoanTra.getNgayXuLy());
            result.put("tenNhanVienXuLy", hoanTra.getTenNhanVienXuLy());
            
            // Các trường cần thiết cho Timeline
            result.put("loaiHoanTra", hoanTra.getLoaiHoanTra());
            result.put("loaiHoanTraHienThi", hoanTra.getLoaiHoanTraHienThi());
            result.put("phuongThucHoanTienHienThi", hoanTra.getPhuongThucHoanTienHienThi());
            result.put("soTienHoanThucTe", hoanTra.getSoTienHoanThucTe());
            result.put("ngayHoanTien", hoanTra.getNgayHoanTien());
            result.put("serialsMoiList", hoanTra.getSerialsMoiList());

            if (hoanTra.getChiTietList() != null) {
                List<Map<String, Object>> chiTietList = hoanTra.getChiTietList().stream()
                        .map(ct -> {
                            Map<String, Object> item = new HashMap<>();
                            item.put("id", ct.getId());
                            item.put("tenSanPham", ct.getTenSanPham());
                            item.put("maSanPham", ct.getMaSanPham());
                            item.put("tenBienThe", ct.getTenBienThe());
                            item.put("mauSac", ct.getMauSac());
                            item.put("kichThuoc", ct.getKichThuoc());
                            item.put("chatLieuDay", ct.getChatLieuDay());
                            item.put("soLuongHoanTra", ct.getSoLuongHoanTra());
                            item.put("donGiaTaiThoiDiemMua", ct.getDonGiaTaiThoiDiemMua());
                            item.put("soTienHoan", ct.getSoTienHoan());
                            item.put("serialsHoanTra", ct.getSerialsHoanTra());
                            // Serial mới cho đổi hàng
                            item.put("serialMoi", ct.getSerialMoi());
                            if (ct.getSerialsChiTiet() != null) {
                                item.put("serialsChiTiet", ct.getSerialsChiTiet().stream()
                                        .map(s -> {
                                            Map<String, Object> sm = new HashMap<>();
                                            sm.put("maSerial", s.getMaSerial());
                                            sm.put("trangThaiHienThi", s.getTrangThaiHienThi());
                                            sm.put("daDuocChon", s.getDaDuocChon());
                                            return sm;
                                        })
                                        .collect(java.util.stream.Collectors.toList()));
                            }
                            return item;
                        })
                        .collect(java.util.stream.Collectors.toList());
                result.put("chiTietList", chiTietList);
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @GetMapping("/hoan-tra/api/list")
    @ResponseBody
    public Map<String, Object> listHoanTraApi(HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        Integer userId = (Integer) session.getAttribute(SESSION_CURRENT_USER_ID);
        
        if (userId == null) {
            result.put("success", false);
            result.put("message", "Vui lòng đăng nhập");
            return result;
        }

        try {
            List<HoanTraDTO> list = hoanTraService.getHoanTraByKhachHangId(userId);
            result.put("success", true);
            result.put("items", list.stream()
                    .map(ht -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", ht.getId());
                        item.put("maHoanTra", ht.getMaHoanTra());
                        item.put("maDonHang", ht.getMaDonHang());
                        item.put("lyDo", ht.getLyDo());
                        item.put("soTienHoanTra", ht.getSoTienHoanTra());
                        item.put("trangThai", ht.getTrangThai());
                        item.put("trangThaiHienThi", ht.getTrangThaiHienThi());
                        item.put("ngayYeuCau", ht.getNgayYeuCau());
                        item.put("ngayXuLy", ht.getNgayXuLy());
                        item.put("tenNhanVienXuLy", ht.getTenNhanVienXuLy());
                        item.put("soLuongSanPham", ht.getChiTietList() != null ? ht.getChiTietList().size() : 0);
                        return item;
                    })
                    .collect(java.util.stream.Collectors.toList()));
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }

    @GetMapping("/hoan-tra/api/paged")
    @ResponseBody
    public Map<String, Object> listHoanTraPagedApi(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        Integer userId = (Integer) session.getAttribute(SESSION_CURRENT_USER_ID);
        
        if (userId == null) {
            result.put("success", false);
            result.put("message", "Vui lòng đăng nhập");
            return result;
        }

        try {
            List<HoanTraDTO> allList = hoanTraService.getHoanTraByKhachHangId(userId);
            int totalElements = allList.size();
            int totalPages = (int) Math.ceil((double) totalElements / size);
            
            int start = page * size;
            int end = Math.min(start + size, totalElements);
            
            List<HoanTraDTO> pagedList = start < totalElements 
                    ? allList.subList(start, end) 
                    : java.util.Collections.emptyList();
            
            List<Map<String, Object>> items = pagedList.stream()
                    .map(ht -> {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", ht.getId());
                        item.put("maHoanTra", ht.getMaHoanTra());
                        item.put("maDonHang", ht.getMaDonHang());
                        item.put("lyDo", ht.getLyDo());
                        item.put("soTienHoanTra", ht.getSoTienHoanTra());
                        item.put("trangThai", ht.getTrangThai());
                        item.put("trangThaiHienThi", ht.getTrangThaiHienThi());
                        item.put("ngayYeuCau", ht.getNgayYeuCau());
                        item.put("ngayXuLy", ht.getNgayXuLy());
                        item.put("tenNhanVienXuLy", ht.getTenNhanVienXuLy());
                        item.put("loaiHoanTra", ht.getLoaiHoanTra());
                        item.put("loaiHoanTraHienThi", ht.getLoaiHoanTraHienThi());
                        item.put("soLuongSanPham", ht.getChiTietList() != null ? ht.getChiTietList().size() : 0);
                        return item;
                    })
                    .collect(java.util.stream.Collectors.toList());
            
            result.put("success", true);
            result.put("items", items);
            result.put("totalElements", totalElements);
            result.put("totalPages", totalPages);
            result.put("currentPage", page);
            result.put("pageSize", size);
            result.put("hasNext", page < totalPages - 1);
            result.put("hasPrevious", page > 0);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }
        return result;
    }
}
