package com.example.watchaura.controller;

import com.example.watchaura.annotation.RequiresRole;
import com.example.watchaura.entity.HoaDon;
import com.example.watchaura.repository.HoaDonRepository;
import com.example.watchaura.service.HoaDonService;
import com.example.watchaura.service.KhachHangService;
import com.example.watchaura.service.SanPhamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@RequiresRole({"Admin", "Quản lý"})
public class AdminController {

    private final KhachHangService khachHangService;
    private final HoaDonService hoaDonService;
    private final SanPhamService sanPhamService;

    /** Cấu hình in-memory (có thể thay bằng DB sau). */
    private static final Map<String, String> SETTINGS_STORE = new HashMap<>();

    private static final long DASHBOARD_CACHE_TTL_MS = 60_000L;
    private static final ConcurrentHashMap<String, CacheEntry> DASHBOARD_CACHE = new ConcurrentHashMap<>();
    private final HoaDonRepository hoaDonRepository;

    private static final class CacheEntry {
        private final long createdAtMs;
        private final Map<String, Object> data;

        private CacheEntry(long createdAtMs, Map<String, Object> data) {
            this.createdAtMs = createdAtMs;
            this.data = data;
        }

        private boolean isValid() {
            return System.currentTimeMillis() - createdAtMs <= DASHBOARD_CACHE_TTL_MS;
        }
    }

    @GetMapping
    public String getAdminPage(Model model) {
        model.addAttribute("title", "Dashboard");
        model.addAttribute("content", "admin/dashboard");
        return "layout/admin-layout";
    }

    @GetMapping("/dashboard/data")
    @ResponseBody
    public Map<String, Object> getDashboardData(
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            @RequestParam(required = false, defaultValue = "week") String topType
    ) {
        LocalDate today = LocalDate.now();
        LocalDate chartFrom = parseDateOrDefault(fromDate, today.minusDays(6));
        LocalDate chartTo = parseDateOrDefault(toDate, today);
        if (chartFrom.isAfter(chartTo)) {
            LocalDate tmp = chartFrom;
            chartFrom = chartTo;
            chartTo = tmp;
        }
        // Avoid extremely heavy responses and chart rendering.
        long days = java.time.temporal.ChronoUnit.DAYS.between(chartFrom, chartTo) + 1;
        if (days > 90) {
            chartFrom = chartTo.minusDays(89);
        }

        String normalizedTopType = (topType != null && "month".equalsIgnoreCase(topType.trim()))
                ? "month"
                : "week";

        String cacheKey = normalizedTopType + ":" + chartFrom + ":" + chartTo;
        CacheEntry cached = DASHBOARD_CACHE.get(cacheKey);
        if (cached != null && cached.isValid()) {
            return cached.data;
        }

        LocalDate endDate = chartTo;

        // Practical dashboard standard:
        // - KPI values are scoped to selected range [chartFrom..chartTo]
        // - Compare to previous period with same length (period-over-period)
        // - Additionally provide day-over-day at range end (endDate vs endDate-1)
        LocalDate prevDay = endDate.minusDays(1);

        long rangeDays = java.time.temporal.ChronoUnit.DAYS.between(chartFrom, chartTo) + 1;
        String revenueScopeText = rangeDays == 7
                ? "Trong 7 ngày"
                : (rangeDays == 30 ? "Trong 30 ngày" : "Theo khoảng thời gian");

        LocalDate prevPeriodTo = chartFrom.minusDays(1);
        LocalDate prevPeriodFrom = prevPeriodTo.minusDays(rangeDays - 1);

        // KPI values in current range
        long customersInRange = 0L;

        // KPI comparisons (yesterday + week-before)
        long customersEndDay = 0L;
        long customersPrevDay = 0L;
        long customersPrevPeriod = 0L;

        long ordersEndDay = 0L;
        long ordersPrevDay = 0L;
        long ordersPrevPeriod = 0L;

        long canceledOrdersLast7Days = 0L;

        BigDecimal revenueInRange = BigDecimal.ZERO;
        BigDecimal revenueEndDay = BigDecimal.ZERO;
        BigDecimal revenuePrevDay = BigDecimal.ZERO;
        BigDecimal revenuePrevPeriod = BigDecimal.ZERO;

        // Donut and line chart for chart range
        Map<LocalDate, BigDecimal> dailyRevenueMap = new LinkedHashMap<>();
        for (LocalDate d = chartFrom; !d.isAfter(chartTo); d = d.plusDays(1)) {
            dailyRevenueMap.put(d, BigDecimal.ZERO);
        }

        long orderProcessingCount = 0L;
        long orderDeliveredCount = 0L;
        long orderCanceledCount = 0L;

        // Top products window
        LocalDate topFrom = "month".equals(normalizedTopType) ? today.minusDays(29) : today.minusDays(6);
        LocalDate topTo = today;

        // productName -> {quantity, revenue}
        class ProductAgg {
            private long quantity = 0L;
            private BigDecimal revenue = BigDecimal.ZERO;
        }
        Map<String, ProductAgg> productAggMap = new HashMap<>();

        try {
            // Customers
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

            // Orders & items
            List<com.example.watchaura.dto.HoaDonDTO> orders = hoaDonService.getAll();
            for (com.example.watchaura.dto.HoaDonDTO order : orders) {
                if (order == null || order.getNgayDat() == null) continue;
                LocalDate orderDate = order.getNgayDat().toLocalDate();
                String status = order.getTrangThaiDonHang();
                boolean isDelivered = "DA_GIAO".equals(status);
                boolean isCanceled = "DA_HUY".equals(status);

                BigDecimal paid = order.getTongTienThanhToan() != null ? order.getTongTienThanhToan() : BigDecimal.ZERO;

                // KPI order counts (all statuses)
                if (orderDate.equals(endDate)) ordersEndDay++;
                if (orderDate.equals(prevDay)) ordersPrevDay++;
                if (!orderDate.isBefore(prevPeriodFrom) && !orderDate.isAfter(prevPeriodTo)) ordersPrevPeriod++;

                // cancel insight in last 7 days (relative to endDate)
                LocalDate last7From = endDate.minusDays(6);
                if (isCanceled && !orderDate.isBefore(last7From) && !orderDate.isAfter(endDate)) {
                    canceledOrdersLast7Days++;
                }

                // KPI revenue (delivered)
                if (isDelivered) {
                    if (orderDate.equals(endDate)) revenueEndDay = revenueEndDay.add(paid);
                    if (orderDate.equals(prevDay)) revenuePrevDay = revenuePrevDay.add(paid);
                    if (!orderDate.isBefore(prevPeriodFrom) && !orderDate.isAfter(prevPeriodTo)) revenuePrevPeriod = revenuePrevPeriod.add(paid);
                }

                // Donut + daily revenue for chart range
                if (!orderDate.isBefore(chartFrom) && !orderDate.isAfter(chartTo)) {
                    if (isDelivered) {
                        orderDeliveredCount++;
                        revenueInRange = revenueInRange.add(paid);
                        BigDecimal current = dailyRevenueMap.get(orderDate);
                        if (current == null) current = BigDecimal.ZERO;
                        dailyRevenueMap.put(orderDate, current.add(paid));
                    } else if (isCanceled) {
                        orderCanceledCount++;
                    } else {
                        orderProcessingCount++;
                    }
                }

                // Top products by delivered order items
                if (isDelivered && !orderDate.isBefore(topFrom) && !orderDate.isAfter(topTo)) {
                    if (order.getItems() == null) continue;
                    for (com.example.watchaura.dto.HoaDonChiTietDTO item : order.getItems()) {
                        if (item == null) continue;
                        String productName = item.getTenSanPham() != null ? item.getTenSanPham() : "Không rõ";
                        long qty = item.getSoLuong() != null ? item.getSoLuong() : 0L;
                        BigDecimal revenue = item.getThanhTien() != null ? item.getThanhTien() : BigDecimal.ZERO;
                        ProductAgg agg = productAggMap.get(productName);
                        if (agg == null) {
                            agg = new ProductAgg();
                            productAggMap.put(productName, agg);
                        }
                        agg.quantity += qty;
                        agg.revenue = agg.revenue.add(revenue);
                    }
                }
            }
        } catch (Exception ignored) {
            // keep defaults
        }

        // daily revenue series + max/min highlight
        BigDecimal maxVal = null;
        BigDecimal minVal = null;
        LocalDate maxDate = null;
        LocalDate minDate = null;

        List<Map<String, Object>> revenueDaily = new ArrayList<>();
        for (Map.Entry<LocalDate, BigDecimal> e : dailyRevenueMap.entrySet()) {
            LocalDate d = e.getKey();
            BigDecimal v = e.getValue() != null ? e.getValue() : BigDecimal.ZERO;
            double dv = v.doubleValue();
            String label = d.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM"));

            revenueDaily.add(Map.of(
                    "date", d.toString(),
                    "label", label,
                    "value", dv
            ));

            if (maxVal == null || v.compareTo(maxVal) > 0) {
                maxVal = v;
                maxDate = d;
            }
            if (minVal == null || v.compareTo(minVal) < 0) {
                minVal = v;
                minDate = d;
            }
        }

        Map<String, Object> revenueMax = maxDate != null ? Map.of(
                "date", maxDate.toString(),
                "label", maxDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")),
                "value", maxVal != null ? maxVal.doubleValue() : 0d
        ) : null;
        Map<String, Object> revenueMin = minDate != null ? Map.of(
                "date", minDate.toString(),
                "label", minDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")),
                "value", minVal != null ? minVal.doubleValue() : 0d
        ) : null;

        // donut
        long totalOrdersRange = orderProcessingCount + orderDeliveredCount + orderCanceledCount;
        Map<String, Object> orderStatus = Map.of(
                "totalOrders", totalOrdersRange,
                "processingCount", orderProcessingCount,
                "deliveredCount", orderDeliveredCount,
                "canceledCount", orderCanceledCount
        );

        BigDecimal totalRevenueTop = productAggMap.values().stream()
                .map(agg -> agg.revenue != null ? agg.revenue : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // top products: sort by quantity then revenue
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
                .collect(java.util.stream.Collectors.toList());

        // Insights + warnings (structured with tone)
        // tone: "good" | "danger" | "info"
        List<Map<String, Object>> warnings = new ArrayList<>();
        List<Map<String, Object>> insights = new ArrayList<>();

        // ---- Insight: revenue max day vs average ----
        BigDecimal revenueAvgDaily = BigDecimal.ZERO;
        int nonZeroDays = 0;
        BigDecimal sumNonZero = BigDecimal.ZERO;
        for (BigDecimal v : dailyRevenueMap.values()) {
            BigDecimal safeV = v != null ? v : BigDecimal.ZERO;
            if (safeV.compareTo(BigDecimal.ZERO) > 0) {
                sumNonZero = sumNonZero.add(safeV);
                nonZeroDays++;
            }
        }
        if (nonZeroDays > 0) {
            revenueAvgDaily = sumNonZero.divide(BigDecimal.valueOf(nonZeroDays), 2, java.math.RoundingMode.HALF_UP);
        }

        if (revenueInRange.compareTo(BigDecimal.ZERO) == 0 || maxVal == null || maxVal.compareTo(BigDecimal.ZERO) == 0) {
            warnings.add(item("🔴 Doanh thu đã giao = <strong>0đ</strong> trong khoảng thời gian đã chọn.", "danger"));
        } else if (revenueAvgDaily.compareTo(BigDecimal.ZERO) > 0 && maxVal.compareTo(BigDecimal.ZERO) > 0) {
            double pct = maxVal.subtract(revenueAvgDaily).doubleValue() * 100.0d / revenueAvgDaily.doubleValue();
            String sign = pct >= 0 ? "+" : "-";
            String pctTxt = sign + String.format(java.util.Locale.US, "%.0f", Math.abs(pct)) + "%";
            String dayTxt = maxDate != null
                    ? maxDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM"))
                    : "--";
            insights.add(item(
                    "📈 " + dayTxt + " đạt <strong>" + formatMoneyInsight(maxVal) + "</strong> (<strong>" + pctTxt + "</strong> vs trung bình)",
                    pct >= 0 ? "good" : "danger"
            ));
        } else {
            insights.add(item(
                    "📈 Ngày doanh thu cao nhất: " + (maxDate != null
                            ? maxDate.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM"))
                            : "--") + " (<strong>" + (maxVal != null ? formatMoneyInsight(maxVal) : "0") + "</strong>)",
                    "info"
            ));
        }

        // ---- Insight: canceled orders in last 7 days ----
        if (canceledOrdersLast7Days == 0) {
            insights.add(item("Không có đơn bị hủy trong 7 ngày gần nhất.", "good"));
        } else {
            insights.add(item(
                    "7 ngày gần nhất: <strong>" + canceledOrdersLast7Days + "</strong> đơn hủy",
                    "danger"
            ));
        }

        // ---- Insight: best seller top product ----
        String bestSellerName = !topProducts.isEmpty() ? (String) topProducts.get(0).get("name") : null;
        if (bestSellerName != null) {
            long bestQty = ((Number) topProducts.get(0).get("quantity")).longValue();
            double bestRev = ((Number) topProducts.get(0).get("revenue")).doubleValue();
            double bestShare = ((Number) topProducts.get(0).getOrDefault("sharePercent", 0d)).doubleValue();
            BigDecimal bestRevBD = BigDecimal.valueOf(bestRev);
            insights.add(item(
                    "🏆 <strong>" + bestSellerName + "</strong> đạt <strong>" + bestQty + "</strong> bán, doanh thu <strong>" +
                            formatMoneyInsight(bestRevBD) + "</strong> (đóng góp <strong>" + String.format(java.util.Locale.US, "%.1f", bestShare) + "%</strong>)",
                    "good"
            ));
        }

        // ---- Warning: 3 consecutive days revenue=0 (alert5) ----
        int zeroStreak = 0;
        LocalDate streakStart = null;
        LocalDate streakEnd = null;
        for (Map.Entry<LocalDate, BigDecimal> e : dailyRevenueMap.entrySet()) {
            BigDecimal v = e.getValue() != null ? e.getValue() : BigDecimal.ZERO;
            if (v.compareTo(BigDecimal.ZERO) == 0) {
                zeroStreak++;
                if (zeroStreak == 1) {
                    streakStart = e.getKey();
                }
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
                    "🔴 3 ngày liên tiếp doanh thu đã giao = <strong>0đ</strong> (" +
                            streakStart.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")) + " - " +
                            streakEnd.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM")) + ").",
                    "danger"
            ));
        }

        // ---- Warning: cancel rate high in selected range ----
        double cancelRate = totalOrdersRange == 0 ? 0d : (orderCanceledCount * 100.0d / totalOrdersRange);
        if (cancelRate >= 20d && totalOrdersRange > 0) {
            warnings.add(item(
                    "🔴 Tỉ lệ hủy đơn cao: <strong>" + String.format(java.util.Locale.US, "%.1f", cancelRate) + "%</strong>.",
                    "danger"
            ));
        }

        // ---- Warning: revenue low abnormal vs previous week ----
        // (kept) low abnormal vs previous period of same length
        if (revenuePrevPeriod.compareTo(BigDecimal.ZERO) > 0
                && revenueInRange.compareTo(revenuePrevPeriod.multiply(new BigDecimal("0.5"))) < 0) {
            Double pct = percentChangeBDSpec(revenueInRange, revenuePrevPeriod);
            String pctTxt = pct == null ? "" : (" (" + (pct >= 0 ? "+" : "-") + String.format(java.util.Locale.US, "%.0f", Math.abs(pct)) + "%)");
            warnings.add(item(
                    "🔴 Doanh thu giảm mạnh vs kỳ trước: <strong>" + formatMoneyInsight(revenueInRange) +
                            "</strong> (kỳ trước: <strong>" + formatMoneyInsight(revenuePrevPeriod) + "</strong>)" + pctTxt,
                    "danger"
            ));
        }

        if (warnings.isEmpty()) {
            warnings.add(item("Không phát hiện bất thường trong giai đoạn này.", "good"));
        }

        // ---- KPI % changes (N/A khi KPI hiện tại = 0) ----
        // Real-world standard percent handling:
        // - previous == null => null
        // - previous == 0 and current > 0 => null (display "Mới" instead of misleading 100% or ∞)
        // - previous == 0 and current == 0 => 0
        Double customersDayPct = percentChangeLongPractical(customersEndDay, customersPrevDay);
        Double customersPeriodPct = percentChangeLongPractical(customersInRange, customersPrevPeriod);

        Double ordersDayPct = percentChangeLongPractical(ordersEndDay, ordersPrevDay);
        Double ordersPeriodPct = percentChangeLongPractical(totalOrdersRange, ordersPrevPeriod);

        Double revenueDayPct = percentChangeBDPractical(revenueEndDay, revenuePrevDay);
        Double revenuePeriodPct = percentChangeBDPractical(revenueInRange, revenuePrevPeriod);

        long totalProducts = 0L;
        try {
            totalProducts = sanPhamService.getAllSanPham().size();
        } catch (Exception ignored) {
        }

        Map<String, Object> kpi = new HashMap<>();
        Map<String, Object> customersMap = new HashMap<>();
        customersMap.put("value", customersInRange);
        customersMap.put("dayCompare", compareBlock(
                customersEndDay,
                customersPrevDay,
                customersDayPct,
                "So với hôm trước"
        ));
        customersMap.put("periodCompare", compareBlock(
                customersInRange,
                customersPrevPeriod,
                customersPeriodPct,
                "So với kỳ trước"
        ));
        kpi.put("customers", customersMap);

        Map<String, Object> ordersMap = new HashMap<>();
        ordersMap.put("value", totalOrdersRange);
        ordersMap.put("dayCompare", compareBlock(
                ordersEndDay,
                ordersPrevDay,
                ordersDayPct,
                "So với hôm trước"
        ));
        ordersMap.put("periodCompare", compareBlock(
                totalOrdersRange,
                ordersPrevPeriod,
                ordersPeriodPct,
                "So với kỳ trước"
        ));
        kpi.put("orders", ordersMap);

        Map<String, Object> productsMap = new HashMap<>();
        productsMap.put("value", totalProducts);
        kpi.put("products", productsMap);

        Map<String, Object> revenueMap = new HashMap<>();
        revenueMap.put("value", revenueInRange.doubleValue());
        revenueMap.put("dayCompare", compareBlockMoney(
                revenueEndDay,
                revenuePrevDay,
                revenueDayPct,
                "So với hôm trước"
        ));
        revenueMap.put("periodCompare", compareBlockMoney(
                revenueInRange,
                revenuePrevPeriod,
                revenuePeriodPct,
                "So với kỳ trước"
        ));
        kpi.put("revenue", revenueMap);

        Map<String, Object> response = new HashMap<>();
        response.put("chartFrom", chartFrom.toString());
        response.put("chartTo", chartTo.toString());
        response.put("prevPeriodFrom", prevPeriodFrom.toString());
        response.put("prevPeriodTo", prevPeriodTo.toString());
        response.put("revenueScopeText", revenueScopeText);
        response.put("kpi", kpi);
        response.put("revenueDaily", revenueDaily);
        response.put("revenueMax", revenueMax);
        response.put("revenueMin", revenueMin);
        response.put("orderStatus", orderStatus);
        response.put("topProducts", topProducts);
        response.put("insights", insights);
        response.put("warnings", warnings);

        CacheEntry entry = new CacheEntry(System.currentTimeMillis(), response);
        DASHBOARD_CACHE.put(cacheKey, entry);
        return response;
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
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            if (current.compareTo(BigDecimal.ZERO) == 0) return 0d;
            return 100d;
        }
        return current.subtract(previous).doubleValue() * 100.0d / previous.doubleValue();
    }

    private static Double percentChangeLongPractical(long current, long previous) {
        // Practical dashboards avoid showing misleading 100%/∞ when previous=0 and current>0.
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

    /**
     * Định dạng tiền ngắn cho insight để tránh hiển thị kiểu "... .00đ".
     * Ví dụ: 1152000000 -> 1.15B, 1520000 -> 1.52M, 120000 -> 120K.
     */
    private static String formatMoneyInsight(BigDecimal value) {
        if (value == null) return "0";
        BigDecimal v = value.stripTrailingZeros();
        BigDecimal abs = v.abs();

        BigDecimal billion = new BigDecimal("1000000000");
        BigDecimal million = new BigDecimal("1000000");
        BigDecimal thousand = new BigDecimal("1000");

        java.math.RoundingMode rm = java.math.RoundingMode.HALF_UP;

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

        java.text.NumberFormat nf = java.text.NumberFormat.getInstance(new java.util.Locale("vi", "VN"));
        nf.setGroupingUsed(true);
        nf.setMaximumFractionDigits(0);
        return nf.format(v) + "đ";
    }

    @GetMapping("/cai-dat")
    public String getCaiDatPage(
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String error,
            Model model) {
        model.addAttribute("title", "Cài đặt");
        model.addAttribute("content", "admin/cai-dat");
        model.addAttribute("settings", SETTINGS_STORE.isEmpty() ? null : new HashMap<>(SETTINGS_STORE));
        if (message != null) model.addAttribute("message", message);
        if (error != null) model.addAttribute("error", error);
        return "layout/admin-layout";
    }

    @PostMapping("/cai-dat/thong-tin")
    public String saveThongTin(
            @RequestParam(required = false) String tenCuaHang,
            @RequestParam(required = false) String slogan,
            @RequestParam(required = false) String diaChi,
            @RequestParam(required = false) String hotline,
            @RequestParam(required = false) String email,
            RedirectAttributes redirectAttributes) {
        if (tenCuaHang != null) SETTINGS_STORE.put("site.name", tenCuaHang);
        if (slogan != null) SETTINGS_STORE.put("site.slogan", slogan);
        if (diaChi != null) SETTINGS_STORE.put("site.address", diaChi);
        if (hotline != null) SETTINGS_STORE.put("site.hotline", hotline);
        if (email != null) SETTINGS_STORE.put("site.email", email);
        redirectAttributes.addAttribute("message", "Đã lưu thông tin cửa hàng.");
        return "redirect:/admin/cai-dat";
    }

    @PostMapping("/cai-dat/cau-hinh")
    public String saveCauHinh(
            @RequestParam(required = false) String donViTienTe,
            @RequestParam(required = false) String pageSize,
            @RequestParam(required = false) String timezone,
            RedirectAttributes redirectAttributes) {
        if (donViTienTe != null) SETTINGS_STORE.put("currency", donViTienTe);
        if (pageSize != null) SETTINGS_STORE.put("page.size", pageSize);
        if (timezone != null) SETTINGS_STORE.put("timezone", timezone);
        redirectAttributes.addAttribute("message", "Đã lưu cấu hình chung.");
        return "redirect:/admin/cai-dat";
    }

    @PostMapping("/cai-dat/email")
    public String saveEmail(
            @RequestParam(required = false) String emailXacNhanDon,
            @RequestParam(required = false) String emailFrom,
            RedirectAttributes redirectAttributes) {
        if (emailXacNhanDon != null) SETTINGS_STORE.put("email.orderConfirm", emailXacNhanDon);
        if (emailFrom != null) SETTINGS_STORE.put("email.from", emailFrom);
        redirectAttributes.addAttribute("message", "Đã lưu cấu hình email.");
        return "redirect:/admin/cai-dat";
    }

    @PostMapping("/cai-dat/bao-mat")
    public String saveBaoMat(
            @RequestParam(required = false) String minPasswordLength,
            @RequestParam(required = false) String maxLoginAttempts,
            RedirectAttributes redirectAttributes) {
        if (minPasswordLength != null) SETTINGS_STORE.put("security.minPasswordLength", minPasswordLength);
        if (maxLoginAttempts != null) SETTINGS_STORE.put("security.maxLoginAttempts", maxLoginAttempts);
        redirectAttributes.addAttribute("message", "Đã lưu cấu hình bảo mật.");
        return "redirect:/admin/cai-dat";
    }


}


