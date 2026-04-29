package com.example.watchaura.controller;

import com.example.watchaura.dto.SerialCheckResponse;
import com.example.watchaura.entity.HoanTra;
import com.example.watchaura.repository.HoaDonRepository;
import com.example.watchaura.repository.HoanTraRepository;
import com.example.watchaura.service.PhieuNhapKhoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin/tra-hang")
@RequiredArgsConstructor
public class TraHangController {

    private final PhieuNhapKhoService phieuNhapKhoService;
    private final HoanTraRepository hoanTraRepository;
    private final HoaDonRepository hoaDonRepository;

    @GetMapping
    public String traHangPage(Model model) {
        model.addAttribute("title", "Trả hàng (Refund)");
        model.addAttribute("content", "admin/tra-hang");
        return "layout/admin-layout";
    }

    @PostMapping("/tao-hoan-tra-tu-serial")
    public String taoHoanTraTuSerial(
            @RequestParam String maSerial,
            @RequestParam(required = false) String lyDo,
            RedirectAttributes redirectAttributes) {
        
        try {
            SerialCheckResponse check = phieuNhapKhoService.checkSerial(maSerial.trim());
            
            if (!check.isValid()) {
                redirectAttributes.addFlashAttribute("errorMessage", check.getErrorMessage());
                return "redirect:/admin/tra-hang";
            }

            if (check.getHoanTraId() == null) {
                redirectAttributes.addFlashAttribute("errorMessage", 
                    "Serial này chưa có yêu cầu hoàn trả. Vui lòng tạo yêu cầu hoàn trả trước.");
                return "redirect:/admin/tra-hang";
            }

            redirectAttributes.addFlashAttribute("successMessage", 
                "Đã tìm thấy hoàn trả " + check.getMaHoanTra() + " cho serial " + maSerial);
            redirectAttributes.addFlashAttribute("hoanTraId", check.getHoanTraId());
            
            return "redirect:/admin/tra-hang/" + check.getHoanTraId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi: " + e.getMessage());
            return "redirect:/admin/tra-hang";
        }
    }

    @GetMapping("/{hoanTraId}")
    public String chiTietHoanTra(@PathVariable Integer hoanTraId, Model model) {
        model.addAttribute("hoanTraId", hoanTraId);
        model.addAttribute("title", "Xác nhận hoàn tiền");
        model.addAttribute("content", "admin/tra-hang-chi-tiet");
        return "layout/admin-layout";
    }

    @GetMapping("/api/tinh-tien/{hoanTraId}")
    @ResponseBody
    public Map<String, Object> tinhTienHoan(@PathVariable Integer hoanTraId) {
        Map<String, Object> result = new HashMap<>();
        try {
            BigDecimal soTien = phieuNhapKhoService.calculateRefundAmount(hoanTraId);
            result.put("success", true);
            result.put("soTienHoan", soTien);
            result.put("soTienHoanFormatted", formatCurrency(soTien));
            return result;
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
            return result;
        }
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0đ";
        return amount.toPlainString() + "đ";
    }
}
