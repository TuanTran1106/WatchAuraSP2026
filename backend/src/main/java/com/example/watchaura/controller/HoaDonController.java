package com.example.watchaura.controller;

import com.example.watchaura.dto.HoaDonDTO;
import com.example.watchaura.dto.HoaDonRequest;
import com.example.watchaura.util.PaginationWindow;
import com.example.watchaura.entity.Voucher;
import com.example.watchaura.service.HoaDonService;
import com.example.watchaura.service.KhachHangService;
import com.example.watchaura.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/hoa-don")
@RequiredArgsConstructor
public class HoaDonController {

    private final HoaDonService hoaDonService;
    private final KhachHangService khachHangService;
    private final VoucherService voucherService;

    private static final int PAGE_SIZE = 10;

    @GetMapping
    public String list(@RequestParam(required = false) String q,
                      @RequestParam(required = false) String trangThai,
                      @RequestParam(defaultValue = "0") int page,
                      Model model,
                      @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id"));
        Page<HoaDonDTO> pageResult = hoaDonService.searchPage(q, trangThai, pageable);
        model.addAttribute("title", "Hóa đơn");
        model.addAttribute("content", "admin/hoadon-list");
        model.addAttribute("list", pageResult.getContent());
        model.addAttribute("page", pageResult);
        model.addAttribute("paginationItems", PaginationWindow.build(pageResult, 2));
        model.addAttribute("searchKeyword", q != null ? q : "");
        model.addAttribute("filterTrangThai", trangThai != null ? trangThai : "");
        if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
            return "admin/hoadon-list :: content";
        }
        return "layout/admin-layout";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        HoaDonDTO dto = hoaDonService.getById(id);
        model.addAttribute("title", "Chi tiết hóa đơn");
        model.addAttribute("content", "admin/hoadon-detail");
        model.addAttribute("hoaDon", dto);
        return "layout/admin-layout";
    }

    private static String statusToCssClass(String trangThai) {
        if (trangThai == null) return "";
        return switch (trangThai) {
            case "CHO_XAC_NHAN", "CHO_THANH_TOAN", "DRAFT_OFFLINE" -> "invoice-detail__status--cho-xac-nhan";
            case "DA_XAC_NHAN" -> "invoice-detail__status--da-xac-nhan";
            case "DANG_GIAO" -> "invoice-detail__status--dang-giao";
            case "DA_GIAO", "DA THANH TOAN", "DA_THANH_TOAN" -> "invoice-detail__status--da-giao";
            case "DA_HUY" -> "invoice-detail__status--da-huy";
            case "CAN_XU_LY" -> "invoice-detail__status--dang-xu-ly";
            default -> "";
        };
    }

    @GetMapping("/{id}/sua")
    public String formEdit(@PathVariable Integer id, Model model) {
        HoaDonDTO dto = hoaDonService.getById(id);
        HoaDonRequest request = new HoaDonRequest();
        request.setKhachHangId(dto.getKhachHangId());
        request.setNhanVienId(dto.getNhanVienId());
        request.setVoucherId(dto.getVoucherId());
        request.setPhuongThucThanhToan(dto.getPhuongThucThanhToan());
        request.setLoaiHoaDon(dto.getLoaiHoaDon());
        request.setDiaChi(dto.getDiaChi());
        request.setTenKhachHang(dto.getTenKhachHang());
        request.setSdtKhachHang(dto.getSdtKhachHang());
        request.setGhiChu(dto.getGhiChu());
        request.setItems(dto.getItems() != null ? dto.getItems().stream()
                .map(ct -> new com.example.watchaura.dto.HoaDonChiTietRequest(
                        ct.getSanPhamChiTietId(), ct.getSoLuong()))
                .toList() : Collections.emptyList());
        model.addAttribute("title", "Sửa hóa đơn");
        model.addAttribute("content", "admin/hoadon-form");
        model.addAttribute("hoaDon", request);
        model.addAttribute("hoaDonId", id);
        model.addAttribute("khachHangList", khachHangService.getAll());
        Page<Voucher> voucherPage =
                voucherService.findAll(PageRequest.of(0, 100));
        model.addAttribute("voucherList", voucherPage.getContent());

        model.addAttribute("formAction", "/admin/hoa-don/" + id);
        return "layout/admin-layout";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id, @Valid @ModelAttribute("hoaDon") HoaDonRequest request, BindingResult result, Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            model.addAttribute("title", "Sửa hóa đơn");
            model.addAttribute("content", "admin/hoadon-form");
            model.addAttribute("hoaDon", request);
            model.addAttribute("hoaDonId", id);
            model.addAttribute("khachHangList", khachHangService.getAll());
            Page<Voucher> voucherPage =
                    voucherService.findAll(PageRequest.of(0, 100));

            model.addAttribute("voucherList", voucherPage.getContent());
            model.addAttribute("formAction", "/admin/hoa-don/" + id);
            return "layout/admin-layout";
        }
        hoaDonService.update(id, request);
        redirect.addFlashAttribute("message", "Cập nhật hóa đơn thành công.");
        return "redirect:/admin/hoa-don";
    }

    @GetMapping("/{id}/pdf")
    public void exportPdf(@PathVariable Integer id, jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        HoaDonDTO dto = hoaDonService.getById(id);
        byte[] pdf = hoaDonService.exportPdf(dto);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"hoadon-" + dto.getMaDonHang() + ".pdf\"");
        response.setContentLength(pdf.length);
        response.getOutputStream().write(pdf);
        response.getOutputStream().flush();
    }

    @GetMapping("/bao-cao/pdf")
    public void exportRevenueReportPdf(
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false, defaultValue = "DA_GIAO") String status,
            jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        LocalDate from = parseDate(fromDate);
        LocalDate to = parseDate(toDate);

        List<HoaDonDTO> filtered = hoaDonService.getAll().stream()
                .filter(o -> status == null || status.isBlank() || status.equals(o.getTrangThaiDonHang()))
                .filter(o -> {
                    if (o.getNgayDat() == null) return false;
                    LocalDate d = o.getNgayDat().toLocalDate();
                    return (from == null || !d.isBefore(from)) && (to == null || !d.isAfter(to));
                })
                .sorted((a, b) -> b.getNgayDat().compareTo(a.getNgayDat()))
                .collect(Collectors.toList());

        byte[] pdf = hoaDonService.exportRevenueReportPdf(filtered, from, to, status);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"bao-cao-doanh-thu.pdf\"");
        response.setContentLength(pdf.length);
        response.getOutputStream().write(pdf);
        response.getOutputStream().flush();
    }

    private LocalDate parseDate(String input) {
        if (input == null || input.isBlank()) return null;
        try {
            return LocalDate.parse(input.trim());
        } catch (Exception e) {
            return null;
        }
    }

    @PostMapping("/{id}/trang-thai")
    public String updateTrangThai(@PathVariable Integer id, @RequestParam String trangThai,
                                  @RequestParam(required = false) String q,
                                  @RequestParam(required = false) String trangThaiFilter,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(required = false) String tuChiTiet,
                                  Model model,
                                  RedirectAttributes redirect,
                                  @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
        boolean veChiTiet = "1".equals(tuChiTiet)
                || (tuChiTiet != null && "true".equalsIgnoreCase(tuChiTiet.trim()));
        try {
            HoaDonDTO updated = hoaDonService.updateTrangThaiDonHang(id, trangThai);

            // Kiểm tra nếu đơn bị chuyển sang "Cần xử lý" do không đủ tồn kho
            boolean chuyenCanXuLy = "CAN_XU_LY".equals(updated.getTrangThaiDonHang())
                    && ("DA_XAC_NHAN".equals(trangThai)
                    || "DA THANH TOAN".equals(trangThai)
                    || "DA_THANH_TOAN".equals(trangThai));
            String msg = chuyenCanXuLy
                    ? ("Không đủ tồn kho! Đơn hàng đã chuyển sang \"Cần xử lý\". Vui lòng liên hệ khách hàng để giảm số lượng hoặc hủy đơn.")
                    : "Đã cập nhật trạng thái đơn hàng.";

            if (veChiTiet) {
                if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
                    HoaDonDTO dto = hoaDonService.getById(id);
                    model.addAttribute("hoaDon", dto);
                    model.addAttribute("hoaDonStatusClass", statusToCssClass(dto.getTrangThaiDonHang()));
                    model.addAttribute(chuyenCanXuLy ? "warning" : "message", msg);
                    return "admin/hoadon-detail :: content";
                }
                redirect.addFlashAttribute(chuyenCanXuLy ? "warning" : "message", msg);
                return "redirect:/admin/hoa-don/" + id;
            }

            if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
                Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id"));
                Page<HoaDonDTO> pageResult = hoaDonService.searchPage(q, trangThaiFilter, pageable);
                model.addAttribute("title", "Hóa đơn");
                model.addAttribute("content", "admin/hoadon-list");
                model.addAttribute("list", pageResult.getContent());
                model.addAttribute("page", pageResult);
                model.addAttribute("paginationItems", PaginationWindow.build(pageResult, 2));
                model.addAttribute("searchKeyword", q != null ? q : "");
                model.addAttribute("filterTrangThai", trangThaiFilter != null ? trangThaiFilter : "");
                model.addAttribute(chuyenCanXuLy ? "warning" : "message", msg);
                return "admin/hoadon-list :: content";
            }
            redirect.addFlashAttribute(chuyenCanXuLy ? "warning" : "message", msg);
        } catch (RuntimeException e) {
            String errorMsg = e.getMessage();
            if (veChiTiet) {
                if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
                    HoaDonDTO dto = hoaDonService.getById(id);
                    model.addAttribute("hoaDon", dto);
                    model.addAttribute("hoaDonStatusClass", statusToCssClass(dto.getTrangThaiDonHang()));
                    model.addAttribute("error", errorMsg);
                    return "admin/hoadon-detail :: content";
                }
                redirect.addFlashAttribute("error", errorMsg);
                return "redirect:/admin/hoa-don/" + id;
            }
            if ("XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
                Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "id"));
                Page<HoaDonDTO> pageResult = hoaDonService.searchPage(q, trangThaiFilter, pageable);
                model.addAttribute("title", "Hóa đơn");
                model.addAttribute("content", "admin/hoadon-list");
                model.addAttribute("list", pageResult.getContent());
                model.addAttribute("page", pageResult);
                model.addAttribute("paginationItems", PaginationWindow.build(pageResult, 2));
                model.addAttribute("searchKeyword", q != null ? q : "");
                model.addAttribute("filterTrangThai", trangThaiFilter != null ? trangThaiFilter : "");
                model.addAttribute("error", errorMsg);
                return "admin/hoadon-list :: content";
            }
            redirect.addFlashAttribute("error", errorMsg);
        }
        if (q != null && !q.isBlank()) redirect.addAttribute("q", q);
        if (trangThaiFilter != null && !trangThaiFilter.isBlank()) redirect.addAttribute("trangThai", trangThaiFilter);
        redirect.addAttribute("page", page);
        return "redirect:/admin/hoa-don";
    }

    @PostMapping("/{id}/xoa")
    public String delete(@PathVariable Integer id,
                        @RequestParam(required = false) String q,
                        @RequestParam(required = false) String trangThai,
                        @RequestParam(defaultValue = "0") int page,
                        RedirectAttributes redirect) {
        hoaDonService.delete(id);
        redirect.addFlashAttribute("message", "Xóa hóa đơn thành công.");
        if (q != null && !q.isBlank()) redirect.addAttribute("q", q);
        if (trangThai != null && !trangThai.isBlank()) redirect.addAttribute("trangThai", trangThai);
        redirect.addAttribute("page", page);
        return "redirect:/admin/hoa-don";
    }
}
