package com.example.watchaura.controller;

import com.example.watchaura.dto.HoaDonDTO;
import com.example.watchaura.dto.HoaDonChiTietDTO;
import com.example.watchaura.dto.HoanTraDTO;
import com.example.watchaura.entity.KhuyenMai;
import com.example.watchaura.entity.Voucher;
import com.example.watchaura.repository.KhuyenMaiRepository;
import com.example.watchaura.repository.VoucherRepository;
import com.example.watchaura.service.HoaDonService;
import com.example.watchaura.service.HoanTraService;
import com.example.watchaura.service.KhachHangService;
import com.example.watchaura.service.SanPhamService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Tập hợp & tính toán JSON dashboard admin (filter thời gian, gross/net/hoàn tiền, biểu đồ).
 */
final class AdminDashboardData {

    private static final String PR_TODAY = "today";
    private static final String PR_YESTERDAY = "yesterday";
    private static final String PR_LAST7 = "last7";
    private static final String PR_LAST30 = "last30";
    private static final String PR_THIS_WEEK = "this_week";
    private static final String PR_THIS_MONTH = "this_month";
    private static final String PR_CUSTOM = "custom";

    private AdminDashboardData() {
    }

    static Map<String, Object> build(
            String rangePreset,
            String fromDate,
            String toDate,
            KhachHangService khachHangService,
            HoaDonService hoaDonService,
            SanPhamService sanPhamService,
            HoanTraService hoanTraService,
            KhuyenMaiRepository khuyenMaiRepository,
            VoucherRepository voucherRepository
    ) {
        LocalDate today = LocalDate.now();
        String presetNorm = normalizePreset(rangePreset);
        LocalDate chartFrom;
        LocalDate chartTo;
        {
            LocalDate[] b = resolveRangeBounds(presetNorm, fromDate, toDate, today);
            chartFrom = b[0];
            chartTo = b[1];
        }
        if (chartFrom.isAfter(chartTo)) {
            LocalDate t = chartFrom;
            chartFrom = chartTo;
            chartTo = t;
        }
        long daysInRange = ChronoUnit.DAYS.between(chartFrom, chartTo) + 1;
        if (daysInRange > 90) {
            chartFrom = chartTo.minusDays(89);
            daysInRange = 90;
        }

        String granularity = resolveGranularity(presetNorm, chartFrom, chartTo);
        LocalDate endDate = chartTo;
        LocalDate prevDay = endDate.minusDays(1);
        long rangeDays = ChronoUnit.DAYS.between(chartFrom, chartTo) + 1;
        LocalDate prevPeriodTo = chartFrom.minusDays(1);
        LocalDate prevPeriodFrom = prevPeriodTo.minusDays(rangeDays - 1);

        String revenueScopeText = scopeLabelVi(presetNorm, rangeDays, chartFrom, chartTo);
        String revenuePrimaryLabelVi = primaryLabelVi(presetNorm, chartFrom, chartTo, endDate);
        String chartTitleVi = chartTitleVi(granularity);

        Map<LocalDate, BigDecimal> grossByDay = new HashMap<>();
        Map<LocalDate, BigDecimal> refundsByDay = new HashMap<>();

        long customersInRange = 0L;
        long customersEndDay = 0L;
        long customersPrevDay = 0L;
        long customersPrevPeriod = 0L;

        long ordersEndDay = 0L;
        long ordersPrevDay = 0L;
        long ordersPrevPeriod = 0L;

        long canceledOrdersInRange = 0L;

        BigDecimal grossInRange = BigDecimal.ZERO;
        BigDecimal refundsInRange = BigDecimal.ZERO;

        BigDecimal grossEndDay = BigDecimal.ZERO;
        BigDecimal refundsEndDay = BigDecimal.ZERO;
        BigDecimal grossPrevDayOnly = BigDecimal.ZERO;
        BigDecimal refundsPrevDayOnly = BigDecimal.ZERO;

        BigDecimal grossPrevPeriod = BigDecimal.ZERO;
        BigDecimal refundsPrevPeriod = BigDecimal.ZERO;

        long orderProcessingCount = 0L;
        long orderDeliveredCount = 0L;
        long orderCanceledCount = 0L;

        Map<String, ProductAgg> productAggMap = new HashMap<>();

        List<HoaDonDTO> orders = List.of();
        List<HoanTraDTO> hoanTraList = List.of();
        try {
            orders = hoaDonService.getAll();
            hoanTraList = hoanTraService.getAllHoanTra();
        } catch (Exception ignored) {
        }

        try {
            for (var kh : khachHangService.getAll()) {
                if (kh == null) continue;
                LocalDateTime created = kh.getNgayTao();
                if (created == null) continue;
                LocalDate d = created.toLocalDate();
                if (!d.isBefore(chartFrom) && !d.isAfter(chartTo)) customersInRange++;
                if (d.equals(endDate)) customersEndDay++;
                if (d.equals(prevDay)) customersPrevDay++;
                if (!d.isBefore(prevPeriodFrom) && !d.isAfter(prevPeriodTo)) customersPrevPeriod++;
            }

            for (HoaDonDTO order : orders) {
                if (order == null || order.getNgayDat() == null) continue;
                LocalDate orderDate = order.getNgayDat().toLocalDate();
                String status = order.getTrangThaiDonHang();
                boolean eligible = isDashboardEligibleOrder(order);
                boolean isCanceled = "DA_HUY".equals(status);
                boolean countedPaid = isPaidForDashboardRevenue(order);
                BigDecimal grossOrder = grossMerchandiseAndShip(order);

                if (eligible) {
                    if (orderDate.equals(endDate)) ordersEndDay++;
                    if (orderDate.equals(prevDay)) ordersPrevDay++;
                    if (!orderDate.isBefore(prevPeriodFrom) && !orderDate.isAfter(prevPeriodTo)) ordersPrevPeriod++;
                }

                if (eligible && isCanceled && !orderDate.isBefore(chartFrom) && !orderDate.isAfter(chartTo)) {
                    canceledOrdersInRange++;
                }

                if (countedPaid) {
                    if (!orderDate.isBefore(chartFrom) && !orderDate.isAfter(chartTo)) {
                        grossInRange = grossInRange.add(grossOrder);
                        grossByDay.merge(orderDate, grossOrder, BigDecimal::add);
                    }
                    if (orderDate.equals(endDate)) grossEndDay = grossEndDay.add(grossOrder);
                    if (orderDate.equals(prevDay)) grossPrevDayOnly = grossPrevDayOnly.add(grossOrder);
                    if (!orderDate.isBefore(prevPeriodFrom) && !orderDate.isAfter(prevPeriodTo)) {
                        grossPrevPeriod = grossPrevPeriod.add(grossOrder);
                    }
                }

                if (eligible && !orderDate.isBefore(chartFrom) && !orderDate.isAfter(chartTo)) {
                    if (isCanceled) {
                        orderCanceledCount++;
                    } else if ("DA_GIAO".equals(status) || "HOAN_THANH".equals(status)) {
                        orderDeliveredCount++;
                    } else {
                        orderProcessingCount++;
                    }
                }

                if (countedPaid && !orderDate.isBefore(chartFrom) && !orderDate.isAfter(chartTo)) {
                    if (order.getItems() == null) continue;
                    for (HoaDonChiTietDTO item : order.getItems()) {
                        if (item == null) continue;
                        String productName = item.getTenSanPham() != null ? item.getTenSanPham() : "Không rõ";
                        long qty = item.getSoLuong() != null ? item.getSoLuong() : 0L;
                        BigDecimal revenue = item.getThanhTien() != null ? item.getThanhTien() : BigDecimal.ZERO;
                        ProductAgg agg = productAggMap.computeIfAbsent(productName, k -> new ProductAgg());
                        agg.quantity += qty;
                        agg.revenue = agg.revenue.add(revenue);
                    }
                }
            }

            for (HoanTraDTO h : hoanTraList) {
                if (h == null || !isCompletedRefundHoanTra(h)) continue;
                LocalDateTime ev = refundOccurredAt(h);
                if (ev == null) continue;
                LocalDate rd = ev.toLocalDate();
                BigDecimal amt = refundAmount(h);
                if (amt.compareTo(BigDecimal.ZERO) <= 0) continue;

                if (!rd.isBefore(chartFrom) && !rd.isAfter(chartTo)) {
                    refundsInRange = refundsInRange.add(amt);
                    refundsByDay.merge(rd, amt, BigDecimal::add);
                }
                if (rd.equals(endDate)) refundsEndDay = refundsEndDay.add(amt);
                if (rd.equals(prevDay)) refundsPrevDayOnly = refundsPrevDayOnly.add(amt);
                if (!rd.isBefore(prevPeriodFrom) && !rd.isAfter(prevPeriodTo)) {
                    refundsPrevPeriod = refundsPrevPeriod.add(amt);
                }
            }
        } catch (Exception ignored) {
        }

        BigDecimal netInRange = grossInRange.subtract(refundsInRange);
        BigDecimal netEndDay = grossEndDay.subtract(refundsEndDay);
        BigDecimal netPrevDay = grossPrevDayOnly.subtract(refundsPrevDayOnly);
        BigDecimal netPrevPeriod = grossPrevPeriod.subtract(refundsPrevPeriod);

        Map<LocalDate, BigDecimal> netByDay = new LinkedHashMap<>();
        for (LocalDate d = chartFrom; !d.isAfter(chartTo); d = d.plusDays(1)) {
            BigDecimal g = grossByDay.getOrDefault(d, BigDecimal.ZERO);
            BigDecimal r = refundsByDay.getOrDefault(d, BigDecimal.ZERO);
            netByDay.put(d, g.subtract(r));
        }

        List<Map<String, Object>> revenueSeries = buildRevenueSeries(
                granularity, chartFrom, chartTo, orders, hoanTraList);

        BigDecimal maxVal = BigDecimal.valueOf(Long.MIN_VALUE);
        BigDecimal minVal = BigDecimal.valueOf(Long.MAX_VALUE);
        String maxLabel = "";
        String minLabel = "";
        for (Map<String, Object> pt : revenueSeries) {
            BigDecimal v = BigDecimal.valueOf(((Number) pt.get("value")).doubleValue());
            String lb = String.valueOf(pt.get("label"));
            if (v.compareTo(maxVal) > 0) {
                maxVal = v;
                maxLabel = lb;
            }
            if (v.compareTo(minVal) < 0) {
                minVal = v;
                minLabel = lb;
            }
        }
        if (revenueSeries.isEmpty()) {
            maxVal = BigDecimal.ZERO;
            minVal = BigDecimal.ZERO;
        }

        Map<String, Object> revenueMax = !revenueSeries.isEmpty() && maxVal.compareTo(BigDecimal.valueOf(Long.MIN_VALUE)) != 0
                ? Map.of("label", maxLabel, "value", maxVal.doubleValue())
                : null;
        Map<String, Object> revenueMin = !revenueSeries.isEmpty() && minVal.compareTo(BigDecimal.valueOf(Long.MAX_VALUE)) != 0
                ? Map.of("label", minLabel, "value", minVal.doubleValue())
                : null;

        long totalOrdersRange = orderProcessingCount + orderDeliveredCount + orderCanceledCount;
        Map<String, Object> orderStatus = new HashMap<>();
        orderStatus.put("totalOrders", totalOrdersRange);
        orderStatus.put("processingCount", orderProcessingCount);
        orderStatus.put("deliveredCount", orderDeliveredCount);
        orderStatus.put("canceledCount", orderCanceledCount);
        orderStatus.put("labels", List.of("Đang xử lý", "Đã giao", "Đã hủy"));
        orderStatus.put("colors", List.of("#2563eb", "#22c55e", "#ef4444"));

        BigDecimal totalRevenueTop = productAggMap.values().stream()
                .map(agg -> agg.revenue != null ? agg.revenue : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Map<String, Object>> topProducts = productAggMap.entrySet().stream()
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("name", e.getKey());
                    m.put("quantity", e.getValue().quantity);
                    double rev = e.getValue().revenue != null ? e.getValue().revenue.doubleValue() : 0d;
                    m.put("revenue", rev);
                    double sharePercent = totalRevenueTop.compareTo(BigDecimal.ZERO) == 0
                            ? 0d
                            : (rev * 100.0d / totalRevenueTop.doubleValue());
                    m.put("sharePercent", sharePercent);
                    return m;
                })
                .sorted((a, b) -> {
                    double ra = ((Number) a.get("revenue")).doubleValue();
                    double rb = ((Number) b.get("revenue")).doubleValue();
                    int byRevenue = Double.compare(rb, ra);
                    if (byRevenue != 0) return byRevenue;
                    long qa = ((Number) a.get("quantity")).longValue();
                    long qb = ((Number) b.get("quantity")).longValue();
                    return Long.compare(qb, qa);
                })
                .limit(5)
                .collect(Collectors.toList());

        List<Map<String, Object>> warnings = new ArrayList<>();
        List<Map<String, Object>> insights = new ArrayList<>();

        BigDecimal revenueAvgDaily = BigDecimal.ZERO;
        int nonZeroDays = 0;
        BigDecimal sumNonZero = BigDecimal.ZERO;
        for (BigDecimal v : netByDay.values()) {
            BigDecimal safeV = v != null ? v : BigDecimal.ZERO;
            if (safeV.compareTo(BigDecimal.ZERO) > 0) {
                sumNonZero = sumNonZero.add(safeV);
                nonZeroDays++;
            }
        }
        if (nonZeroDays > 0) {
            revenueAvgDaily = sumNonZero.divide(BigDecimal.valueOf(nonZeroDays), 2, RoundingMode.HALF_UP);
        }

        if (netInRange.compareTo(BigDecimal.ZERO) > 0 && maxVal.compareTo(BigDecimal.ZERO) > 0) {
            if (revenueAvgDaily.compareTo(BigDecimal.ZERO) > 0) {
                double pct = maxVal.subtract(revenueAvgDaily).doubleValue() * 100.0d / revenueAvgDaily.doubleValue();
                String sign = pct >= 0 ? "+" : "-";
                String pctTxt = sign + String.format(Locale.US, "%.0f", Math.abs(pct)) + "%";
                insights.add(item(
                        "📈 Mốc <strong>" + maxLabel + "</strong> đạt <strong>" + formatMoneyInsight(maxVal) + "</strong> (<strong>" + pctTxt + "</strong> vs trung bình trong kỳ)",
                        pct >= 0 ? "good" : "danger"
                ));
            } else {
                insights.add(item(
                        "📈 Điểm cao nhất trong kỳ: <strong>" + maxLabel + "</strong> (<strong>" + formatMoneyInsight(maxVal) + "</strong>)",
                        "info"
                ));
            }
        }

        if (canceledOrdersInRange == 0) {
            insights.add(item("Không có đơn bị hủy trong kỳ đang chọn.", "good"));
        } else {
            insights.add(item(
                    "Trong kỳ: <strong>" + canceledOrdersInRange + "</strong> đơn hủy",
                    "danger"
            ));
        }

        String bestSellerName = !topProducts.isEmpty() ? (String) topProducts.get(0).get("name") : null;
        if (bestSellerName != null) {
            long bestQty = ((Number) topProducts.get(0).get("quantity")).longValue();
            double bestRev = ((Number) topProducts.get(0).get("revenue")).doubleValue();
            double bestShare = ((Number) topProducts.get(0).getOrDefault("sharePercent", 0d)).doubleValue();
            BigDecimal bestRevBD = BigDecimal.valueOf(bestRev);
            insights.add(item(
                    "🏆 <strong>" + bestSellerName + "</strong> đạt <strong>" + bestQty + "</strong> bán, doanh thu dòng <strong>" +
                            formatMoneyInsight(bestRevBD) + "</strong> (đóng góp <strong>" + String.format(Locale.US, "%.1f", bestShare) + "%</strong>)",
                    "good"
            ));
        }

        int zeroStreak = 0;
        LocalDate streakStart = null;
        LocalDate streakEnd = null;
        for (Map.Entry<LocalDate, BigDecimal> e : netByDay.entrySet()) {
            BigDecimal v = e.getValue() != null ? e.getValue() : BigDecimal.ZERO;
            if (v.compareTo(BigDecimal.ZERO) == 0) {
                zeroStreak++;
                if (zeroStreak == 1) streakStart = e.getKey();
                streakEnd = e.getKey();
            } else {
                if (zeroStreak >= 3) break;
                zeroStreak = 0;
                streakStart = null;
                streakEnd = null;
            }
        }
        if (zeroStreak >= 3 && streakStart != null && streakEnd != null) {
            warnings.add(item(
                    "🔴 3 ngày liên tiếp <strong>thực thu = 0đ</strong> (" +
                            streakStart.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")) + " - " +
                            streakEnd.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")) + ").",
                    "danger"
            ));
        }

        double cancelRate = totalOrdersRange == 0 ? 0d : (orderCanceledCount * 100.0d / totalOrdersRange);
        if (cancelRate >= 20d && totalOrdersRange > 0) {
            warnings.add(item(
                    "🔴 Tỉ lệ hủy đơn cao: <strong>" + String.format(Locale.US, "%.1f", cancelRate) + "%</strong>.",
                    "danger"
            ));
        }

        if (netPrevPeriod.compareTo(BigDecimal.ZERO) > 0
                && netInRange.compareTo(netPrevPeriod.multiply(new BigDecimal("0.5"))) < 0) {
            Double pct = percentChangeBDSpec(netInRange, netPrevPeriod);
            String pctTxt = pct == null ? "" : (" (" + (pct >= 0 ? "+" : "-") + String.format(Locale.US, "%.0f", Math.abs(pct)) + "%)");
            warnings.add(item(
                    "🔴 Doanh thu thực thu giảm mạnh so với kỳ trước: <strong>" + formatMoneyInsight(netInRange) +
                            "</strong> (kỳ trước: <strong>" + formatMoneyInsight(netPrevPeriod) + "</strong>)" + pctTxt,
                    "danger"
            ));
        }

        if (warnings.isEmpty()) {
            warnings.add(item("Không phát hiện bất thường trong giai đoạn này.", "good"));
        }

        Double customersDayPct = percentChangeLongPractical(customersEndDay, customersPrevDay);
        Double customersPeriodPct = percentChangeLongPractical(customersInRange, customersPrevPeriod);

        Double ordersDayPct = percentChangeLongPractical(ordersEndDay, ordersPrevDay);
        Double ordersPeriodPct = percentChangeLongPractical(totalOrdersRange, ordersPrevPeriod);

        Double revenueDayPct = percentChangeBDPractical(netEndDay, netPrevDay);
        Double revenuePeriodPct = percentChangeBDPractical(netInRange, netPrevPeriod);

        Map<String, Object> revenuePrimaryMap = buildRevenuePrimaryKpi(
                presetNorm, today, chartFrom, chartTo, endDate, prevDay, orders, hoanTraList);

        long totalProducts = 0L;
        try {
            totalProducts = sanPhamService.getAllSanPham().size();
        } catch (Exception ignored) {
        }

        Map<String, Object> kpi = new HashMap<>();
        Map<String, Object> customersMap = new HashMap<>();
        customersMap.put("value", customersInRange);
        customersMap.put("dayCompare", compareBlock(customersEndDay, customersPrevDay, customersDayPct, "So với ngày trước (cuối kỳ)"));
        customersMap.put("periodCompare", compareBlock(customersInRange, customersPrevPeriod, customersPeriodPct, "So với kỳ trước"));
        kpi.put("customers", customersMap);

        Map<String, Object> ordersMap = new HashMap<>();
        ordersMap.put("value", totalOrdersRange);
        ordersMap.put("dayCompare", compareBlock(ordersEndDay, ordersPrevDay, ordersDayPct, "So với ngày trước (cuối kỳ)"));
        ordersMap.put("periodCompare", compareBlock(totalOrdersRange, ordersPrevPeriod, ordersPeriodPct, "So với kỳ trước"));
        kpi.put("orders", ordersMap);

        Map<String, Object> productsMap = new HashMap<>();
        productsMap.put("value", totalProducts);
        kpi.put("products", productsMap);

        Map<String, Object> revenueMap = new HashMap<>();
        revenueMap.put("value", netInRange.doubleValue());
        revenueMap.put("grossValue", grossInRange.doubleValue());
        revenueMap.put("returnsValue", refundsInRange.doubleValue());
        revenueMap.put("netValue", netInRange.doubleValue());
        revenueMap.put("subtitle",
                "Thu gộp " + formatMoneyInsight(grossInRange) + " · Hoàn trả " + formatMoneyInsight(refundsInRange) + " · Thực thu "
                        + formatMoneyInsight(netInRange));
        revenueMap.put("dayCompare", compareBlockMoney(netEndDay, netPrevDay, revenueDayPct, "So với ngày trước (cuối kỳ)"));
        revenueMap.put("periodCompare", compareBlockMoney(netInRange, netPrevPeriod, revenuePeriodPct, "So với kỳ trước"));
        kpi.put("revenue", revenueMap);

        kpi.put("revenuePrimary", revenuePrimaryMap);

        List<Map<String, Object>> lowStock = new ArrayList<>();
        long lowStockTotal = 0L;
        try {
            for (var sp : sanPhamService.getAllSanPham()) {
                if (sp == null || sp.getSoLuongTon() == null) continue;
                if (sp.getSoLuongTon() < 10) {
                    lowStockTotal++;
                    if (lowStock.size() >= 5) continue;
                    Map<String, Object> row = new HashMap<>();
                    row.put("title", sp.getTenSanPham());
                    row.put("subtitle", "Tồn kho: " + sp.getSoLuongTon());
                    row.put("meta", sp.getSoLuongTon() + " sản phẩm còn lại");
                    lowStock.add(row);
                }
            }
        } catch (Exception ignored) {
        }

        List<Map<String, Object>> recentCustomers = new ArrayList<>();
        try {
            for (var kh : khachHangService.getAll()) {
                if (kh == null) continue;
                Map<String, Object> row = new HashMap<>();
                row.put("title", kh.getTenNguoiDung());
                row.put("subtitle", kh.getEmail());
                row.put("meta", kh.getNgayTao() != null ? kh.getNgayTao().toLocalDate().toString() : "");
                recentCustomers.add(row);
            }
        } catch (Exception ignored) {
        }
        recentCustomers = recentCustomers.stream().limit(5).collect(Collectors.toList());

        List<Map<String, Object>> recentOrders = new ArrayList<>();
        try {
            recentOrders = orders.stream()
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(HoaDonDTO::getNgayDat, Comparator.nullsLast(Comparator.reverseOrder())))
                    .limit(5)
                    .map(order -> {
                        Map<String, Object> row = new HashMap<>();
                        row.put("title", order.getMaDonHang());
                        row.put("subtitle", order.getTenKhachHang());
                        row.put("meta", order.getNgayDat() != null ? order.getNgayDat().toString() : "");
                        return row;
                    })
                    .collect(Collectors.toList());
        } catch (Exception ignored) {
        }

        List<Map<String, Object>> expiringCampaigns = new ArrayList<>();
        try {
            for (KhuyenMai km : khuyenMaiRepository.findAll()) {
                if (km == null || km.getNgayKetThuc() == null) continue;
                if (km.getNgayKetThuc().isBefore(LocalDateTime.now())) continue;
                if (km.getNgayKetThuc().isBefore(LocalDateTime.now().plusDays(7))) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("title", km.getTenChuongTrinh());
                    row.put("subtitle", km.getNgayKetThuc().toLocalDate().toString());
                    row.put("meta", "Còn sắp hết hạn");
                    expiringCampaigns.add(row);
                }
            }
            for (Voucher v : voucherRepository.findAll()) {
                if (v == null || v.getNgayKetThuc() == null) continue;
                if (v.getNgayKetThuc().isBefore(LocalDateTime.now())) continue;
                if (v.getNgayKetThuc().isBefore(LocalDateTime.now().plusDays(7))) {
                    Map<String, Object> row = new HashMap<>();
                    row.put("title", v.getTenVoucher());
                    row.put("subtitle", v.getNgayKetThuc().toLocalDate().toString());
                    row.put("meta", "Voucher sắp hết hạn");
                    expiringCampaigns.add(row);
                }
            }
        } catch (Exception ignored) {
        }
        expiringCampaigns = expiringCampaigns.stream().limit(5).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("rangePreset", presetNorm);
        response.put("chartFrom", chartFrom.toString());
        response.put("chartTo", chartTo.toString());
        response.put("prevPeriodFrom", prevPeriodFrom.toString());
        response.put("prevPeriodTo", prevPeriodTo.toString());
        response.put("revenueGranularity", granularity);
        response.put("chartTitle", chartTitleVi);
        response.put("revenuePrimaryLabel", revenuePrimaryLabelVi);
        response.put("revenueScopeText", revenueScopeText);

        kpi.put("lowStock", Map.of("value", lowStockTotal, "subtitle", "Tồn kho dưới 10"));
        kpi.put("promotions", Map.of("value", khuyenMaiRepository.findAll().size(), "subtitle", "Khuyến mãi"));
        kpi.put("vouchers", Map.of("value", voucherRepository.findAll().size(), "subtitle", "Voucher"));

        response.put("kpi", kpi);
        response.put("revenueSeries", revenueSeries);
        response.put("revenueDaily", revenueSeries);
        response.put("revenueMax", revenueMax);
        response.put("revenueMin", revenueMin);
        response.put("orderStatus", orderStatus);
        response.put("topProducts", topProducts);
        response.put("insights", insights);
        response.put("warnings", warnings);
        response.put("lowStock", lowStock);
        response.put("recentOrders", recentOrders);
        response.put("recentCustomers", recentCustomers);
        response.put("expiringCampaigns", expiringCampaigns);

        return response;
    }

    static String buildCacheKey(String rangePreset, String fromDate, String toDate) {
        LocalDate today = LocalDate.now();
        String presetNorm = normalizePreset(rangePreset);
        LocalDate[] b = resolveRangeBounds(presetNorm, fromDate, toDate, today);
        return presetNorm + ":" + b[0] + ":" + b[1];
    }

    private static String normalizePreset(String rangePreset) {
        if (rangePreset == null || rangePreset.isBlank()) return PR_LAST7;
        String s = rangePreset.trim().toLowerCase(Locale.ROOT);
        if ("7".equals(s)) return PR_LAST7;
        if ("30".equals(s)) return PR_LAST30;
        return s;
    }

    private static LocalDate[] resolveRangeBounds(String preset, String fromDate, String toDate, LocalDate today) {
        switch (preset) {
            case PR_TODAY:
                return new LocalDate[]{today, today};
            case PR_YESTERDAY:
                return new LocalDate[]{today.minusDays(1), today.minusDays(1)};
            case PR_LAST7:
                return new LocalDate[]{today.minusDays(6), today};
            case PR_LAST30:
                return new LocalDate[]{today.minusDays(29), today};
            case PR_THIS_WEEK:
                LocalDate mon = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                return new LocalDate[]{mon, today};
            case PR_THIS_MONTH:
                return new LocalDate[]{today.with(TemporalAdjusters.firstDayOfMonth()), today};
            case PR_CUSTOM:
                LocalDate df = parseDateOrDefault(fromDate, today.minusDays(6));
                LocalDate dt = parseDateOrDefault(toDate, today);
                if (df.isAfter(dt)) {
                    LocalDate x = df;
                    df = dt;
                    dt = x;
                }
                long span = ChronoUnit.DAYS.between(df, dt) + 1;
                if (span > 90) df = dt.minusDays(89);
                return new LocalDate[]{df, dt};
            default:
                return new LocalDate[]{today.minusDays(6), today};
        }
    }

    private static String resolveGranularity(String preset, LocalDate from, LocalDate to) {
        long days = ChronoUnit.DAYS.between(from, to) + 1;
        if (PR_TODAY.equals(preset) || PR_YESTERDAY.equals(preset) || days == 1) {
            return "hour";
        }
        if (PR_CUSTOM.equals(preset) && days > 60) {
            return "week";
        }
        return "day";
    }

    private static String scopeLabelVi(String preset, long rangeDays, LocalDate from, LocalDate to) {
        switch (preset) {
            case PR_TODAY:
                return "Hôm nay";
            case PR_YESTERDAY:
                return "Hôm qua";
            case PR_LAST7:
                return "7 ngày qua";
            case PR_LAST30:
                return "30 ngày qua";
            case PR_THIS_WEEK:
                return "Tuần này";
            case PR_THIS_MONTH:
                return "Tháng này";
            case PR_CUSTOM:
                return from.toString() + " → " + to.toString();
            default:
                return rangeDays + " ngày";
        }
    }

    private static String primaryLabelVi(String preset, LocalDate chartFrom, LocalDate chartTo, LocalDate endDate) {
        long spanDays = ChronoUnit.DAYS.between(chartFrom, chartTo) + 1;
        if (spanDays > 1L && !PR_TODAY.equals(preset) && !PR_YESTERDAY.equals(preset)) {
            return "Ngày cuối kỳ (" + endDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ")";
        }
        switch (preset) {
            case PR_TODAY:
                return "Hôm nay";
            case PR_YESTERDAY:
                return "Hôm qua";
            case PR_LAST7:
                return "7 ngày qua";
            case PR_LAST30:
                return "30 ngày qua";
            case PR_THIS_WEEK:
                return "Tuần này";
            case PR_THIS_MONTH:
                return "Tháng này";
            case PR_CUSTOM:
                return "Kỳ tùy chọn";
            default:
                return "Kỳ đã chọn";
        }
    }

    private static String chartTitleVi(String granularity) {
        switch (granularity) {
            case "hour":
                return "Thực thu theo giờ (đã trừ hoàn trả)";
            case "week":
                return "Thực thu theo tuần (đã trừ hoàn trả)";
            default:
                return "Thực thu theo ngày (đã trừ hoàn trả)";
        }
    }

    private static List<Map<String, Object>> buildRevenueSeries(
            String granularity,
            LocalDate chartFrom,
            LocalDate chartTo,
            List<HoaDonDTO> orders,
            List<HoanTraDTO> hoanTraList
    ) {
        List<Map<String, Object>> out = new ArrayList<>();
        if ("hour".equals(granularity)) {
            BigDecimal[] g = new BigDecimal[24];
            BigDecimal[] r = new BigDecimal[24];
            Arrays.fill(g, BigDecimal.ZERO);
            Arrays.fill(r, BigDecimal.ZERO);
            for (HoaDonDTO order : orders) {
                if (order == null || order.getNgayDat() == null) continue;
                if (!isPaidForDashboardRevenue(order)) continue;
                LocalDateTime placed = order.getNgayDat();
                if (!placed.toLocalDate().equals(chartFrom)) continue;
                int h = placed.getHour();
                g[h] = g[h].add(grossMerchandiseAndShip(order));
            }
            for (HoanTraDTO h : hoanTraList) {
                if (h == null || !isCompletedRefundHoanTra(h)) continue;
                LocalDateTime ev = refundOccurredAt(h);
                if (ev == null || !ev.toLocalDate().equals(chartFrom)) continue;
                int hr = ev.getHour();
                r[hr] = r[hr].add(refundAmount(h));
            }
            for (int h = 0; h < 24; h++) {
                double net = g[h].subtract(r[h]).doubleValue();
                Map<String, Object> row = new HashMap<>();
                row.put("label", h + "h");
                row.put("value", net);
                row.put("date", chartFrom.toString());
                row.put("bucket", "hour");
                out.add(row);
            }
            return out;
        }
        if ("week".equals(granularity)) {
            Map<LocalDate, BigDecimal> weekNet = new TreeMap<>();
            for (LocalDate d = chartFrom; !d.isAfter(chartTo); d = d.plusDays(1)) {
                BigDecimal dg = BigDecimal.ZERO;
                BigDecimal dr = BigDecimal.ZERO;
                for (HoaDonDTO order : orders) {
                    if (order == null || order.getNgayDat() == null) continue;
                    if (!isPaidForDashboardRevenue(order)) continue;
                    if (!order.getNgayDat().toLocalDate().equals(d)) continue;
                    dg = dg.add(grossMerchandiseAndShip(order));
                }
                for (HoanTraDTO ht : hoanTraList) {
                    if (ht == null || !isCompletedRefundHoanTra(ht)) continue;
                    LocalDateTime ev = refundOccurredAt(ht);
                    if (ev == null || !ev.toLocalDate().equals(d)) continue;
                    dr = dr.add(refundAmount(ht));
                }
                LocalDate wk = d.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                BigDecimal dayNet = dg.subtract(dr);
                weekNet.merge(wk, dayNet, BigDecimal::add);
            }
            java.time.format.DateTimeFormatter df = java.time.format.DateTimeFormatter.ofPattern("dd/MM");
            for (Map.Entry<LocalDate, BigDecimal> e : weekNet.entrySet()) {
                LocalDate start = e.getKey();
                LocalDate end = start.plusDays(6);
                if (end.isAfter(chartTo)) end = chartTo;
                Map<String, Object> row = new HashMap<>();
                row.put("label", start.format(df) + " – " + end.format(df));
                row.put("value", e.getValue().doubleValue());
                row.put("date", start.toString());
                row.put("bucket", "week");
                out.add(row);
            }
            return out;
        }
        // day
        for (LocalDate d = chartFrom; !d.isAfter(chartTo); d = d.plusDays(1)) {
            BigDecimal dg = BigDecimal.ZERO;
            BigDecimal dr = BigDecimal.ZERO;
            for (HoaDonDTO order : orders) {
                if (order == null || order.getNgayDat() == null) continue;
                if (!isPaidForDashboardRevenue(order)) continue;
                if (!order.getNgayDat().toLocalDate().equals(d)) continue;
                dg = dg.add(grossMerchandiseAndShip(order));
            }
            for (HoanTraDTO ht : hoanTraList) {
                if (ht == null || !isCompletedRefundHoanTra(ht)) continue;
                LocalDateTime ev = refundOccurredAt(ht);
                if (ev == null || !ev.toLocalDate().equals(d)) continue;
                dr = dr.add(refundAmount(ht));
            }
            Map<String, Object> row = new HashMap<>();
            row.put("label", d.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")));
            row.put("value", dg.subtract(dr).doubleValue());
            row.put("date", d.toString());
            row.put("bucket", "day");
            out.add(row);
        }
        return out;
    }

    private static Map<String, Object> buildRevenuePrimaryKpi(
            String preset,
            LocalDate today,
            LocalDate chartFrom,
            LocalDate chartTo,
            LocalDate endDate,
            LocalDate prevDay,
            List<HoaDonDTO> orders,
            List<HoanTraDTO> hoanTraList
    ) {
        LocalDateTime now = LocalDateTime.now();
        BigDecimal curNet;
        BigDecimal prevNet;
        String cmpLabel;

        if (PR_TODAY.equals(preset)) {
            curNet = netUpTo(orders, hoanTraList, today, now);
            prevNet = netUpTo(orders, hoanTraList, today.minusDays(1), now.minusDays(1));
            cmpLabel = "So với hôm qua cùng thời điểm";
        } else if (PR_YESTERDAY.equals(preset)) {
            LocalDate y = today.minusDays(1);
            LocalDate y2 = today.minusDays(2);
            curNet = netFullDay(orders, hoanTraList, y);
            prevNet = netFullDay(orders, hoanTraList, y2);
            cmpLabel = "So với hôm kia (cả ngày)";
        } else if (ChronoUnit.DAYS.between(chartFrom, chartTo) == 0) {
            curNet = netFullDay(orders, hoanTraList, endDate);
            prevNet = netFullDay(orders, hoanTraList, prevDay);
            cmpLabel = "So với ngày trước";
        } else {
            LocalDate lastDayPrevPeriod = chartFrom.minusDays(1);
            curNet = netFullDay(orders, hoanTraList, endDate);
            prevNet = netFullDay(orders, hoanTraList, lastDayPrevPeriod);
            cmpLabel = "So với ngày cuối kỳ trước";
        }

        Double pct = percentChangeBDPractical(curNet, prevNet);
        Map<String, Object> m = compareBlockMoney(curNet, prevNet, pct, cmpLabel);
        m.put("refreshIntervalMs", 300000);
        m.put("grossHint", "Thực thu = Thu gộp (tiền hàng + ship) − Hoàn trả trong cùng mốc thời gian");
        return m;
    }

    private static BigDecimal netUpTo(List<HoaDonDTO> orders, List<HoanTraDTO> hoanTraList, LocalDate day, LocalDateTime cutoff) {
        BigDecimal g = BigDecimal.ZERO;
        BigDecimal r = BigDecimal.ZERO;
        for (HoaDonDTO order : orders) {
            if (order == null || order.getNgayDat() == null) continue;
            if (!isPaidForDashboardRevenue(order)) continue;
            LocalDateTime placed = order.getNgayDat();
            if (placed.toLocalDate().equals(day) && !placed.isAfter(cutoff)) {
                g = g.add(grossMerchandiseAndShip(order));
            }
        }
        for (HoanTraDTO h : hoanTraList) {
            if (h == null || !isCompletedRefundHoanTra(h)) continue;
            LocalDateTime ev = refundOccurredAt(h);
            if (ev == null) continue;
            if (ev.toLocalDate().equals(day) && !ev.isAfter(cutoff)) {
                r = r.add(refundAmount(h));
            }
        }
        return g.subtract(r);
    }

    private static BigDecimal netFullDay(List<HoaDonDTO> orders, List<HoanTraDTO> hoanTraList, LocalDate day) {
        return netUpTo(orders, hoanTraList, day, LocalDateTime.of(day, java.time.LocalTime.of(23, 59, 59)));
    }

    private static boolean isCompletedRefundHoanTra(HoanTraDTO h) {
        String st = h.getTrangThai();
        return "DA_HOAN_TIEN".equals(st) || "DA_XU_LY".equals(st);
    }

    private static LocalDateTime refundOccurredAt(HoanTraDTO h) {
        if (h.getNgayHoanTien() != null) return h.getNgayHoanTien();
        if (h.getNgayXuLy() != null) return h.getNgayXuLy();
        return h.getNgayYeuCau();
    }

    private static BigDecimal refundAmount(HoanTraDTO h) {
        if (h.getSoTienHoanThucTe() != null && h.getSoTienHoanThucTe().compareTo(BigDecimal.ZERO) > 0) {
            return h.getSoTienHoanThucTe();
        }
        return h.getSoTienHoanTra() != null ? h.getSoTienHoanTra() : BigDecimal.ZERO;
    }

    private static BigDecimal grossMerchandiseAndShip(HoaDonDTO order) {
        BigDecimal tam = order.getTongTienTamTinh() != null ? order.getTongTienTamTinh() : BigDecimal.ZERO;
        BigDecimal ship = order.getPhiVanChuyen() != null ? order.getPhiVanChuyen() : BigDecimal.ZERO;
        return tam.add(ship);
    }

    private static boolean isDashboardEligibleOrder(HoaDonDTO order) {
        if (order == null) return false;
        if (Boolean.FALSE.equals(order.getTrangThai())) return false;
        return !"DRAFT_OFFLINE".equals(order.getTrangThaiDonHang());
    }

    private static boolean isPaidForDashboardRevenue(HoaDonDTO order) {
        if (!isDashboardEligibleOrder(order)) return false;
        String tt = order.getTrangThaiThanhToan();
        if (tt == null) return false;
        String n = tt.trim();
        return "DA_THANH_TOAN".equals(n) || "DA THANH TOAN".equals(n);
    }

    private static LocalDate parseDateOrDefault(String input, LocalDate defaultValue) {
        if (input == null || input.isBlank()) return defaultValue;
        try {
            return LocalDate.parse(input.trim());
        } catch (DateTimeParseException e) {
            return defaultValue;
        }
    }

    private static Double percentChangeBDSpec(BigDecimal current, BigDecimal previous) {
        if (previous == null || current == null) return null;
        if (previous.compareTo(BigDecimal.ZERO) <= 0) {
            if (previous.compareTo(BigDecimal.ZERO) == 0) {
                if (current.compareTo(BigDecimal.ZERO) == 0) return 0d;
                return 100d;
            }
            return null;
        }
        return current.subtract(previous).doubleValue() * 100.0d / previous.doubleValue();
    }

    private static Double percentChangeLongPractical(long current, long previous) {
        if (previous == 0L) {
            return current == 0L ? 0d : null;
        }
        return ((double) (current - previous)) * 100.0d / (double) previous;
    }

    private static Double percentChangeBDPractical(BigDecimal current, BigDecimal previous) {
        BigDecimal c = current != null ? current : BigDecimal.ZERO;
        BigDecimal p = previous != null ? previous : BigDecimal.ZERO;
        if (p.compareTo(BigDecimal.ZERO) == 0) {
            return c.compareTo(BigDecimal.ZERO) == 0 ? 0d : null;
        }
        if (p.compareTo(BigDecimal.ZERO) < 0) {
            return null;
        }
        return c.subtract(p).doubleValue() * 100.0d / p.doubleValue();
    }

    private static Map<String, Object> compareBlock(long today, long yesterday, Double percentChange, String compareLabel) {
        Map<String, Object> m = new HashMap<>();
        m.put("current", today);
        m.put("previous", yesterday);
        m.put("percentChange", percentChange);
        m.put("trend", trendFromPercent(percentChange));
        m.put("compareLabel", compareLabel);
        m.put("tooltip", compareLabel + " (" + yesterday + " → " + today + ")");
        return m;
    }

    private static Map<String, Object> compareBlockMoney(BigDecimal today, BigDecimal yesterday, Double percentChange, String compareLabel) {
        BigDecimal t = today != null ? today : BigDecimal.ZERO;
        BigDecimal y = yesterday != null ? yesterday : BigDecimal.ZERO;
        Map<String, Object> m = new HashMap<>();
        m.put("current", t.doubleValue());
        m.put("previous", y.doubleValue());
        m.put("percentChange", percentChange);
        m.put("trend", trendFromPercent(percentChange));
        m.put("compareLabel", compareLabel);
        m.put("tooltip", compareLabel + " (" + formatMoneyInsight(y) + " → " + formatMoneyInsight(t) + ")");
        return m;
    }

    private static String trendFromPercent(Double percentChange) {
        if (percentChange == null) return null;
        if (percentChange > 0d) return "up";
        if (percentChange < 0d) return "down";
        return "flat";
    }

    private static Map<String, Object> item(String text, String tone) {
        Map<String, Object> m = new HashMap<>();
        m.put("text", text);
        m.put("tone", tone);
        return m;
    }

    private static String formatMoneyInsight(BigDecimal value) {
        if (value == null) return "0";
        BigDecimal v = value.stripTrailingZeros();
        BigDecimal abs = v.abs();

        BigDecimal billion = new BigDecimal("1000000000");
        BigDecimal million = new BigDecimal("1000000");
        BigDecimal thousand = new BigDecimal("1000");

        RoundingMode rm = RoundingMode.HALF_UP;

        if (abs.compareTo(billion) >= 0) {
            BigDecimal shortV = v.divide(billion, 2, rm).stripTrailingZeros();
            return shortV.toPlainString() + "B";
        }
        if (abs.compareTo(million) >= 0) {
            BigDecimal shortV = v.divide(million, 2, rm).stripTrailingZeros();
            return shortV.toPlainString() + "M";
        }
        if (abs.compareTo(thousand) >= 0) {
            BigDecimal shortV = v.divide(thousand, 1, rm).stripTrailingZeros();
            return shortV.toPlainString() + "K";
        }

        java.text.NumberFormat nf = java.text.NumberFormat.getInstance(new Locale("vi", "VN"));
        nf.setGroupingUsed(true);
        nf.setMaximumFractionDigits(0);
        return nf.format(v) + "đ";
    }

    private static final class ProductAgg {
        private long quantity = 0L;
        private BigDecimal revenue = BigDecimal.ZERO;
    }
}
