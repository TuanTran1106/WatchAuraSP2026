package com.example.watchaura.controller;

import com.example.watchaura.dto.ShippingFeeRequest;
import com.example.watchaura.dto.ShippingFeeResponse;
import com.example.watchaura.dto.ShippingLocationOption;
import com.example.watchaura.service.ShippingService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shipping")
@RequiredArgsConstructor
public class ShippingController {

    private final ShippingService shippingService;

    @GetMapping("/provinces")
    public ResponseEntity<?> getProvinces() {
        try {
            List<ShippingLocationOption> provinces = shippingService.getProvinces();
            return ResponseEntity.ok(Map.of("success", true, "items", provinces));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                    "success", false,
                    "message", "Không tải được danh sách tỉnh/thành từ GHN."
            ));
        }
    }

    @GetMapping("/districts")
    public ResponseEntity<?> getDistricts(@RequestParam("provinceId") Integer provinceId) {
        try {
            List<ShippingLocationOption> districts = shippingService.getDistricts(provinceId);
            return ResponseEntity.ok(Map.of("success", true, "items", districts));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                    "success", false,
                    "message", "Không tải được danh sách quận/huyện từ GHN."
            ));
        }
    }

    @GetMapping("/wards")
    public ResponseEntity<?> getWards(@RequestParam("districtId") Integer districtId) {
        try {
            List<ShippingLocationOption> wards = shippingService.getWards(districtId);
            return ResponseEntity.ok(Map.of("success", true, "items", wards));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                    "success", false,
                    "message", "Không tải được danh sách phường/xã từ GHN."
            ));
        }
    }

    @PostMapping("/fee")
    public ResponseEntity<?> calculateFee(@Valid @RequestBody ShippingFeeRequest request, HttpSession session) {
        Integer userId = (Integer) session.getAttribute(AuthController.SESSION_CURRENT_USER_ID);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                    "message", "Vui lòng đăng nhập để tính phí vận chuyển."
            ));
        }

        try {
            ShippingFeeResponse response = shippingService.calculateFee(request, userId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of(
                    "message", ex.getMessage()
            ));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "Không thể tính phí vận chuyển. Vui lòng thử lại."
            ));
        }
    }
}
