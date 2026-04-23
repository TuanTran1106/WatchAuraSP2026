package com.example.watchaura.service.impl;

import com.example.watchaura.dto.GuestOrderPlaceResponse;
import com.example.watchaura.dto.GuestOrderPreviewResponse;
import com.example.watchaura.dto.GuestOrderRequest;
import com.example.watchaura.dto.GuestOrderTrackResponse;
import com.example.watchaura.dto.KhuyenMaiPriceResult;
import com.example.watchaura.dto.ShippingFeeRequest;
import com.example.watchaura.dto.ShippingFeeResponse;
import com.example.watchaura.entity.HoaDon;
import com.example.watchaura.entity.HoaDonChiTiet;
import com.example.watchaura.entity.KhuyenMai;
import com.example.watchaura.entity.SanPhamChiTiet;
import com.example.watchaura.repository.HoaDonChiTietRepository;
import com.example.watchaura.repository.HoaDonRepository;
import com.example.watchaura.repository.SanPhamChiTietRepository;
import com.example.watchaura.service.EmailService;
import com.example.watchaura.service.GuestOrderService;
import com.example.watchaura.service.KhuyenMaiService;
import com.example.watchaura.service.SanPhamChiTietKhuyenMaiService;
import com.example.watchaura.service.ShippingService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GuestOrderServiceImpl implements GuestOrderService {
    private static final String HMAC_SHA256 = "HmacSHA256";

    private final HoaDonRepository hoaDonRepository;
    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final SanPhamChiTietKhuyenMaiService sanPhamChiTietKhuyenMaiService;
    private final KhuyenMaiService khuyenMaiService;
    private final ShippingService shippingService;
    private final EmailService emailService;

    @Value("${checkout.guest.tracking-secret:watchaura-guest-secret-change-me}")
    private String trackingSecret;

    @Override
    @Transactional(readOnly = true)
    public GuestOrderPreviewResponse previewOrder(GuestOrderRequest request, HttpSession session) {
        CartPricing pricing = buildCartPricing(session, false);
        ShippingFeeResponse shipping = shippingService.calculateGuestFee(toShippingRequest(request, pricing.subtotal));
        BigDecimal shippingFee = BigDecimal.valueOf(Math.max(0L, shipping.getShippingFee()));
        BigDecimal total = pricing.subtotal.add(shippingFee);

        return GuestOrderPreviewResponse.builder()
                .subtotal(pricing.subtotal)
                .shippingFee(shippingFee)
                .total(total)
                .currency(shipping.getCurrency())
                .provider(shipping.getProvider())
                .fallbackApplied(shipping.isFallbackApplied())
                .providerErrorCode(shipping.getProviderErrorCode())
                .message(shipping.getMessage())
                .totalItems(pricing.totalItems)
                .build();
    }

    @Override
    @Transactional
    public GuestOrderPlaceResponse placeOrder(GuestOrderRequest request, HttpSession session) {
        CartPricing pricing = buildCartPricing(session, true);
        ShippingFeeResponse shipping = shippingService.calculateGuestFee(toShippingRequest(request, pricing.subtotal));
        BigDecimal shippingFee = BigDecimal.valueOf(Math.max(0L, shipping.getShippingFee()));
        BigDecimal total = pricing.subtotal.add(shippingFee);

        HoaDon hoaDon = new HoaDon();
        hoaDon.setMaDonHang("WA" + System.currentTimeMillis());
        hoaDon.setTenKhachHang(clean(request.getHoTen()));
        hoaDon.setEmail(clean(request.getEmail()));
        hoaDon.setSdtKhachHang(clean(request.getSdt()));
        hoaDon.setDiaChi(buildAddress(request));
        hoaDon.setGhiChu(clean(request.getGhiChu()));
        hoaDon.setTongTienTamTinh(pricing.subtotal);
        hoaDon.setTienGiam(BigDecimal.ZERO);
        hoaDon.setTongTienThanhToan(total);
        hoaDon.setTrangThaiDonHang("CHO_XAC_NHAN");
        hoaDon.setPhuongThucThanhToan(resolvePaymentMethod(request.getPhuongThucThanhToan()));
        hoaDon.setLoaiHoaDon("ONLINE");
        hoaDon.setNgayDat(LocalDateTime.now());
        hoaDon.setTrangThai(true);
        HoaDon saved = hoaDonRepository.save(hoaDon);

        for (CartLine line : pricing.lines) {
            HoaDonChiTiet ct = new HoaDonChiTiet();
            ct.setHoaDon(saved);
            ct.setSanPhamChiTiet(line.spct);
            ct.setSoLuong(line.quantity);
            ct.setDonGia(line.unitPrice);
            hoaDonChiTietRepository.save(ct);
        }

        try {
            emailService.sendOrderConfirmation(saved);
        } catch (Exception ignored) {
        }
        session.removeAttribute("cart");

        String token = generateTrackingToken(saved.getMaDonHang(), saved.getEmail(), saved.getSdtKhachHang());
        return GuestOrderPlaceResponse.builder()
                .orderId(saved.getId())
                .orderCode(saved.getMaDonHang())
                .trackingToken(token)
                .message("Đặt hàng thành công.")
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GuestOrderTrackResponse trackOrder(String orderCode, String email, String phone, String token) {
        HoaDon order = hoaDonRepository.findByMaDonHang(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng."));
        if (order.getKhachHang() != null) {
            throw new IllegalArgumentException("Đơn hàng này không phải đơn guest.");
        }
        String normalizedEmail = clean(email);
        String normalizedPhone = clean(phone);
        if (!safeEqualsIgnoreCase(order.getEmail(), normalizedEmail) || !safeEquals(order.getSdtKhachHang(), normalizedPhone)) {
            throw new IllegalArgumentException("Thông tin tra cứu đơn hàng không chính xác.");
        }
        String expected = generateTrackingToken(order.getMaDonHang(), order.getEmail(), order.getSdtKhachHang());
        if (!safeEquals(expected, token)) {
            throw new IllegalArgumentException("Tracking token không hợp lệ.");
        }
        return GuestOrderTrackResponse.builder()
                .orderCode(order.getMaDonHang())
                .orderStatus(order.getTrangThaiDonHang())
                .customerName(order.getTenKhachHang())
                .phoneMasked(maskPhone(order.getSdtKhachHang()))
                .totalAmount(order.getTongTienThanhToan())
                .createdAt(order.getNgayDat())
                .build();
    }

    private CartPricing buildCartPricing(HttpSession session, boolean lockForPlaceOrder) {
        Map<Integer, Integer> cart = readCartFromSession(session);
        if (cart.isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng trống.");
        }

        LocalDateTime now = LocalDateTime.now();
        List<KhuyenMai> activePromotions = khuyenMaiService.getActivePromotions(now);
        List<CartLine> lines = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;
        int totalItems = 0;

        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            Integer spctId = entry.getKey();
            Integer quantity = entry.getValue();
            if (spctId == null || quantity == null || quantity <= 0) {
                continue;
            }
            SanPhamChiTiet locked = (lockForPlaceOrder
                    ? sanPhamChiTietRepository.findByIdWithLock(spctId)
                    : sanPhamChiTietRepository.findById(spctId))
                    .orElseThrow(() -> new IllegalArgumentException("Sản phẩm không tồn tại: " + spctId));

            int tonKho = locked.getSoLuongTon() != null ? locked.getSoLuongTon() : 0;
            if (tonKho < quantity) {
                String tenSp = locked.getSanPham() != null ? locked.getSanPham().getTenSanPham() : "ID " + spctId;
                throw new IllegalArgumentException("Sản phẩm " + tenSp + " không đủ tồn kho. Còn " + tonKho + " sản phẩm.");
            }

            SanPhamChiTiet forPrice = sanPhamChiTietRepository.findByIdWithDetails(spctId).orElse(locked);
            KhuyenMaiPriceResult pr = sanPhamChiTietKhuyenMaiService.resolveBestForCartOrOrderLine(forPrice, now, activePromotions);
            BigDecimal unitPrice = pr.giaSauGiam() != null ? pr.giaSauGiam() : BigDecimal.ZERO;
            subtotal = subtotal.add(unitPrice.multiply(BigDecimal.valueOf(quantity)));
            totalItems += quantity;
            lines.add(new CartLine(locked, quantity, unitPrice));
        }

        if (lines.isEmpty()) {
            throw new IllegalArgumentException("Giỏ hàng trống.");
        }
        return new CartPricing(lines, subtotal, totalItems);
    }

    private Map<Integer, Integer> readCartFromSession(HttpSession session) {
        Object raw = session.getAttribute("cart");
        if (!(raw instanceof Map<?, ?> rawMap) || rawMap.isEmpty()) {
            return Map.of();
        }
        Map<Integer, Integer> cart = new java.util.LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
            if (entry.getKey() instanceof Integer spctId && entry.getValue() instanceof Integer qty) {
                cart.put(spctId, qty);
            }
        }
        return cart;
    }

    private ShippingFeeRequest toShippingRequest(GuestOrderRequest request, BigDecimal subtotal) {
        ShippingFeeRequest shippingRequest = new ShippingFeeRequest();
        shippingRequest.setToDistrictId(request.getGhnDistrictId());
        shippingRequest.setToWardCode(clean(request.getGhnWardCode()));
        shippingRequest.setInsuranceValue(subtotal != null ? Math.max(0L, subtotal.longValue()) : 0L);
        return shippingRequest;
    }

    private String buildAddress(GuestOrderRequest request) {
        return String.join(", ",
                clean(request.getDiaChiCuThe()),
                clean(request.getPhuongXa()),
                clean(request.getQuanHuyen()),
                clean(request.getTinhThanh()));
    }

    private String resolvePaymentMethod(String paymentMethod) {
        String normalized = clean(paymentMethod);
        return normalized == null || normalized.isBlank() ? "COD" : normalized;
    }

    private String generateTrackingToken(String orderCode, String email, String phone) {
        String payload = orderCode + "|" + clean(email) + "|" + clean(phone);
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(trackingSecret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception ex) {
            throw new IllegalStateException("Không thể tạo tracking token.", ex);
        }
    }

    private static String maskPhone(String phone) {
        String x = clean(phone);
        if (x == null || x.length() < 4) {
            return "****";
        }
        return "*".repeat(Math.max(0, x.length() - 4)) + x.substring(x.length() - 4);
    }

    private static String clean(String value) {
        return value == null ? null : value.trim();
    }

    private static boolean safeEqualsIgnoreCase(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        return a.trim().equalsIgnoreCase(b.trim());
    }

    private static boolean safeEquals(String a, String b) {
        if (a == null || b == null) {
            return false;
        }
        return a.trim().equals(b.trim());
    }

    private record CartLine(SanPhamChiTiet spct, Integer quantity, BigDecimal unitPrice) {
    }

    private record CartPricing(List<CartLine> lines, BigDecimal subtotal, int totalItems) {
    }
}
