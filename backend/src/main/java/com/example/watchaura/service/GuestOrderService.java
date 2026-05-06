package com.example.watchaura.service;

import com.example.watchaura.dto.GuestOrderPlaceResponse;
import com.example.watchaura.dto.GuestOrderPreviewResponse;
import com.example.watchaura.dto.GuestOrderRequest;
import com.example.watchaura.dto.GuestOrderTrackResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public interface GuestOrderService {
    GuestOrderPreviewResponse previewOrder(GuestOrderRequest request, HttpSession session);

    GuestOrderPlaceResponse placeOrder(GuestOrderRequest request, HttpSession session);

    GuestOrderPlaceResponse placeOrderForVnPay(GuestOrderRequest request, HttpSession session, HttpServletRequest httpRequest);

    GuestOrderTrackResponse trackOrder(String orderCode, String email, String phone, String token);

    boolean handleVnPayReturn(HttpServletRequest request, HttpSession session);
}
