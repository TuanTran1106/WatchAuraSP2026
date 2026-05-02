package com.example.watchaura.controller;

import com.example.watchaura.dto.GuestOrderPlaceResponse;
import com.example.watchaura.dto.GuestOrderPreviewResponse;
import com.example.watchaura.dto.GuestOrderRequest;
import com.example.watchaura.dto.GuestOrderTrackResponse;
import com.example.watchaura.service.GuestOrderService;
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

import java.util.Map;

@RestController
@RequestMapping("/api/guest-checkout")
@RequiredArgsConstructor
public class GuestOrderController {
    private final GuestOrderService guestOrderService;

    @PostMapping("/preview")
    public ResponseEntity<?> preview(@Valid @RequestBody GuestOrderRequest request, HttpSession session) {
        try {
            GuestOrderPreviewResponse response = guestOrderService.previewOrder(request, session);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Không thể preview đơn guest lúc này."));
        }
    }

    @PostMapping("/place-order")
    public ResponseEntity<?> placeOrder(@Valid @RequestBody GuestOrderRequest request, HttpSession session) {
        try {
            GuestOrderPlaceResponse response = guestOrderService.placeOrder(request, session);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Không thể đặt đơn guest lúc này."));
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
