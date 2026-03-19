//package com.example.watchaura.service.impl;
//
//import com.example.watchaura.config.VNPayProperties;
//import com.example.watchaura.service.VNPayService;
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Service;
//
//import javax.crypto.Mac;
//import javax.crypto.spec.SecretKeySpec;
//import java.nio.charset.StandardCharsets;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class VNPayServiceImpl implements VNPayService {
//
//    private static final String VNP_VERSION = "2.1.0";
//    private static final String VNP_COMMAND = "pay";
//    private static final String VNP_CURR_CODE = "VND";
//    private static final String HMAC_SHA512 = "HmacSHA512";
//
//    private final VNPayProperties vnPayProperties;
//
//    @Override
//    public String createPaymentUrl(long amountVnd, String orderRef, String orderInfo, String returnUrl, String clientIp, String locale) {
//        if (locale == null || locale.isBlank()) locale = "vn";
//
//        Map<String, String> params = new TreeMap<>();
//        params.put("vnp_Version", VNP_VERSION);
//        params.put("vnp_Command", VNP_COMMAND);
//        params.put("vnp_TmnCode", vnPayProperties.getTmnCode());
//        params.put("vnp_Amount", String.valueOf(amountVnd * 100)); // VN Pay yêu cầu số tiền * 100
//        params.put("vnp_CurrCode", VNP_CURR_CODE);
//        params.put("vnp_TxnRef", orderRef);
//        params.put("vnp_OrderInfo", orderInfo);
//        params.put("vnp_OrderType", "other");
//        params.put("vnp_Locale", locale);
//        params.put("vnp_ReturnUrl", returnUrl);
//        params.put("vnp_IpAddr", clientIp != null ? clientIp : "127.0.0.1");
//        params.put("vnp_CreateDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));
//
//        // Chuỗi để ký: key=value&... (giá trị raw)
//        StringBuilder signData = new StringBuilder();
//        StringBuilder queryEncoded = new StringBuilder();
//        for (Map.Entry<String, String> e : params.entrySet()) {
//            if (e.getValue() != null && !e.getValue().isEmpty()) {
//                if (signData.length() > 0) {
//                    signData.append("&");
//                    queryEncoded.append("&");
//                }
//                signData.append(e.getKey()).append("=").append(e.getValue());
//                queryEncoded.append(e.getKey()).append("=").append(urlEncode(e.getValue()));
//            }
//        }
//
//        log.info("=== VN Pay Create Payment URL ===");
//        log.info("TMN Code: {}", vnPayProperties.getTmnCode());
//        log.info("Order Ref: {}", orderRef);
//        log.info("Amount (VND): {}", amountVnd);
//        log.info("Sign Data: {}", signData.toString());
//
//        String secureHash = hmacSha512(vnPayProperties.getHashSecret(), signData.toString());
//        log.info("Computed SecureHash: {}", secureHash);
//
//        queryEncoded.append("&vnp_SecureHash=").append(secureHash);
//
//        String paymentUrl = vnPayProperties.getUrl() + "?" + queryEncoded;
//        log.info("Payment URL: {}", paymentUrl);
//        return paymentUrl;
//    }
//
//    @Override
//    public boolean verifyReturn(HttpServletRequest request) {
//        Map<String, String> params = getReturnParams(request);
//        String vnpSecureHash = params.get("vnp_SecureHash");
//        String vnpResponseCode = params.get("vnp_ResponseCode");
//        if (vnpSecureHash == null || vnpSecureHash.isEmpty()) {
//            log.warn("VN Pay return: Missing vnp_SecureHash");
//            return false;
//        }
//
//        StringBuilder signData = new StringBuilder();
//        params.entrySet().stream()
//                .filter(e -> !"vnp_SecureHash".equals(e.getKey()) && !"vnp_SecureHashType".equals(e.getKey()))
//                .sorted(Map.Entry.comparingByKey())
//                .forEach(e -> {
//                    if (e.getValue() != null && !e.getValue().isEmpty()) {
//                        if (signData.length() > 0) signData.append("&");
//                        signData.append(e.getKey()).append("=").append(e.getValue());
//                    }
//                });
//
//        String computedHash = hmacSha512(vnPayProperties.getHashSecret(), signData.toString());
//        boolean validHash = computedHash.equalsIgnoreCase(vnpSecureHash);
//        boolean successCode = "00".equals(vnpResponseCode);
//
//        // Debug logging
//        log.info("=== VN Pay Return Verification ===");
//        log.info("TMN Code: {}", vnPayProperties.getTmnCode());
//        log.info("Response Code: {}", vnpResponseCode);
//        log.info("Sign Data (raw): {}", signData.toString());
//        log.info("Received SecureHash: {}", vnpSecureHash);
//        log.info("Computed SecureHash: {}", computedHash);
//        log.info("Hash Match: {}", validHash);
//        log.info("Success Code: {}", successCode);
//        log.info("All params: {}", params);
//
//        if (!validHash) {
//            log.warn("VN Pay return: Invalid SecureHash - Signature mismatch!");
//        }
//
//        return validHash && successCode;
//    }
//
//    @Override
//    public Map<String, String> getReturnParams(HttpServletRequest request) {
//        Map<String, String> params = new HashMap<>();
//        request.getParameterMap().forEach((key, values) -> {
//            if (key != null && values != null && values.length > 0 && values[0] != null) {
//                params.put(key, values[0]);
//            }
//        });
//        return params;
//    }
//
//    private static String hmacSha512(String key, String data) {
//        try {
//            Mac hmac = Mac.getInstance(HMAC_SHA512);
//            SecretKeySpec spec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA512);
//            hmac.init(spec);
//            byte[] hash = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
//            StringBuilder hex = new StringBuilder();
//            for (byte b : hash) {
//                hex.append(String.format("%02x", b));
//            }
//            return hex.toString();
//        } catch (Exception e) {
//            throw new RuntimeException("VN Pay HMAC SHA512 error", e);
//        }
//    }
//
//    private static String urlEncode(String value) {
//        try {
//            return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
//        } catch (Exception e) {
//            return value;
//        }
//    }
//}
package com.example.watchaura.service.impl;

import com.example.watchaura.config.VNPayProperties;
import com.example.watchaura.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class VNPayServiceImpl implements VNPayService {

    private final VNPayProperties vnPayProperties;

    // 1. TẠO URL THANH TOÁN
    // Được gọi: vnPayService.createPaymentUrl(amountVnd, maDonHang, orderInfo, returnUrl, clientIp, "vn")

    @Override
    public String createPaymentUrl(long amount, String maDonHang, String orderInfo,
                                   String returnUrl, String ipAddr, String locale) {

        log.info("[VNPay] Tạo URL thanh toán | Đơn: {} | Tiền: {}", maDonHang, amount);

        String vnp_CreateDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        cal.add(Calendar.MINUTE, 15);
        String vnp_ExpireDate = new SimpleDateFormat("yyyyMMddHHmmss").format(cal.getTime());

        // VNPay yêu cầu số tiền × 100
        long vnpAmount = amount * 100;

        // TreeMap tự sort theo alphabet — đúng yêu cầu VNPay
        Map<String, String> vnp_Params = new TreeMap<>();
        vnp_Params.put("vnp_Version",    "2.1.0");
        vnp_Params.put("vnp_Command",    "pay");
        vnp_Params.put("vnp_TmnCode",    vnPayProperties.getTmnCode());
        vnp_Params.put("vnp_Amount",     String.valueOf(vnpAmount));
        vnp_Params.put("vnp_CurrCode",   "VND");
        vnp_Params.put("vnp_TxnRef",     maDonHang);
        vnp_Params.put("vnp_OrderInfo",  orderInfo);
        vnp_Params.put("vnp_OrderType",  "other");
        vnp_Params.put("vnp_Locale",     locale != null ? locale : "vn");
        vnp_Params.put("vnp_ReturnUrl",  returnUrl);
        vnp_Params.put("vnp_IpAddr",     ipAddr);
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);
        vnp_Params.put("vnp_ExpireDate", vnp_ExpireDate);

        StringBuilder hashData   = new StringBuilder();
        StringBuilder queryString = new StringBuilder();

        for (Map.Entry<String, String> entry : vnp_Params.entrySet()) {
            String key   = entry.getKey();
            String value = entry.getValue();
            if (value != null && !value.isEmpty()) {
                hashData.append(key).append('=')
                        .append(URLEncoder.encode(value, StandardCharsets.US_ASCII))
                        .append('&');
                queryString.append(URLEncoder.encode(key, StandardCharsets.US_ASCII))
                        .append('=')
                        .append(URLEncoder.encode(value, StandardCharsets.US_ASCII))
                        .append('&');
            }
        }

        // Xóa '&' cuối
        if (hashData.length() > 0)    hashData.deleteCharAt(hashData.length() - 1);
        if (queryString.length() > 0) queryString.deleteCharAt(queryString.length() - 1);

        String secureHash = hmacSHA512(vnPayProperties.getHashSecret(), hashData.toString());
        queryString.append("&vnp_SecureHash=").append(secureHash);

        String paymentUrl = vnPayProperties.getPayUrl() + "?" + queryString;
        log.info("[VNPay] URL tạo thành công cho đơn: {}", maDonHang);
        return paymentUrl;
    }

    // 2. LẤY PARAMS TỪ REQUEST (Return URL)
    // Được gọi: vnPayService.getReturnParams(request)

    @Override
    public Map<String, String> getReturnParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values != null && values.length > 0) {
                params.put(key, values[0]);
            }
        });
        return params;
    }

    // 3. XÁC THỰC CHỮ KÝ (Return URL)
    // Được gọi: vnPayService.verifyReturn(request)

    @Override
    public boolean verifyReturn(HttpServletRequest request) {
        Map<String, String> params = getReturnParams(request);
        String vnp_SecureHash = params.get("vnp_SecureHash");

        if (vnp_SecureHash == null || vnp_SecureHash.isBlank()) {
            log.warn("[VNPay] Không có vnp_SecureHash trong response");
            return false;
        }

        // Loại bỏ các field không tham gia ký
        Map<String, String> signParams = new TreeMap<>(params);
        signParams.remove("vnp_SecureHash");
        signParams.remove("vnp_SecureHashType");

        StringBuilder hashData = new StringBuilder();
        for (Map.Entry<String, String> entry : signParams.entrySet()) {
            String value = entry.getValue();
            if (value != null && !value.isEmpty()) {
                hashData.append(entry.getKey()).append('=')
                        .append(URLEncoder.encode(value, StandardCharsets.US_ASCII))
                        .append('&');
            }
        }
        if (hashData.length() > 0) hashData.deleteCharAt(hashData.length() - 1);

        String computedHash = hmacSHA512(vnPayProperties.getHashSecret(), hashData.toString());
        boolean valid = computedHash.equalsIgnoreCase(vnp_SecureHash);

        if (!valid) {
            log.warn("[VNPay] Chữ ký không hợp lệ! Computed: {} | Received: {}",
                    computedHash, vnp_SecureHash);
        }
        return valid;
    }

    // HELPER — HMAC-SHA512


    private String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo HMAC-SHA512", e);
        }
    }
}