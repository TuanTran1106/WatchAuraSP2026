package com.example.watchaura.service.impl;

import com.example.watchaura.dto.KhuyenMaiPriceResult;
import com.example.watchaura.dto.TrangChuSanPhamGia;
import com.example.watchaura.entity.KhuyenMai;
import com.example.watchaura.entity.SanPham;
import com.example.watchaura.entity.SanPhamChiTiet;
import com.example.watchaura.entity.SanPhamChiTietKhuyenMai;
import com.example.watchaura.repository.SanPhamChiTietKhuyenMaiRepository;
import com.example.watchaura.repository.SanPhamChiTietRepository;
import com.example.watchaura.service.KhuyenMaiService;
import com.example.watchaura.service.SanPhamChiTietKhuyenMaiService;
import com.example.watchaura.util.KhuyenMaiPricePick;
import com.example.watchaura.util.KhuyenMaiPricing;
import com.example.watchaura.util.SanPhamKhuyenMaiPricing;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SanPhamChiTietKhuyenMaiServiceImpl implements SanPhamChiTietKhuyenMaiService {

    private final SanPhamChiTietKhuyenMaiRepository sanPhamChiTietKhuyenMaiRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final KhuyenMaiService khuyenMaiService;

    @Override
    @Transactional(readOnly = true)
    public TrangChuSanPhamGia resolveLowestPriceForSanPham(Integer sanPhamId) {
        if (sanPhamId == null) {
            return TrangChuSanPhamGia.bangGiaNiemyet(BigDecimal.ZERO);
        }
        List<SanPhamChiTiet> variants = sanPhamChiTietRepository.findBySanPhamId(sanPhamId);
        if (variants == null || variants.isEmpty()) {
            return TrangChuSanPhamGia.bangGiaNiemyet(BigDecimal.ZERO);
        }

        LocalDateTime now = LocalDateTime.now();
        List<KhuyenMai> chuongTrinhDangChay = khuyenMaiService.getActivePromotions(now);

        BigDecimal bestEff = null;
        BigDecimal bestListForDisplay = null;
        boolean bestHasKm = false;

        BigDecimal promoOnlyBestEff = null;
        BigDecimal promoOnlyBestList = null;

        for (SanPhamChiTiet v : variants) {
            if (v.getGiaBan() == null) {
                continue;
            }
            KhuyenMaiPriceResult r = resolveBestForCartOrOrderLine(v, now, chuongTrinhDangChay);
            BigDecimal eff = r.giaSauGiam();
            int cmp = bestEff == null ? -1 : eff.compareTo(bestEff);
            if (cmp < 0) {
                bestEff = eff;
                bestListForDisplay = v.getGiaBan();
                bestHasKm = r.coKhuyenMai();
            } else if (cmp == 0 && r.coKhuyenMai() && !bestHasKm) {
                bestListForDisplay = v.getGiaBan();
                bestHasKm = true;
            }

            if (r.coKhuyenMai()) {
                int pcmp = promoOnlyBestEff == null ? -1 : eff.compareTo(promoOnlyBestEff);
                if (pcmp < 0) {
                    promoOnlyBestEff = eff;
                    promoOnlyBestList = v.getGiaBan();
                }
            }
        }

        if (bestEff == null) {
            return TrangChuSanPhamGia.bangGiaNiemyet(BigDecimal.ZERO);
        }
        if (bestHasKm && bestListForDisplay != null) {
            return new TrangChuSanPhamGia(bestListForDisplay, bestEff, true, null, null, false);
        }
        if (!bestHasKm && promoOnlyBestEff != null && promoOnlyBestList != null) {
            return new TrangChuSanPhamGia(null, bestEff, false, promoOnlyBestList, promoOnlyBestEff, true);
        }
        return TrangChuSanPhamGia.bangGiaNiemyet(bestEff);
    }

    @Override
    @Transactional(readOnly = true)
    public KhuyenMaiPriceResult resolveForSanPhamChiTiet(
            Integer sanPhamChiTietId,
            BigDecimal giaBanNiemyet,
            List<KhuyenMai> chuongTrinhDangChay,
            String tenDanhMucSanPham) {
        if (sanPhamChiTietId == null) {
            return KhuyenMaiPriceResult.none(giaBanNiemyet);
        }
        if (giaBanNiemyet == null) {
            return KhuyenMaiPriceResult.none(BigDecimal.ZERO);
        }

        LocalDateTime now = LocalDateTime.now();
        List<KhuyenMai> catalog = chuongTrinhDangChay != null
                ? chuongTrinhDangChay
                : khuyenMaiService.getActivePromotions(now);

        String tenDm = tenDanhMucSanPham;
        if (tenDm == null) {
            tenDm = sanPhamChiTietRepository.findByIdWithDetails(sanPhamChiTietId)
                    .map(SanPhamChiTietKhuyenMaiServiceImpl::resolveTenDanhMucSanPham)
                    .orElse(null);
        }

        List<SanPhamChiTietKhuyenMai> links = sanPhamChiTietKhuyenMaiRepository
                .findActiveKhuyenMaiBySpctId(sanPhamChiTietId, now);

        Map<Integer, KhuyenMai> skuCandidates = new LinkedHashMap<>();
        Map<Integer, KhuyenMai> categoryCandidates = new LinkedHashMap<>();
        Map<Integer, KhuyenMai> allCandidates = new LinkedHashMap<>();

        if (links != null) {
            for (SanPhamChiTietKhuyenMai link : links) {
                KhuyenMai km = link.getKhuyenMai();
                if (km == null || km.getId() == null) {
                    continue;
                }
                skuCandidates.putIfAbsent(km.getId(), km);
            }
        }

        if (catalog != null) {
            for (KhuyenMai km : catalog) {
                if (km == null || km.getId() == null) {
                    continue;
                }
                KhuyenMai.PhamViApDung scope = km.getPhamViApDung() != null
                        ? km.getPhamViApDung()
                        : KhuyenMai.PhamViApDung.ALL;
                switch (scope) {
                    case SKU -> {
                    }
                    case CATEGORY -> {
                        if (appliesToDanhMucApDung(km, tenDm)) {
                            categoryCandidates.putIfAbsent(km.getId(), km);
                        }
                    }
                    case ALL -> allCandidates.putIfAbsent(km.getId(), km);
                }
            }
        }

        Map<Integer, KhuyenMai> allAvailableCandidates = new LinkedHashMap<>();
        allAvailableCandidates.putAll(skuCandidates);
        allAvailableCandidates.putAll(categoryCandidates);
        allAvailableCandidates.putAll(allCandidates);

        if (allAvailableCandidates.isEmpty()) {
            return KhuyenMaiPriceResult.none(giaBanNiemyet);
        }

        // Chọn khuyến mãi có giá tốt nhất cho khách hàng (best price wins).
        return allAvailableCandidates.values().stream()
                .filter(km -> (km.getTrangThai() == null || Boolean.TRUE.equals(km.getTrangThai())))
                .filter(km -> (km.getNgayBatDau() == null || !km.getNgayBatDau().isAfter(now)))
                .filter(km -> (km.getNgayKetThuc() == null || !km.getNgayKetThuc().isBefore(now)))
                .map(km -> KhuyenMaiPricing.compute(km, giaBanNiemyet))
                .filter(KhuyenMaiPriceResult::coKhuyenMai)
                .min(Comparator
                        .comparing(KhuyenMaiPriceResult::giaSauGiam)
                        .thenComparing(KhuyenMaiPriceResult::soTienGiam, Comparator.reverseOrder())
                        .thenComparing(r -> r.tenChuongTrinh() != null ? r.tenChuongTrinh() : "", String::compareTo))
                .orElse(KhuyenMaiPriceResult.none(giaBanNiemyet));
    }

    @Override
    @Transactional(readOnly = true)
    public KhuyenMaiPriceResult resolveBestForCartOrOrderLine(
            SanPhamChiTiet spct,
            LocalDateTime now,
            List<KhuyenMai> chuongTrinhDangChay) {
        if (spct == null) {
            return KhuyenMaiPriceResult.none(BigDecimal.ZERO);
        }
        BigDecimal base = spct.getGiaBan();
        if (base == null) {
            return KhuyenMaiPriceResult.none(BigDecimal.ZERO);
        }
        LocalDateTime t = now != null ? now : LocalDateTime.now();
        List<KhuyenMai> catalog = chuongTrinhDangChay != null
                ? chuongTrinhDangChay
                : khuyenMaiService.getActivePromotions(t);
        String tenDm = resolveTenDanhMucSanPham(spct);
        KhuyenMaiPriceResult fromVariant = resolveForSanPhamChiTiet(spct.getId(), base, catalog, tenDm);
        KhuyenMaiPriceResult fromProduct = SanPhamKhuyenMaiPricing.compute(spct.getSanPham(), base, t);
        return KhuyenMaiPricePick.pickBetter(fromVariant, fromProduct, base);
    }

    private static boolean appliesToDanhMucApDung(KhuyenMai km, String tenDanhMucSanPham) {
        KhuyenMai.PhamViApDung scope = km.getPhamViApDung();
        if (scope == null) {
            scope = KhuyenMai.PhamViApDung.ALL;
        }
        if (scope == KhuyenMai.PhamViApDung.ALL) {
            return true;
        }
        if (scope == KhuyenMai.PhamViApDung.SKU) {
            return false;
        }
        String dm = km.getDanhMucApDung();
        if (dm == null || dm.isBlank()) {
            return false;
        }
        if (tenDanhMucSanPham == null || tenDanhMucSanPham.isBlank()) {
            return false;
        }
        return dm.trim().equalsIgnoreCase(tenDanhMucSanPham.trim());
    }

    private static String resolveTenDanhMucSanPham(SanPhamChiTiet spct) {
        if (spct == null) {
            return null;
        }
        SanPham sp = spct.getSanPham();
        if (sp == null || sp.getDanhMuc() == null) {
            return null;
        }
        return sp.getDanhMuc().getTenDanhMuc();
    }
}
