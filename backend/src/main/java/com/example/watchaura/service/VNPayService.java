package com.example.watchaura.service;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

public interface VNPayService {

    /**
     * Tạo URL thanh toán VN Pay để redirect khách hàng.
     *
     * @param amountVnd    Tổng tiền thanh toán (VND). VN Pay yêu cầu số tiền * 100.
     * @param orderRef     Mã tham chiếu đơn hàng (vd: maDonHang), không trùng trong ngày.
     * @param orderInfo    Nội dung đơn hàng (không dấu, không ký tự đặc biệt).
     * @param returnUrl    URL VN Pay redirect sau khi thanh toán.
     * @param clientIp    IP khách hàng.
     * @param locale      "vn" hoặc "en".
     * @return URL đầy đủ (có vnp_SecureHash) để redirect.
     */
    String createPaymentUrl(long amountVnd, String orderRef, String orderInfo, String returnUrl, String clientIp, String locale);

    /**
     * Kiểm tra chữ ký và mã trả về từ VN Pay (khi user được redirect về returnUrl).
     *
     * @param request Request chứa query params từ VN Pay (vnp_ResponseCode, vnp_TxnRef, vnp_SecureHash, ...).
     * @return true nếu chữ ký hợp lệ và giao dịch thành công (vnp_ResponseCode = "00").
     */
    boolean verifyReturn(HttpServletRequest request);

    /**
     * Lấy các tham số từ request (để lấy vnp_TxnRef, vnp_ResponseCode, vnp_Amount...).
     */
    Map<String, String> getReturnParams(HttpServletRequest request);
}
