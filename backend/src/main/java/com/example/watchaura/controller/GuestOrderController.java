package com.example.watchaura.controller;

import com.example.watchaura.dto.GuestOrderPlaceResponse;
import com.example.watchaura.dto.GuestOrderPreviewResponse;
import com.example.watchaura.dto.GuestOrderRequest;
import com.example.watchaura.dto.GuestOrderTrackResponse;
import com.example.watchaura.service.GuestOrderService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/guest-checkout")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class GuestOrderController {
    private final GuestOrderService guestOrderService;

    @PostMapping("/preview")
    public ResponseEntity<?> preview(@Valid @RequestBody GuestOrderRequest request, HttpSession session) {
        try {
            log.info("[GuestOrder] Preview request received");
            GuestOrderPreviewResponse response = guestOrderService.previewOrder(request, session);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            log.error("[GuestOrder] Preview failed: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            log.error("[GuestOrder] Preview error: ", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Không thể preview đơn guest lúc này."));
        }
    }

    @PostMapping("/place-order")
    public ResponseEntity<?> placeOrder(@RequestBody GuestOrderRequest request, HttpSession session, HttpServletRequest httpRequest) {
        log.info("[GuestOrder] placeOrder request received, paymentMethod={}", request.getPhuongThucThanhToan());
        try {
            // Kiểm tra nếu chọn VnPay
            boolean isVnPay = request.getPhuongThucThanhToan() != null
                    && "VNPAY".equalsIgnoreCase(request.getPhuongThucThanhToan());
            if (isVnPay) {
                log.info("[GuestOrder] Processing VNPay order...");
                GuestOrderPlaceResponse response = guestOrderService.placeOrderForVnPay(request, session, httpRequest);
                log.info("[GuestOrder] VNPay response: {}", response);
                return ResponseEntity.ok(response);
            }
            GuestOrderPlaceResponse response = guestOrderService.placeOrder(request, session);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            log.error("[GuestOrder] Place order failed: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            log.error("[GuestOrder] Place order error: ", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Không thể đặt đơn guest lúc này: " + ex.getMessage()));
        }
    }

    @GetMapping("/test")
    public ResponseEntity<?> testEndpoint() {
        log.info("[GuestOrder] Test endpoint called!");
        return ResponseEntity.ok(Map.of("status", "ok", "message", "Test successful"));
    }

    @GetMapping("/test-vnpay")
    public ResponseEntity<?> testVnPayResponse() {
        log.info("[GuestOrder] Test VNPay response called!");
        GuestOrderPlaceResponse testResponse = GuestOrderPlaceResponse.builder()
                .orderId(1)
                .orderCode("WA1234567890")
                .redirectUrl("https://sandbox.vnpayment.vn/paymentv2/vpcpay.html?test=1")
                .needVnPayRedirect(true)
                .message("Test redirect")
                .build();
        return ResponseEntity.ok(testResponse);
    }

    @GetMapping("/vnpay/return")
    public void vnpayReturn(HttpServletRequest request, HttpSession session, HttpServletResponse response) throws IOException {
        try {
            boolean success = guestOrderService.handleVnPayReturn(request, session);
            String maDonHang = request.getParameter("vnp_TxnRef");
            String encodedOrderCode = maDonHang != null ? URLEncoder.encode(maDonHang, StandardCharsets.UTF_8) : "";
            if (success) {
                response.sendRedirect("/checkout/success?orderCode=" + encodedOrderCode + "&payment=VNPAY");
            } else {
                response.sendRedirect("/checkout/vnpay-failed?orderCode=" + encodedOrderCode);
            }
        } catch (Exception e) {
            response.sendRedirect("/checkout/vnpay-failed");
        }
    }

    @GetMapping("/track")
    public ResponseEntity<?> track(
            @RequestParam String orderCode,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam String token) {
        try {
            GuestOrderTrackResponse response = guestOrderService.trackOrder(orderCode, email, phone, token);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Không thể tra cứu đơn guest lúc này."));
        }
    }
}
