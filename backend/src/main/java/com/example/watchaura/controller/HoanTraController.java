package com.example.watchaura.controller;

import com.example.watchaura.dto.HoanTraDTO;
import com.example.watchaura.dto.HoanTraRequest;
import com.example.watchaura.service.HoanTraService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.watchaura.controller.AuthController.SESSION_CURRENT_USER_ID;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin/hoan-tra")
@RequiredArgsConstructor
public class HoanTraController {

    private final HoanTraService hoanTraService;

    @GetMapping
    public String listHoanTra(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String trangThai,
            Model model) {
        
        List<HoanTraDTO> list;
        if (trangThai != null && !trangThai.isEmpty()) {
            list = hoanTraService.getHoanTraByTrangThai(trangThai);
            model.addAttribute("currentTrangThai", trangThai);
        } else {
            list = hoanTraService.getAllHoanTra();
        }
        model.addAttribute("listHoanTra", list);

        long choXuLy = list.stream().filter(h -> "CHO_XU_LY".equals(h.getTrangThai())).count();
        long dangXuLy = list.stream().filter(h -> "DANG_XU_LY".equals(h.getTrangThai())).count();
        long daXuLy = list.stream().filter(h -> "DA_XU_LY".equals(h.getTrangThai())).count();
        long tuChoi = list.stream().filter(h -> "TU_CHOI".equals(h.getTrangThai())).count();
        model.addAttribute("statChoXuLy", choXuLy);
        model.addAttribute("statDangXuLy", dangXuLy);
        model.addAttribute("statDaXuLy", daXuLy);
        model.addAttribute("statTuChoi", tuChoi);

        // DOI_HANG stats
        long choDuyetDoi = list.stream().filter(h -> "DOI_HANG".equals(h.getLoaiHoanTra()) && "CHO_DUYET_DOI".equals(h.getTrangThai())).count();
        long daDuyetDoi = list.stream().filter(h -> "DOI_HANG".equals(h.getLoaiHoanTra()) && "DA_DUYET_DOI".equals(h.getTrangThai())).count();
        long daNhanHangDoi = list.stream().filter(h -> "DOI_HANG".equals(h.getLoaiHoanTra()) && "DA_NHAN_HANG_DOI".equals(h.getTrangThai())).count();
        long chonSerialMoi = list.stream().filter(h -> "DOI_HANG".equals(h.getLoaiHoanTra()) && "CHON_SERIAL_MOI".equals(h.getTrangThai())).count();
        long daDoi = list.stream().filter(h -> "DOI_HANG".equals(h.getLoaiHoanTra()) && "DA_DOI".equals(h.getTrangThai())).count();
        long ketThucDoi = list.stream().filter(h -> "DOI_HANG".equals(h.getLoaiHoanTra()) && "KET_THUC".equals(h.getTrangThai())).count();

        model.addAttribute("statChoDuyetDoi", choDuyetDoi);
        model.addAttribute("statDaDuyetDoi", daDuyetDoi);
        model.addAttribute("statDaNhanHangDoi", daNhanHangDoi);
        model.addAttribute("statChonSerialMoi", chonSerialMoi);
        model.addAttribute("statDaDoi", daDoi);
        model.addAttribute("statKetThucDoi", ketThucDoi);

        model.addAttribute("statTongSo", (long) list.size());

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", (int) Math.ceil((double) list.size() / size));
        
        return "admin/hoan-tra";
    }

    @GetMapping("/chi-tiet/{id}")
    public String hoanTraDetail(@PathVariable Integer id, Model model) {
        HoanTraDTO hoanTra = hoanTraService.getHoanTraById(id);
        model.addAttribute("hoanTra", hoanTra);
        model.addAttribute("title", "Chi tiết hoàn trả");
        model.addAttribute("content", "admin/hoan-tra-detail");
        return "layout/admin-layout";
    }

    @GetMapping("/tao-moi")
    public String createHoanTraForm(Model model) {
        model.addAttribute("title", "Tạo yêu cầu hoàn trả");
        model.addAttribute("content", "admin/hoan-tra-create");
        return "layout/admin-layout";
    }

    @GetMapping("/api/hoa-don-co-the-hoan")
    @ResponseBody
    public Map<String, Object> getHoaDonCoTheHoanTra() {
        try {
            Map<String, Object> result = hoanTraService.getHoaDonCoTheHoanTraForAdmin();
            return result;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return error;
        }
    }

    @GetMapping("/api/hoa-don/{id}/chi-tiet")
    @ResponseBody
    public Map<String, Object> getHoaDonChiTietForAdmin(@PathVariable Integer id) {
        try {
            Map<String, Object> result = hoanTraService.getHoaDonChiTietForAdmin(id);
            return result;
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return error;
        }
    }

    @PostMapping("/tao-moi")
    public String submitHoanTraAdmin(
            @RequestParam Integer idHoaDon,
            @RequestParam String lyDo,
            @RequestParam(required = false) String chiTietJson,
            @RequestParam(required = false) String ghiChuXuLy,
            @RequestParam(required = false, defaultValue = "TRA_HANG") String loaiHoanTra,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        
        try {
            HoanTraRequest request = new HoanTraRequest();
            request.setIdHoaDon(idHoaDon);
            request.setLyDo(lyDo);
            request.setGhiChuXuLy(ghiChuXuLy);
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

                    chiTietList.add(ct);
                }
                request.setChiTietList(chiTietList);
            }

            HoanTraDTO created = hoanTraService.createHoanTra(request);
            redirectAttributes.addFlashAttribute("successMessage", "Tạo hoàn trả '" + created.getMaHoanTra() + "' thành công!");
            return "redirect:/admin/hoan-tra/chi-tiet/" + created.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + (e.getMessage() != null ? e.getMessage() : "Không thể tạo hoàn trả"));
            return "redirect:/admin/hoan-tra/tao-moi";
        }
    }

    @PostMapping("/xu-ly/{id}")
    public String xuLyHoanTra(
            @PathVariable Integer id,
            @RequestParam(required = false) String ghiChuXuLy,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            Integer nhanVienId = (Integer) session.getAttribute(SESSION_CURRENT_USER_ID);
            if (nhanVienId == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Bạn chưa đăng nhập!");
                return "redirect:/auth/login";
            }
            hoanTraService.xuLyHoanTra(id, ghiChuXuLy, nhanVienId);
            redirectAttributes.addFlashAttribute("successMessage", "Xử lý hoàn trả thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/hoan-tra";
    }

    @PostMapping("/tu-choi/{id}")
    public String tuChoiHoanTra(
            @PathVariable Integer id,
            @RequestParam(required = false) String ghiChuXuLy,
            HttpSession session,
            RedirectAttributes redirectAttributes) {
        try {
            Integer nhanVienId = (Integer) session.getAttribute(SESSION_CURRENT_USER_ID);
            if (nhanVienId == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Bạn chưa đăng nhập!");
                return "redirect:/auth/login";
            }
            hoanTraService.tuChoiHoanTra(id, ghiChuXuLy, nhanVienId);
            redirectAttributes.addFlashAttribute("successMessage", "Từ chối hoàn trả thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/hoan-tra";
    }

    @GetMapping("/thong-ke")
    public String thongKe(Model model) {
        long choXuLy = hoanTraService.getAllHoanTra().stream()
                .filter(h -> "CHO_XU_LY".equals(h.getTrangThai())).count();
        long daXuLy = hoanTraService.getAllHoanTra().stream()
                .filter(h -> "DA_XU_LY".equals(h.getTrangThai())).count();
        long tuChoi = hoanTraService.getAllHoanTra().stream()
                .filter(h -> "TU_CHOI".equals(h.getTrangThai())).count();

        model.addAttribute("choXuLy", choXuLy);
        model.addAttribute("daXuLy", daXuLy);
        model.addAttribute("tuChoi", tuChoi);
        model.addAttribute("tongSo", choXuLy + daXuLy + tuChoi);

        return "admin/hoan-tra-thong-ke";
    }

    @GetMapping("/chi-tiet/{id}/in")
    public String inPhieuHoanTra(@PathVariable Integer id, Model model) {
        HoanTraDTO hoanTra = hoanTraService.getHoanTraById(id);
        model.addAttribute("hoanTra", hoanTra);
        return "admin/hoan-tra-print";
    }

}
