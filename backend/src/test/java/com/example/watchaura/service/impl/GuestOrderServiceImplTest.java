package com.example.watchaura.service.impl;

import com.example.watchaura.dto.GuestOrderPlaceResponse;
import com.example.watchaura.dto.GuestOrderPreviewResponse;
import com.example.watchaura.dto.GuestOrderRequest;
import com.example.watchaura.dto.KhuyenMaiPriceResult;
import com.example.watchaura.dto.ShippingFeeResponse;
import com.example.watchaura.entity.HoaDon;
import com.example.watchaura.entity.SanPham;
import com.example.watchaura.entity.SanPhamChiTiet;
import com.example.watchaura.repository.HoaDonChiTietRepository;
import com.example.watchaura.repository.HoaDonRepository;
import com.example.watchaura.repository.SanPhamChiTietRepository;
import com.example.watchaura.service.EmailService;
import com.example.watchaura.service.KhuyenMaiService;
import com.example.watchaura.service.SanPhamChiTietKhuyenMaiService;
import com.example.watchaura.service.ShippingService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings("null")
class GuestOrderServiceImplTest {
    @Mock
    private HoaDonRepository hoaDonRepository;
    @Mock
    private HoaDonChiTietRepository hoaDonChiTietRepository;
    @Mock
    private SanPhamChiTietRepository sanPhamChiTietRepository;
    @Mock
    private SanPhamChiTietKhuyenMaiService sanPhamChiTietKhuyenMaiService;
    @Mock
    private KhuyenMaiService khuyenMaiService;
    @Mock
    private ShippingService shippingService;
    @Mock
    private EmailService emailService;
    @Mock
    private HttpSession session;

    @InjectMocks
    private GuestOrderServiceImpl service;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(service, "trackingSecret", "unit-test-secret");
    }

    @Test
    void previewOrder_returnsShippingFallbackData() {
        GuestOrderRequest request = sampleRequest();
        SanPhamChiTiet spct = sampleProduct(1, 10, new BigDecimal("120000"));
        when(session.getAttribute("cart")).thenReturn(Map.of(1, 2));
        when(khuyenMaiService.getActivePromotions(any(LocalDateTime.class))).thenReturn(List.of());
        when(sanPhamChiTietRepository.findById(1)).thenReturn(Optional.of(spct));
        when(sanPhamChiTietRepository.findByIdWithDetails(1)).thenReturn(Optional.of(spct));
        when(sanPhamChiTietKhuyenMaiService.resolveBestForCartOrOrderLine(eq(spct), any(LocalDateTime.class), any()))
                .thenReturn(KhuyenMaiPriceResult.none(new BigDecimal("120000")));
        when(shippingService.calculateGuestFee(any())).thenReturn(new ShippingFeeResponse(
                35000L, "VND", "GHN", true, "provider_connection_error", "fallback"));

        GuestOrderPreviewResponse response = service.previewOrder(request, session);

        assertEquals(new BigDecimal("240000"), response.getSubtotal());
        assertEquals(new BigDecimal("35000"), response.getShippingFee());
        assertEquals(new BigDecimal("275000"), response.getTotal());
        assertEquals(true, response.isFallbackApplied());
    }

    @Test
    void placeOrder_success_persistsOrderAndClearsSessionCart() {
        GuestOrderRequest request = sampleRequest();
        SanPhamChiTiet spct = sampleProduct(2, 5, new BigDecimal("200000"));
        when(session.getAttribute("cart")).thenReturn(Map.of(2, 1));
        when(khuyenMaiService.getActivePromotions(any(LocalDateTime.class))).thenReturn(List.of());
        when(sanPhamChiTietRepository.findByIdWithLock(2)).thenReturn(Optional.of(spct));
        when(sanPhamChiTietRepository.findByIdWithDetails(2)).thenReturn(Optional.of(spct));
        when(sanPhamChiTietKhuyenMaiService.resolveBestForCartOrOrderLine(eq(spct), any(LocalDateTime.class), any()))
                .thenReturn(KhuyenMaiPriceResult.none(new BigDecimal("200000")));
        when(shippingService.calculateGuestFee(any())).thenReturn(new ShippingFeeResponse(
                30000L, "VND", "GHN", false, null, "success"));
        when(hoaDonRepository.save(any(HoaDon.class))).thenAnswer(inv -> {
            HoaDon hd = inv.getArgument(0);
            hd.setId(999);
            return hd;
        });

        GuestOrderPlaceResponse response = service.placeOrder(request, session);

        assertEquals(999, response.getOrderId());
        assertNotNull(response.getTrackingToken());
        verify(session).removeAttribute("cart");
        verify(hoaDonChiTietRepository).save(any());
    }

    @Test
    void placeOrder_throwsWhenOutOfStock() {
        GuestOrderRequest request = sampleRequest();
        SanPhamChiTiet spct = sampleProduct(3, 0, new BigDecimal("500000"));
        when(session.getAttribute("cart")).thenReturn(Map.of(3, 1));
        when(khuyenMaiService.getActivePromotions(any(LocalDateTime.class))).thenReturn(List.of());
        when(sanPhamChiTietRepository.findByIdWithLock(3)).thenReturn(Optional.of(spct));

        assertThrows(IllegalArgumentException.class, () -> service.placeOrder(request, session));
        verify(hoaDonRepository, never()).save(any(HoaDon.class));
    }

    @Test
    void trackOrder_throwsWhenTokenInvalid() {
        HoaDon hoaDon = new HoaDon();
        hoaDon.setMaDonHang("WA123");
        hoaDon.setEmail("guest@example.com");
        hoaDon.setSdtKhachHang("0987654321");
        hoaDon.setTenKhachHang("Guest");
        hoaDon.setTongTienThanhToan(new BigDecimal("500000"));
        when(hoaDonRepository.findByMaDonHang("WA123")).thenReturn(Optional.of(hoaDon));

        assertThrows(IllegalArgumentException.class, () ->
                service.trackOrder("WA123", "guest@example.com", "0987654321", "wrong-token"));
    }

    private static GuestOrderRequest sampleRequest() {
        GuestOrderRequest request = new GuestOrderRequest();
        request.setHoTen("Khach Le");
        request.setEmail("guest@example.com");
        request.setSdt("0987654321");
        request.setDiaChiCuThe("12 Nguyen Trai");
        request.setPhuongXa("Phuong 1");
        request.setQuanHuyen("Quan 3");
        request.setTinhThanh("TP HCM");
        request.setGhnDistrictId(1442);
        request.setGhnWardCode("21211");
        return request;
    }

    private static SanPhamChiTiet sampleProduct(Integer id, Integer stock, BigDecimal price) {
        SanPham sp = new SanPham();
        sp.setTenSanPham("Watch " + id);
        SanPhamChiTiet spct = new SanPhamChiTiet();
        spct.setId(id);
        spct.setSanPham(sp);
        spct.setSoLuongTon(stock);
        spct.setGiaBan(price);
        return spct;
    }
}
