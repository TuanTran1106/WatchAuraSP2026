package com.example.watchaura.controller;

import com.example.watchaura.dto.HoaDonDTO;
import com.example.watchaura.dto.HoaDonRequest;
import com.example.watchaura.entity.Voucher;
import com.example.watchaura.service.HoaDonService;
import com.example.watchaura.service.KhachHangService;
import com.example.watchaura.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/admin/hoa-don")
@RequiredArgsConstructor
public class HoaDonController {

    private final HoaDonService hoaDonService;
    private final KhachHangService khachHangService;
    private final VoucherService voucherService;

    private static final int PAGE_SIZE = 6;

    @GetMapping
    public String list(@RequestParam(required = false) String q,
                      @RequestParam(required = false) String trangThai,
                      @RequestParam(defaultValue = "0") int page,
                      Model model) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<HoaDonDTO> pageResult = hoaDonService.searchPage(q, trangThai, pageable);
        model.addAttribute("title", "Hóa đơn");
        model.addAttribute("content", "admin/hoadon-list");
        model.addAttribute("list", pageResult.getContent());
        model.addAttribute("page", pageResult);
        model.addAttribute("searchKeyword", q != null ? q : "");
        model.addAttribute("filterTrangThai", trangThai != null ? trangThai : "");
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

    @PostMapping("/{id}/trang-thai")
    public String updateTrangThai(@PathVariable Integer id, @RequestParam String trangThai,
                                  @RequestParam(required = false) String q,
                                  @RequestParam(required = false) String trangThaiFilter,
                                  @RequestParam(defaultValue = "0") int page,
                                  RedirectAttributes redirect) {
        try {
            hoaDonService.updateTrangThaiDonHang(id, trangThai);
            redirect.addFlashAttribute("message", "Đã cập nhật trạng thái đơn hàng.");
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("error", e.getMessage());
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
