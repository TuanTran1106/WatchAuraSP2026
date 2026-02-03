package com.example.watchaura.controller;

import com.example.watchaura.dto.HoaDonDTO;
import com.example.watchaura.dto.HoaDonRequest;
import com.example.watchaura.service.HoaDonService;
import com.example.watchaura.service.KhachHangService;
import com.example.watchaura.service.VoucherService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/admin/hoa-don")
@RequiredArgsConstructor
public class HoaDonController {

    private final HoaDonService hoaDonService;
    private final KhachHangService khachHangService;
    private final VoucherService voucherService;

    @GetMapping
    public String list(Model model) {
        List<HoaDonDTO> list = hoaDonService.getAll();
        model.addAttribute("title", "Hóa đơn");
        model.addAttribute("content", "admin/hoadon-list");
        model.addAttribute("list", list);
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

    @GetMapping("/them")
    public String formCreate(Model model) {
        model.addAttribute("title", "Thêm hóa đơn");
        model.addAttribute("content", "admin/hoadon-form");
        HoaDonRequest request = new HoaDonRequest();
        request.setItems(Collections.emptyList());
        model.addAttribute("hoaDon", request);
        model.addAttribute("khachHangList", khachHangService.getAll());
        model.addAttribute("voucherList", voucherService.getAll());
        model.addAttribute("formAction", "/admin/hoa-don");
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
        model.addAttribute("voucherList", voucherService.getAll());
        model.addAttribute("formAction", "/admin/hoa-don/" + id);
        return "layout/admin-layout";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("hoaDon") HoaDonRequest request, RedirectAttributes redirect) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            request.setItems(Collections.emptyList());
        }
        hoaDonService.create(request);
        redirect.addFlashAttribute("message", "Thêm hóa đơn thành công.");
        return "redirect:/admin/hoa-don";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id, @Valid @ModelAttribute("hoaDon") HoaDonRequest request, RedirectAttributes redirect) {
        hoaDonService.update(id, request);
        redirect.addFlashAttribute("message", "Cập nhật hóa đơn thành công.");
        return "redirect:/admin/hoa-don";
    }

    @PostMapping("/{id}/xoa")
    public String delete(@PathVariable Integer id, RedirectAttributes redirect) {
        hoaDonService.delete(id);
        redirect.addFlashAttribute("message", "Xóa hóa đơn thành công.");
        return "redirect:/admin/hoa-don";
    }
}
