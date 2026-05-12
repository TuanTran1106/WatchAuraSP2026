package com.example.watchaura.controller;

import com.example.watchaura.annotation.RequiresRole;
import com.example.watchaura.dto.AssignSerialRequest;
import com.example.watchaura.dto.HoaDonDTO;
import com.example.watchaura.dto.HoaDonSerialSelectionDTO;
import com.example.watchaura.repository.HoaDonRepository;
import com.example.watchaura.service.HoaDonService;
import com.example.watchaura.service.SerialSanPhamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/admin/serial")
@RequiredArgsConstructor
@RequiresRole({"Admin", "Quản lý", "Nhân viên"})
public class SerialSelectionController {

    private final SerialSanPhamService serialSanPhamService;
    private final HoaDonService hoaDonService;
    private final HoaDonRepository hoaDonRepository;

    /**
     * Hiển thị trang chọn serial cho hóa đơn
     */
    @GetMapping("/chon/{hoaDonId}")
    public String showSerialSelectionPage(@PathVariable Integer hoaDonId,
                                          Model model,
                                          RedirectAttributes redirect) {
        HoaDonSerialSelectionDTO data = serialSanPhamService.getSerialSelectionData(hoaDonId);

        // Check tồn kho serial TRƯỚC KHI hiển thị trang
        for (HoaDonSerialSelectionDTO.BienTheSerialGroup group : data.getBienTheGroups()) {
            int soLuongKhaDung = group.getSerials() != null ? group.getSerials().size() : 0;
            if (soLuongKhaDung < group.getSoLuongMua()) {
                // Tự động chuyển đơn sang CAN_XU_LY
                hoaDonRepository.findById(hoaDonId).ifPresent(hd -> {
                    hd.setTrangThaiDonHang("CAN_XU_LY");
                    hd.setGhiChu("Không đủ serial trong kho: " + group.getTenSanPham() +
                        " " + group.getTenBienThe() + " - cần " + group.getSoLuongMua() +
                        ", chỉ còn " + soLuongKhaDung);
                    hoaDonRepository.save(hd);
                });

                redirect.addFlashAttribute("warning",
                    "Không đủ serial trong kho cho sản phẩm '" + group.getTenSanPham() +
                    " " + group.getTenBienThe() + "': cần " + group.getSoLuongMua() +
                    ", chỉ còn " + soLuongKhaDung + ". Đơn hàng đã được chuyển sang trạng thái Cần xử lý.");
                return "redirect:/admin/hoa-don/" + hoaDonId;
            }
        }

        model.addAttribute("title", "Chọn Serial - " + data.getMaDonHang());
        model.addAttribute("content", "admin/serial-selection");
        model.addAttribute("serialData", data);
        return "layout/admin-layout";
    }

    /**
     * Xử lý gán serial (redirect sau khi gán)
     */
    @PostMapping("/assign")
    public String assignSerials(@Valid @ModelAttribute AssignSerialRequest request,
                                RedirectAttributes redirect) {
        try {
            serialSanPhamService.assignSerialsToOrder(request.getHoaDonId(), request.getSerialsByBienThe());

            if (request.getTrangThaiMoi() != null && !request.getTrangThaiMoi().isBlank()) {
                hoaDonService.updateTrangThaiDonHang(request.getHoaDonId(), request.getTrangThaiMoi());
                redirect.addFlashAttribute("message", "Đã gán serial và cập nhật trạng thái thành công.");
            } else {
                redirect.addFlashAttribute("message", "Đã gán serial thành công.");
            }
        } catch (RuntimeException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/hoa-don/" + request.getHoaDonId();
    }

    /**
     * API gán serial (AJAX) - trả về JSON
     */
    @PostMapping("/api/assign")
    @ResponseBody
    public ResponseEntity<?> assignSerialsApi(@Valid @RequestBody AssignSerialRequest request) {
        try {
            serialSanPhamService.assignSerialsToOrder(request.getHoaDonId(), request.getSerialsByBienThe());

            if (request.getTrangThaiMoi() != null && !request.getTrangThaiMoi().isBlank()) {
                hoaDonService.updateTrangThaiDonHang(request.getHoaDonId(), request.getTrangThaiMoi());
                return ResponseEntity.ok(Map.of(
                        "success", true,
                        "message", "Đã gán serial và chuyển trạng thái thành công!"
                ));
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Đã gán serial thành công"
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));
        }
    }

    /**
     * API lấy dữ liệu serial cho hóa đơn
     */
    @GetMapping("/api/{hoaDonId}")
    @ResponseBody
    public ResponseEntity<HoaDonSerialSelectionDTO> getSerialData(@PathVariable Integer hoaDonId) {
        try {
            HoaDonSerialSelectionDTO data = serialSanPhamService.getSerialSelectionData(hoaDonId);
            return ResponseEntity.ok(data);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
