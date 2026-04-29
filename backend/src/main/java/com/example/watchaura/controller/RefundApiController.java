package com.example.watchaura.controller;

import com.example.watchaura.dto.PhieuNhapKhoDTO;
import com.example.watchaura.dto.RefundRequest;
import com.example.watchaura.dto.SerialCheckResponse;
import com.example.watchaura.service.PhieuNhapKhoService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/refund")
@RequiredArgsConstructor
public class RefundApiController {

    private final PhieuNhapKhoService phieuNhapKhoService;

    @GetMapping("/serial/check")
    public ResponseEntity<?> checkSerial(@RequestParam String maSerial) {
        try {
            SerialCheckResponse result = phieuNhapKhoService.checkSerial(maSerial);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/hoan-tien")
    public ResponseEntity<?> xacNhanHoanTien(
            @Valid @RequestBody RefundRequest request,
            HttpSession session) {
        try {
            Integer nhanVienId = (Integer) session.getAttribute("nhanVienId");
            if (nhanVienId == null) {
                nhanVienId = 1;
            }

            PhieuNhapKhoDTO result = phieuNhapKhoService.createFromHoanTra(request, nhanVienId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Xác nhận hoàn tiền thành công!");
            response.put("data", result);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/hoan-tien-truc-tiep")
    public ResponseEntity<?> xacNhanHoanTienTrucTiep(
            @RequestBody Map<String, Object> request,
            HttpSession session) {
        try {
            Integer nhanVienId = (Integer) session.getAttribute("nhanVienId");
            if (nhanVienId == null) {
                nhanVienId = 1;
            }

            String maSerial = (String) request.get("maSerial");
            BigDecimal soTienHoan = new BigDecimal(request.get("soTienHoan").toString());
            String phuongThucHoanTien = (String) request.get("phuongThucHoanTien");
            String ghiChu = (String) request.getOrDefault("ghiChu", "");

            SerialCheckResponse serialCheck = phieuNhapKhoService.checkSerial(maSerial);
            if (!serialCheck.isValid()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", serialCheck.getErrorMessage());
                return ResponseEntity.badRequest().body(error);
            }

            RefundRequest refundRequest = RefundRequest.builder()
                    .hoanTraId(serialCheck.getHoanTraId())
                    .soTienHoan(soTienHoan)
                    .phuongThucHoanTien(phuongThucHoanTien)
                    .ghiChu(ghiChu)
                    .build();

            PhieuNhapKhoDTO result = phieuNhapKhoService.createFromHoanTra(refundRequest, nhanVienId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Xác nhận hoàn tiền thành công!");
            response.put("data", result);
            response.put("serialInfo", serialCheck);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/hoan-tra/{hoanTraId}/phi-nhap-kho")
    public ResponseEntity<?> getPhieuNhapKho(@PathVariable Integer hoanTraId) {
        try {
            PhieuNhapKhoDTO result = phieuNhapKhoService.getByHoanTraId(hoanTraId);
            if (result == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Chưa có phiếu nhập kho cho hoàn trả này");
                return ResponseEntity.ok(error);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/hoan-tra/{hoanTraId}/tinh-tien")
    public ResponseEntity<?> tinhTienHoan(@PathVariable Integer hoanTraId) {
        try {
            BigDecimal soTien = phieuNhapKhoService.calculateRefundAmount(hoanTraId);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("soTienHoan", soTien);
            result.put("soTienHoanFormatted", formatCurrency(soTien));
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    private String formatCurrency(BigDecimal amount) {
        if (amount == null) return "0đ";
        return amount.toPlainString() + "đ";
    }
}
