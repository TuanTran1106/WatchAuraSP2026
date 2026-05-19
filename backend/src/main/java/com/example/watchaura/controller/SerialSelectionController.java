package com.example.watchaura.controller;

import com.example.watchaura.annotation.RequiresRole;
import com.example.watchaura.dto.AssignSerialRequest;
import com.example.watchaura.dto.HoaDonDTO;
import com.example.watchaura.dto.HoaDonSerialSelectionDTO;
import com.example.watchaura.repository.HoaDonRepository;
import com.example.watchaura.service.HoaDonService;
import com.example.watchaura.service.SerialSanPhamService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
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

    /**
     * API validate serial trước khi gán (AJAX)
     * Kiểm tra xem các serial được chọn có còn trong kho không
     */
    @PostMapping("/api/validate")
    @ResponseBody
    public ResponseEntity<?> validateSerials(@RequestBody Map<String, Object> request,
                                            HttpServletRequest httpRequest) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> serialsByBienTheRaw = (Map<String, Object>) request.get("serialsByBienThe");

            if (serialsByBienTheRaw == null || serialsByBienTheRaw.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "valid", true,
                        "soldSerials", java.util.Collections.emptyList()
                ));
            }

            // Lấy hoaDonId từ request để kiểm tra serial có thuộc đơn này không
            Integer hoaDonId = null;
            if (request.containsKey("hoaDonId")) {
                hoaDonId = (Integer) request.get("hoaDonId");
            }

            List<Map<String, Object>> soldSerials = new java.util.ArrayList<>();
            List<String> soldSerialCodes = new java.util.ArrayList<>();

            for (Map.Entry<String, Object> entry : serialsByBienTheRaw.entrySet()) {
                Integer hoaDonChiTietId = Integer.parseInt(entry.getKey());
                @SuppressWarnings("unchecked")
                List<Integer> serialIds = (List<Integer>) entry.getValue();

                if (serialIds == null || serialIds.isEmpty()) {
                    continue;
                }

                for (Integer serialId : serialIds) {
                    var serialOpt = serialSanPhamService.getSerialById(serialId);
                    if (serialOpt.isPresent()) {
                        var serial = serialOpt.get();
                        
                        // Kiểm tra serial có đang ở trạng thái có thể chọn không
                        // TRONG_KHO: có thể chọn
                        // CHO_XAC_NHAN: đã gán cho 1 đơn hàng - kiểm tra xem có phải đơn hiện tại không
                        boolean isAvailable = serial.getTrangThai() == com.example.watchaura.entity.SerialSanPham.TRANG_THAI_TRONG_KHO;
                        boolean isAlreadyAssignedToThisOrder = serial.getTrangThai() == com.example.watchaura.entity.SerialSanPham.TRANG_THAI_CHO_XAC_NHAN 
                            && serial.getHoaDonChiTiet() != null 
                            && serial.getHoaDonChiTiet().getHoaDon() != null
                            && serial.getHoaDonChiTiet().getHoaDon().getId().equals(hoaDonId);
                        
                        if (!isAvailable && !isAlreadyAssignedToThisOrder) {
                            // Serial đã bán hoặc gán cho đơn khác
                            Map<String, Object> soldSerial = new java.util.HashMap<>();
                            soldSerial.put("id", serial.getId());
                            soldSerial.put("maSerial", serial.getMaSerial());
                            soldSerial.put("trangThai", serial.getTrangThai());
                            soldSerials.add(soldSerial);
                            soldSerialCodes.add(serial.getMaSerial());
                        }
                    }
                }
            }

            if (soldSerials.isEmpty()) {
                return ResponseEntity.ok(Map.of(
                        "valid", true,
                        "soldSerials", soldSerials
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                        "valid", false,
                        "soldSerials", soldSerials,
                        "message", "Serial đã được bán hoặc gán cho đơn khác. Vui lòng chọn serial khác."
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "valid", false,
                    "message", e.getMessage()
            ));
        }
    }
}
