package com.example.watchaura.util;

/**
 * Chuẩn hóa và kiểm tra SĐT di động Việt Nam (10 số, đầu 0 + đầu mạng 3/5/7/8/9).
 */
public final class PhoneUtils {

    private static final String VN_MOBILE_PATTERN = "^0[35789]\\d{8}$";

    private PhoneUtils() {
    }

    /**
     * Bỏ khoảng trắng/ký tự không phải số; hỗ trợ +84 / 84 đầu số.
     */
    public static String normalizeVnMobile(String raw) {
        if (raw == null) {
            return "";
        }
        String t = raw.trim().replaceAll("\\s+", "");
        if (t.isEmpty()) {
            return "";
        }
        if (t.startsWith("+84")) {
            t = "0" + t.substring(3).replaceAll("\\D", "");
        } else if (t.startsWith("84") && t.length() >= 10) {
            t = "0" + t.substring(2).replaceAll("\\D", "");
        } else {
            t = t.replaceAll("\\D", "");
        }
        if (t.length() == 9 && t.startsWith("9")) {
            t = "0" + t;
        }
        return t;
    }

    public static boolean isValidVnMobile(String normalizedDigits) {
        return normalizedDigits != null && normalizedDigits.matches(VN_MOBILE_PATTERN);
    }
}
