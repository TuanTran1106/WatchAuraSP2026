package com.example.watchaura.service.impl;

import com.example.watchaura.config.VNPayProperties;
import com.example.watchaura.service.VNPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class VNPayServiceImpl implements VNPayService {

    private static final String VNP_VERSION = "2.1.0";
    private static final String VNP_COMMAND = "pay";
    private static final String VNP_CURR_CODE = "VND";
    private static final String HMAC_SHA512 = "HmacSHA512";

    private final VNPayProperties vnPayProperties;

    @Override
    public String createPaymentUrl(long amountVnd, String orderRef, String orderInfo, String returnUrl, String clientIp, String locale) {
        if (locale == null || locale.isBlank()) locale = "vn";

        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Version", VNP_VERSION);
        params.put("vnp_Command", VNP_COMMAND);
        params.put("vnp_TmnCode", vnPayProperties.getTmnCode());
        params.put("vnp_Amount", String.valueOf(amountVnd * 100)); // VN Pay yêu cầu số tiền * 100
        params.put("vnp_CurrCode", VNP_CURR_CODE);
        params.put("vnp_TxnRef", orderRef);
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_OrderType", "other");
        params.put("vnp_Locale", locale);
        params.put("vnp_ReturnUrl", returnUrl);
        params.put("vnp_IpAddr", clientIp != null ? clientIp : "127.0.0.1");
        params.put("vnp_CreateDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")));

        // Chuỗi để ký: key=value&... (giá trị raw)
        StringBuilder signData = new StringBuilder();
        StringBuilder queryEncoded = new StringBuilder();
        for (Map.Entry<String, String> e : params.entrySet()) {
            if (e.getValue() != null && !e.getValue().isEmpty()) {

                String encodedValue = urlEncode(e.getValue());

                if (signData.length() > 0) {
                    signData.append("&");
                    queryEncoded.append("&");
                }

                // VNPay yêu cầu encode trước khi ký
                signData.append(e.getKey()).append("=").append(encodedValue);

                queryEncoded.append(e.getKey()).append("=").append(encodedValue);
            }
        }
        String secureHash = hmacSha512(vnPayProperties.getHashSecret(), signData.toString());
        queryEncoded.append("&vnp_SecureHash=").append(secureHash);

        return vnPayProperties.getUrl() + "?" + queryEncoded;
    }

    @Override
    public boolean verifyReturn(HttpServletRequest request) {
        Map<String, String> params = getReturnParams(request);
        String vnpSecureHash = params.get("vnp_SecureHash");
        String vnpResponseCode = params.get("vnp_ResponseCode");
        if (vnpSecureHash == null || vnpSecureHash.isEmpty()) return false;

        StringBuilder signData = new StringBuilder();
        params.entrySet().stream()
                .filter(e -> !"vnp_SecureHash".equals(e.getKey()) && !"vnp_SecureHashType".equals(e.getKey()))
                .sorted(Map.Entry.comparingByKey())
                .forEach(e -> {
                    if (e.getValue() != null && !e.getValue().isEmpty()) {
                        if (signData.length() > 0) signData.append("&");
                        signData.append(e.getKey()).append("=").append(urlEncode(e.getValue()));
                    }
                });
        String computedHash = hmacSha512(vnPayProperties.getHashSecret(), signData.toString());
        boolean validHash = computedHash.equalsIgnoreCase(vnpSecureHash);
        boolean successCode = "00".equals(vnpResponseCode);
        if (!validHash) log.warn("VN Pay return: invalid SecureHash");
        return validHash && successCode;
    }

    @Override
    public Map<String, String> getReturnParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (key != null && values != null && values.length > 0 && values[0] != null) {
                params.put(key, values[0]);
            }
        });
        return params;
    }

    private static String hmacSha512(String key, String data) {
        try {
            Mac hmac = Mac.getInstance(HMAC_SHA512);
            SecretKeySpec spec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA512);
            hmac.init(spec);
            byte[] hash = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException("VN Pay HMAC SHA512 error", e);
        }
    }

    private static String urlEncode(String value) {
        try {
            return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }
}
