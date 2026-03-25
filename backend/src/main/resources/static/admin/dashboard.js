/* Admin dashboard - line chart + donut + top products
   - Uses Chart.js from CDN
   - Fetches JSON from backend endpoint: /admin/dashboard/data
   - Keeps UI responsive and shows skeleton while loading
*/
(function () {
  function $(id) {
    return document.getElementById(id);
  }

  function hasDashboardRoot() {
    return (
      $('dashRevenueChart') &&
      $('dashOrderStatusChart') &&
      $('dashTopProductsList')
    );
  }

  function toISODate(d) {
    // yyyy-MM-dd in local time
    var year = d.getFullYear();
    var month = String(d.getMonth() + 1).padStart(2, '0');
    var day = String(d.getDate()).padStart(2, '0');
    return year + '-' + month + '-' + day;
  }

  function parseISODate(input) {
    // input expected: yyyy-MM-dd
    if (!input) return null;
    var parts = input.split('-');
    if (parts.length !== 3) return null;
    var y = parseInt(parts[0], 10);
    var m = parseInt(parts[1], 10) - 1;
    var d = parseInt(parts[2], 10);
    if (isNaN(y) || isNaN(m) || isNaN(d)) return null;
    var dt = new Date(y, m, d);
    return isNaN(dt.getTime()) ? null : dt;
  }

  function formatMoneyVND(value) {
    var n = value;
    if (n === null || n === undefined) n = 0;
    if (typeof n === 'string') n = parseFloat(n);
    if (isNaN(n)) n = 0;
    // Vi-VN thousands separator with no decimals + append VND symbol.
    var intPart = Math.round(n); // dashboard values are sums; no need for decimals
    return intPart.toLocaleString('vi-VN') + '₫';
  }

  function formatCompactNumber(value) {
    var n = value;
    if (n === null || n === undefined) n = 0;
    if (typeof n === 'string') n = parseFloat(n);
    if (isNaN(n)) n = 0;
    return n.toLocaleString('vi-VN');
  }

  function formatPercent(value) {
    var n = value;
    if (n === null || n === undefined) n = 0;
    if (typeof n === 'string') n = parseFloat(n);
    if (isNaN(n)) n = 0;
    return (Math.round(n * 10) / 10).toFixed(1).replace('.0', '.0') + '%';
  }

  function applyTrendToEl(elValue, elTrend, percent) {
    if (!elValue || !elTrend) return;
    // backend trả null để biểu thị N/A thay vì -100% hay +∞
    if (percent === null || percent === undefined) {
      elValue.textContent = '--';
      elValue.classList.remove('is-good', 'is-danger', 'is-na');
      elValue.classList.add('is-na');
      elTrend.textContent = '—';
      elTrend.style.color = '#64748b';
      return;
    }

    var n = percent;
    if (typeof n === 'string') n = parseFloat(n);
    if (isNaN(n)) {
      elValue.textContent = '--';
      elValue.classList.remove('is-good', 'is-danger', 'is-na');
      elValue.classList.add('is-na');
      elTrend.textContent = '—';
      elTrend.style.color = '#64748b';
      return;
    }

    var rounded = Math.round(n * 10) / 10;
    var sign = rounded > 0 ? '+' : '';
    elValue.textContent = sign + rounded.toFixed(1) + '%';

    elValue.classList.remove('is-good', 'is-danger', 'is-na');
    if (n > 0.01) {
      elValue.classList.add('is-good');
      elTrend.textContent = '↑';
      elTrend.style.color = '#16a34a';
    } else if (n < -0.01) {
      elValue.classList.add('is-danger');
      elTrend.textContent = '↓';
      elTrend.style.color = '#dc2626';
    } else {
      elValue.classList.add('is-na');
      elTrend.textContent = '→';
      elTrend.style.color = '#64748b';
    }
  }

  function setTextIfExists(id, text) {
    var el = $(id);
    if (!el) return;
    el.textContent = text;
  }

  function setHtmlIfExists(id, html) {
    var el = $(id);
    if (!el) return;
    el.innerHTML = html;
  }

  var revenueChart = null;
  var orderStatusChart = null;
  var revenueSparklineChart = null;
  var lastKpiNumeric = {
    customers: null,
    orders: null,
    products: null,
    revenue: null
  };

  function animateCountUp(el, from, to, durationMs, formatter) {
    if (!el) return;
    var start = typeof from === 'number' ? from : 0;
    var end = typeof to === 'number' ? to : 0;
    var delta = end - start;
    var duration = durationMs || 600;
    var startTime = null;

    function step(timestamp) {
      if (!startTime) startTime = timestamp;
      var elapsed = timestamp - startTime;
      var progress = Math.min(1, elapsed / duration);
      var current = start + delta * progress;
      el.textContent = formatter(current);
      if (progress < 1) {
        requestAnimationFrame(step);
      }
    }

    requestAnimationFrame(step);
  }

  function setDashLoading(isLoading) {
    var revenueSkel = $('dashRevenueChartSkeleton');
    var orderSkel = $('dashOrderStatusChartSkeleton');
    var topSkel = $('dashTopProductsSkeleton');
    var wrapRevenue = $('dashRevenueChartWrap');
    var wrapOrder = $('dashOrderStatusChartWrap');
    var wrapTop = $('dashTopProductsWrap');

    [wrapRevenue, wrapOrder, wrapTop].forEach(function (w) {
      if (!w) return;
      if (isLoading) w.classList.add('is-loading');
      else w.classList.remove('is-loading');
    });

    if (revenueSkel) revenueSkel.style.display = isLoading ? 'block' : 'none';
    if (orderSkel) orderSkel.style.display = isLoading ? 'block' : 'none';
    if (topSkel) topSkel.style.display = isLoading ? 'block' : 'none';

    var insightsList = $('dashInsightsList');
    var warningsList = $('dashWarningsList');
    if (insightsList && warningsList) {
      if (isLoading) {
        insightsList.innerHTML = '<li class="dashboard-muted">Đang tải gợi ý...</li>';
        warningsList.innerHTML = '<li class="dashboard-muted">Đang tải cảnh báo...</li>';
      }
    }
  }

  function updateRangeInputState() {
    var rangeSel = $('dashRangeSelect');
    var fromInput = $('dashFromDate');
    var toInput = $('dashToDate');
    if (!rangeSel || !fromInput || !toInput) return;
    var custom = rangeSel.value === 'custom';
    fromInput.disabled = !custom;
    toInput.disabled = !custom;
    if (custom) {
      // Keep current values, but ensure min logic is reasonable.
      if (!fromInput.value || !toInput.value) {
        var today = new Date();
        var from = new Date(today.getTime() - 6 * 86400000);
        fromInput.value = toISODate(from);
        toInput.value = toISODate(today);
      }
    }
  }

  function buildRequestParams() {
    var rangeSel = $('dashRangeSelect');
    var fromInput = $('dashFromDate');
    var toInput = $('dashToDate');
    var topSel = $('dashTopRangeSelect');

    var topType = topSel ? topSel.value : 'week';

    var today = new Date();
    var from = null;
    var to = null;

    if (!rangeSel || rangeSel.value === '7') {
      from = new Date(today.getTime() - 6 * 86400000);
      to = today;
    } else if (rangeSel.value === '30') {
      from = new Date(today.getTime() - 29 * 86400000);
      to = today;
    } else {
      // custom
      var f = parseISODate(fromInput ? fromInput.value : null);
      var t = parseISODate(toInput ? toInput.value : null);
      from = f || new Date(today.getTime() - 6 * 86400000);
      to = t || today;
    }

    // Ensure order from <= to
    if (from && to && from.getTime() > to.getTime()) {
      var tmp = from;
      from = to;
      to = tmp;
    }

    // Keep request payload reasonable (backend also clamps).
    // Limit to 90 days for custom ranges.
    try {
      var diffDays = Math.floor((to.getTime() - from.getTime()) / 86400000) + 1;
      if (diffDays > 90) {
        from = new Date(to.getTime() - (90 - 1) * 86400000);
      }
    } catch (e) {}

    return {
      fromDate: toISODate(from),
      toDate: toISODate(to),
      topType: topType
    };
  }

  function renderInsights(insights, warnings) {
    var listEl = $('dashInsightsList');
    var warnEl = $('dashWarningsList');
    if (!listEl || !warnEl) return;

    var safeTone = function (tone) {
      if (!tone) return 'info';
      tone = String(tone).toLowerCase();
      return tone === 'good' || tone === 'danger' || tone === 'info' ? tone : 'info';
    };

    if (!insights || insights.length === 0) {
      listEl.innerHTML = '<li class="dashboard-muted">Chưa có gợi ý.</li>';
    } else {
      listEl.innerHTML = insights
        .map(function (it) {
          var text = it && it.text ? it.text : '';
          var tone = safeTone(it && it.tone);
          var icon =
            tone === 'good' ? 'fa-check-circle' :
              (tone === 'danger' ? 'fa-triangle-exclamation' : 'fa-lightbulb');
          var color =
            tone === 'good' ? '#16a34a' :
              (tone === 'danger' ? '#dc2626' : '#1d4ed8');
          return '<li class="dash-insight dash-insight--' + tone + '">' +
            '<i class="fas ' + icon + '" aria-hidden="true" style="color:' + color + ';"></i>' +
            '<span class="dash-note-text">' + text + '</span>' +
            '</li>';
        })
        .join('');
    }

    if (!warnings || warnings.length === 0) {
      warnEl.innerHTML = '<li class="dashboard-muted">Tình trạng ổn định trong giai đoạn này.</li>';
    } else {
      warnEl.innerHTML = warnings
        .map(function (it) {
          var text = it && it.text ? it.text : '';
          var tone = safeTone(it && it.tone);
          var icon =
            tone === 'danger' ? 'fa-triangle-exclamation' :
              (tone === 'good' ? 'fa-check-circle' : 'fa-info-circle');
          var color =
            tone === 'danger' ? '#dc2626' :
              (tone === 'good' ? '#16a34a' : '#1d4ed8');
          var scrollTarget = 'dashRevenueChartWrap';
          if (String(text).indexOf('hủy') !== -1 || String(text).indexOf('hủy đơn') !== -1) {
            scrollTarget = 'dashOrderStatusChartWrap';
          }
          var cta =
            tone === 'danger'
              ? '<button type="button" class="dash-warning-cta" data-cta-target="' + scrollTarget + '">Xem chi tiết</button>'
              : '';

          return '<li class="dash-warning dash-warning--' + tone + '">' +
            '<i class="fas ' + icon + '" aria-hidden="true" style="color:' + color + ';"></i>' +
            '<span class="dash-note-text">' + text + '</span>' +
            cta +
            '</li>';
        })
        .join('');
    }

    // CTA click: scroll to related block
    warnEl.onclick = function (e) {
      var target = e.target;
      if (!target || !target.matches || !target.matches('.dash-warning-cta')) return;
      var targetId = target.getAttribute('data-cta-target');
      if (!targetId) return;
      var el = $(targetId);
      if (!el) return;
      el.classList.remove('dash-focus-pulse');
      // Force reflow
      el.offsetHeight;
      el.classList.add('dash-focus-pulse');
      setTimeout(function () {
        el.classList.remove('dash-focus-pulse');
      }, 950);
      el.scrollIntoView({ behavior: 'smooth', block: 'start' });
    };
  }

  function renderKpi(payload) {
    var kpi = payload && payload.kpi ? payload.kpi : null;
    if (!kpi) return;

    setTextIfExists('dashKpiRevenueScope', payload && payload.revenueScopeText ? payload.revenueScopeText : '—');

    var customersVal = kpi.customers && kpi.customers.value != null ? Number(kpi.customers.value) : 0;
    var ordersVal = kpi.orders && kpi.orders.value != null ? Number(kpi.orders.value) : 0;
    var productsVal = kpi.products && kpi.products.value != null ? Number(kpi.products.value) : 0;
    var revenueVal = kpi.revenue && kpi.revenue.value != null ? Number(kpi.revenue.value) : 0;

    animateCountUp($('dashKpiCustomers'), lastKpiNumeric.customers === null ? 0 : lastKpiNumeric.customers, customersVal, 500, function (n) {
      return formatCompactNumber(Math.round(n));
    });
    lastKpiNumeric.customers = customersVal;
    applyTrendToEl($('dashKpiCustomersYesterday'), $('dashKpiCustomersYesterdayTrend'), kpi.customers && kpi.customers.yesterdayChangePercent);
    applyTrendToEl($('dashKpiCustomersWeek'), $('dashKpiCustomersWeekTrend'), kpi.customers && kpi.customers.weekChangePercent);

    animateCountUp($('dashKpiOrders'), lastKpiNumeric.orders === null ? 0 : lastKpiNumeric.orders, ordersVal, 500, function (n) {
      return formatCompactNumber(Math.round(n));
    });
    lastKpiNumeric.orders = ordersVal;
    applyTrendToEl($('dashKpiOrdersYesterday'), $('dashKpiOrdersYesterdayTrend'), kpi.orders && kpi.orders.yesterdayChangePercent);
    applyTrendToEl($('dashKpiOrdersWeek'), $('dashKpiOrdersWeekTrend'), kpi.orders && kpi.orders.weekChangePercent);

    animateCountUp($('dashKpiProducts'), lastKpiNumeric.products === null ? 0 : lastKpiNumeric.products, productsVal, 500, function (n) {
      return formatCompactNumber(Math.round(n));
    });
    lastKpiNumeric.products = productsVal;

    var revenueFrom = lastKpiNumeric.revenue === null ? 0 : lastKpiNumeric.revenue;
    animateCountUp($('dashKpiRevenue'), revenueFrom, revenueVal, 600, function (n) {
      return formatMoneyVND(n);
    });
    lastKpiNumeric.revenue = revenueVal;

    applyTrendToEl($('dashKpiRevenueYesterday'), $('dashKpiRevenueYesterdayTrend'), kpi.revenue && kpi.revenue.yesterdayChangePercent);
    applyTrendToEl($('dashKpiRevenueWeek'), $('dashKpiRevenueWeekTrend'), kpi.revenue && kpi.revenue.weekChangePercent);
  }

  function renderRevenueSparkline(payload) {
    var canvas = $('dashKpiRevenueSparkline');
    if (!canvas) return;
    var daily = payload && payload.revenueDaily ? payload.revenueDaily : [];
    if (!daily || daily.length === 0) return;

    var values = daily.map(function (p) { return p.value || 0; });
    var first = values[0] || 0;
    var last = values[values.length - 1] || 0;
    var good = last >= first;
    var color = good ? '#16a34a' : '#dc2626';

    var ctx = canvas.getContext('2d');
    var labels = daily.map(function (p) { return p.label; });

    function formatMoneyVNDCompactLocal(v) {
      var n = Number(v) || 0;
      function trimZeros(str) {
        return String(str).replace(/\.?0+$/, '').replace(/(\.\d*[1-9])0+$/, '$1');
      }
      if (n >= 1e9) return trimZeros((n / 1e9).toFixed(2)) + 'B₫';
      if (n >= 1e6) return trimZeros((n / 1e6).toFixed(2)) + 'M₫';
      if (n >= 1e3) return trimZeros((n / 1e3).toFixed(1)) + 'K₫';
      return Math.round(n).toLocaleString('vi-VN') + '₫';
    }

    if (revenueSparklineChart) {
      revenueSparklineChart.data.labels = labels;
      revenueSparklineChart.data.datasets[0].data = values;
      revenueSparklineChart.data.datasets[0].borderColor = color;
      revenueSparklineChart.update();
      return;
    }

    revenueSparklineChart = new Chart(ctx, {
      type: 'line',
      data: {
        labels: labels,
        datasets: [
          {
            data: values,
            borderColor: color,
            backgroundColor: 'rgba(0,0,0,0)',
            borderWidth: 2,
            tension: 0.35,
            fill: false,
            pointRadius: 0
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: {
            callbacks: {
              label: function (ctx) {
                return ' ' + formatMoneyVNDCompactLocal(ctx.parsed.y || 0);
              }
            }
          }
        },
        scales: {
          x: { display: false },
          y: { display: false }
        },
        interaction: { mode: 'index', intersect: false }
      }
    });
  }

  function renderRevenueChart(payload) {
    var daily = payload && payload.revenueDaily ? payload.revenueDaily : [];
    var max = payload && payload.revenueMax ? payload.revenueMax : null;
    var min = payload && payload.revenueMin ? payload.revenueMin : null;

    var labels = daily.map(function (p) { return p.label; });
    var values = daily.map(function (p) { return p.value || 0; });

    var maxIndex = -1;
    var minIndex = -1;
    for (var i = 0; i < daily.length; i++) {
      if (max && daily[i].date === max.date) maxIndex = i;
      if (min && daily[i].date === min.date) minIndex = i;
    }

    if (max && min) {
      $('dashRevenueMaxLabel').textContent = max.label + ' (' + formatYAxisCompact(max.value) + ')';
      $('dashRevenueMinLabel').textContent = min.label + ' (' + formatYAxisCompact(min.value) + ')';
    } else {
      $('dashRevenueMaxLabel').textContent = '-';
      $('dashRevenueMinLabel').textContent = '-';
    }

    var ctx = $('dashRevenueChart').getContext('2d');

    var pointBg = values.map(function (v, idx) {
      if (idx === maxIndex) return '#f59e0b';
      if (idx === minIndex) return '#ef4444';
      return '#0ea5e9';
    });

    var pointRadius = values.map(function (v, idx) {
      if (idx === maxIndex || idx === minIndex) return 6;
      return 3;
    });

    function formatYAxisCompact(v) {
      var n = Number(v) || 0;
      function trimZeros(str) {
        return String(str).replace(/\.?0+$/, '').replace(/(\.\d*[1-9])0+$/, '$1');
      }
      if (n >= 1e9) return trimZeros((n / 1e9).toFixed(2)) + 'B';
      if (n >= 1e6) return trimZeros((n / 1e6).toFixed(2)) + 'M';
      if (n >= 1e3) return trimZeros((n / 1e3).toFixed(1)) + 'K';
      return Math.round(n).toLocaleString('vi-VN');
    }

    function formatMoneyVNDCompact(v) {
      var n = Number(v) || 0;
      function trimZeros(str) {
        return String(str).replace(/\.?0+$/, '').replace(/(\.\d*[1-9])0+$/, '$1');
      }
      if (n >= 1e9) return trimZeros((n / 1e9).toFixed(2)) + 'B₫';
      if (n >= 1e6) return trimZeros((n / 1e6).toFixed(2)) + 'M₫';
      if (n >= 1e3) return trimZeros((n / 1e3).toFixed(1)) + 'K₫';
      return Math.round(n).toLocaleString('vi-VN') + '₫';
    }

    var dashHighlightPlugin = {
      id: 'dashHighlightPlugin',
      afterDatasetsDraw: function (chart) {
        if (!chart || !chart.$dashHighlight) return;
        var isDarkTheme = !!chart.$dashIsDark;
        var h = chart.$dashHighlight;
        var meta = chart.getDatasetMeta(0);
        if (!meta || !meta.data) return;

        var ctx = chart.ctx;
        ctx.save();
        ctx.font = '600 12px Segoe UI, system-ui, -apple-system, sans-serif';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'bottom';

        function drawLabel(index, text) {
          if (index === null || index === undefined) return;
          if (index < 0 || index >= meta.data.length) return;
          var p = meta.data[index];
          if (!p) return;
          var x = p.x;
          var y = p.y - 10;

          var paddingX = 8;
          var paddingY = 4;
          var metrics = ctx.measureText(text);
          var w = metrics.width + paddingX * 2;
          var h = 18 + paddingY;
          var boxY = y - h + 2;
          // Background box for readability
          ctx.fillStyle = isDarkTheme ? 'rgba(15,23,42,0.85)' : 'rgba(255,255,255,0.92)';
          ctx.strokeStyle = isDarkTheme ? 'rgba(148,163,184,0.25)' : 'rgba(148,163,184,0.25)';
          ctx.lineWidth = 1;
          ctx.beginPath();
          if (typeof ctx.roundRect === 'function') {
            ctx.roundRect(x - w / 2, boxY, w, h, 8);
          } else {
            ctx.rect(x - w / 2, boxY, w, h);
          }
          ctx.fill();
          ctx.stroke();
          ctx.fillStyle = isDarkTheme ? '#e2e8f0' : '#0f172a';
          ctx.fillText(text, x, y);
        }

        if (h.maxIndex !== null && h.maxLabel) drawLabel(h.maxIndex, h.maxLabel);
        if (h.minIndex !== null && h.minLabel) drawLabel(h.minIndex, h.minLabel);
        ctx.restore();
      }
    };

    if (revenueChart) {
      revenueChart.data.labels = labels;
      revenueChart.data.datasets[0].data = values;
      revenueChart.data.datasets[0].pointBackgroundColor = pointBg;
      revenueChart.data.datasets[0].pointRadius = pointRadius;
      revenueChart.$dashHighlight = {
        maxIndex: maxIndex,
        minIndex: minIndex,
        maxLabel: max && maxIndex >= 0 ? ('Max: ' + formatYAxisCompact(max.value)) : null,
        minLabel: min && minIndex >= 0 ? ('Min: ' + formatYAxisCompact(min.value)) : null
      };
      try {
        var layout = document.querySelector('.admin-layout');
        revenueChart.$dashIsDark = !!(layout && layout.classList.contains('theme-dark'));
      } catch (e) {
        revenueChart.$dashIsDark = false;
      }
      revenueChart.update();
      return;
    }

    revenueChart = new Chart(ctx, {
      type: 'line',
      data: {
        labels: labels,
        datasets: [
          {
            label: 'Doanh thu',
            data: values,
            borderColor: '#1e3a5f',
            backgroundColor: 'rgba(30, 58, 95, 0.1)',
            fill: false,
            tension: 0.15,
            cubicInterpolationMode: 'default',
            borderWidth: 2,
            pointRadius: pointRadius,
            pointBackgroundColor: pointBg,
            pointBorderColor: '#ffffff',
            pointBorderWidth: 2,
            pointHitRadius: 10
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { display: false },
          tooltip: {
            interaction: { mode: 'index', intersect: false },
            callbacks: {
              title: function (items) {
                if (!items || !items.length) return '';
                return items[0].label || '';
              },
              label: function (ctx) {
                var v = ctx.parsed.y || 0;
                return 'Doanh thu: ' + formatMoneyVNDCompact(v);
              }
            }
          }
        },
        interaction: { mode: 'index', intersect: false },
        scales: {
          y: {
            beginAtZero: true,
            ticks: {
              callback: function (value) {
                return formatYAxisCompact(value);
              }
            },
            grid: { color: 'rgba(148,163,184,0.15)' }
          },
          x: {
            grid: { display: false }
          }
        },
        elements: { line: { borderCapStyle: 'round' } }
      }
      ,
      plugins: [dashHighlightPlugin]
    });

    revenueChart.$dashHighlight = {
      maxIndex: maxIndex,
      minIndex: minIndex,
      maxLabel: max && maxIndex >= 0 ? ('Max: ' + formatYAxisCompact(max.value)) : null,
      minLabel: min && minIndex >= 0 ? ('Min: ' + formatYAxisCompact(min.value)) : null
    };
    try {
      var layout2 = document.querySelector('.admin-layout');
      revenueChart.$dashIsDark = !!(layout2 && layout2.classList.contains('theme-dark'));
    } catch (e) {
      revenueChart.$dashIsDark = false;
    }
  }

  function renderOrderStatus(payload) {
    var os = payload && payload.orderStatus ? payload.orderStatus : null;
    if (!os) return;

    setTextIfExists('dashOrderTotal', formatCompactNumber(os.totalOrders));
    setTextIfExists('dashOrderProcessingCount', formatCompactNumber(os.processingCount));
    setTextIfExists('dashOrderDeliveredCount', formatCompactNumber(os.deliveredCount));
    setTextIfExists('dashOrderCanceledCount', formatCompactNumber(os.canceledCount));

    var ctx = $('dashOrderStatusChart').getContext('2d');

    var labels = ['Đang xử lý', 'Đã giao', 'Đã hủy'];
    var values = [os.processingCount || 0, os.deliveredCount || 0, os.canceledCount || 0];

    var colors = ['#60a5fa', '#10b981', '#ef4444'];

    var data = values.map(function (v) {
      var total = os.totalOrders || 1;
      return {
        value: v,
        percent: (v * 100) / total
      };
    });

    var tooltipLabels = {
      callbacks: {
        label: function (context) {
          var i = context.dataIndex;
          var row = data[i];
          return ' ' + labels[i] + ': ' + formatCompactNumber(row.value) + ' (' + (Math.round(row.percent * 10) / 10).toFixed(1) + '%)';
        }
      }
    };

    if (orderStatusChart) {
      orderStatusChart.data.datasets[0].data = values;
      orderStatusChart.update();
      return;
    }

    orderStatusChart = new Chart(ctx, {
      type: 'doughnut',
      data: {
        labels: labels,
        datasets: [
          {
            data: values,
            backgroundColor: colors,
            borderColor: '#ffffff',
            borderWidth: 2
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { position: 'bottom' },
          tooltip: tooltipLabels
        },
        cutout: '62%'
      }
    });
  }

  function renderTopProducts(payload) {
    var list = $('dashTopProductsList');
    var empty = $('dashTopProductsEmpty');
    var detail = $('dashTopProductDetail');
    if (!list) return;

    var products = payload && payload.topProducts ? payload.topProducts : [];

    if (!products || products.length === 0) {
      list.innerHTML = '';
      if (empty) empty.style.display = 'block';
      if (detail) detail.style.display = 'none';
      return;
    }
    if (empty) empty.style.display = 'none';

    list.innerHTML = products
      .map(function (p, idx) {
        var name = p.name || 'Sản phẩm';
        var qty = p.quantity || 0;
        var rev = p.revenue || 0;
        var share = p.sharePercent || 0;
        var shareTxt = (typeof share === 'number' ? share : parseFloat(share)) || 0;
        if (isNaN(shareTxt)) shareTxt = 0;

        return (
          '<li data-top-idx="' + idx + '" class="dash-top-product-item">' +
          '<div class="dash-top-product-left">' +
          '<a href="#" class="dash-top-product-link">' +
          '<span class="dash-top-product-rank">#' + (idx + 1) + '</span>' +
          '<span class="dash-top-product-name">' + name + '</span>' +
          '</a>' +
          '<div class="dash-top-product-mini">' +
          '<div class="dash-top-product-mini__fill" style="width:' + shareTxt + '%"></div>' +
          '</div>' +
          '<div class="dash-top-product-share">Đóng góp: <strong>' + shareTxt.toFixed(1) + '%</strong></div>' +
          '</div>' +
          '<div class="dash-top-product-right">' +
          '<div class="dash-top-product-qty">SL: <strong>' + formatCompactNumber(qty) + '</strong></div>' +
          '<div class="dash-top-product-revenue">' + formatMoneyVND(rev) + '</div>' +
          '</div>' +
          '</li>'
        );
      })
      .join('');

    // Click to show detail
    list.onclick = function (e) {
      var target = e.target;
      while (target && target !== list) {
        if (target.matches && target.matches('li[data-top-idx]')) break;
        target = target.parentElement;
      }
      if (!target || target === list) return;

      var idx = parseInt(target.getAttribute('data-top-idx'), 10);
      if (isNaN(idx)) return;

      var p = products[idx];
      if (!p || !detail) return;

      // highlight selected
      var all = list.querySelectorAll('li[data-top-idx]');
      for (var i = 0; i < all.length; i++) {
        all[i].classList.remove('is-active');
      }
      target.classList.add('is-active');

      $('dashTopProductDetailName').textContent = p.name || '-';
      $('dashTopProductDetailQty').textContent = formatCompactNumber(p.quantity || 0);
      $('dashTopProductDetailRevenue').textContent = formatMoneyVND(p.revenue || 0);
      var share = p.sharePercent || 0;
      if (typeof share === 'string') share = parseFloat(share);
      if (isNaN(share)) share = 0;
      var shareEl = $('dashTopProductDetailShare');
      if (shareEl) shareEl.textContent = share.toFixed(1) + '%';
      detail.style.display = 'block';

      // Micro drill-down: focus revenue chart
      var chartWrap = $('dashRevenueChartWrap');
      if (chartWrap) {
        chartWrap.classList.remove('dash-focus-pulse');
        chartWrap.offsetHeight;
        chartWrap.classList.add('dash-focus-pulse');
        setTimeout(function () {
          chartWrap.classList.remove('dash-focus-pulse');
        }, 950);
        chartWrap.scrollIntoView({ behavior: 'smooth', block: 'start' });
      }
    };
  }

  async function fetchDashboardData() {
    var params = buildRequestParams();

    var endpoint = '/admin/dashboard/data'
      + '?fromDate=' + encodeURIComponent(params.fromDate)
      + '&toDate=' + encodeURIComponent(params.toDate)
      + '&topType=' + encodeURIComponent(params.topType);

    // Update export button to current chart range
    var exportBtn = $('dashExportPdfBtn');
    if (exportBtn) {
      // Use existing revenue PDF report endpoint (status defaults to DA_GIAO if omitted).
      exportBtn.href = '/admin/hoa-don/bao-cao/pdf'
        + '?fromDate=' + encodeURIComponent(params.fromDate)
        + '&toDate=' + encodeURIComponent(params.toDate)
        + '&status=DA_GIAO';
    }

    var res = await fetch(endpoint, {
      headers: {
        'X-Requested-With': 'XMLHttpRequest'
      }
    });
    if (!res.ok) {
      throw new Error('Fetch dashboard data failed: ' + res.status);
    }
    return await res.json();
  }

  async function refreshDashboard() {
    setDashLoading(true);
    try {
      var payload = await fetchDashboardData();
      renderKpi(payload);
      renderRevenueSparkline(payload);
      renderRevenueChart(payload);
      renderOrderStatus(payload);
      renderTopProducts(payload);
      renderInsights(payload.insights, payload.warnings);
    } catch (e) {
      // minimal fallback
      console.error(e);
      var container = $('dashTopProductsWrap');
      if (container) container.innerHTML = '<p class="dashboard-muted">Không thể tải dashboard. Vui lòng thử lại.</p>';
    } finally {
      setDashLoading(false);
    }
  }

  function setupKpiDrillDown() {
    function focusAndScroll(targetEl) {
      if (!targetEl) return;
      try {
        targetEl.classList.remove('dash-focus-pulse');
        // Force reflow so animation triggers again.
        // eslint-disable-next-line no-unused-expressions
        targetEl.offsetHeight;
        targetEl.classList.add('dash-focus-pulse');
        setTimeout(function () {
          targetEl.classList.remove('dash-focus-pulse');
        }, 950);
        targetEl.scrollIntoView({ behavior: 'smooth', block: 'start' });
      } catch (e) {
        targetEl.scrollIntoView();
      }
    }

    var revenueCard = document.querySelector('.stat-card--revenue');
    var ordersCard = document.querySelector('.stat-card--orders');
    var customersCard = document.querySelector('.stat-card--customers');

    if (revenueCard) {
      revenueCard.style.cursor = 'pointer';
      revenueCard.addEventListener('click', function () {
        focusAndScroll($('dashRevenueChartWrap'));
      });
    }

    if (ordersCard) {
      ordersCard.style.cursor = 'pointer';
      ordersCard.addEventListener('click', function () {
        focusAndScroll($('dashOrderStatusChartWrap'));
      });
    }

    if (customersCard) {
      customersCard.style.cursor = 'pointer';
      customersCard.addEventListener('click', function () {
        focusAndScroll($('dashInsightsList'));
      });
    }
  }

  function initDashboard() {
    if (!hasDashboardRoot()) return;

    updateRangeInputState();

    var rangeSel = $('dashRangeSelect');
    var fromInput = $('dashFromDate');
    var toInput = $('dashToDate');
    var topSel = $('dashTopRangeSelect');

    // Initialize default date values
    var today = new Date();
    var from = new Date(today.getTime() - 6 * 86400000);
    if (fromInput && toInput) {
      fromInput.value = toISODate(from);
      toInput.value = toISODate(today);
    }

    if (rangeSel) {
      rangeSel.addEventListener('change', function () {
        updateRangeInputState();
        refreshDashboard();
      });
    }
    if (topSel) {
      topSel.addEventListener('change', function () {
        refreshDashboard();
      });
    }
    if (fromInput) {
      fromInput.addEventListener('change', function () {
        refreshDashboard();
      });
    }
    if (toInput) {
      toInput.addEventListener('change', function () {
        refreshDashboard();
      });
    }

    setupKpiDrillDown();

    // first load
    refreshDashboard();
  }

  document.addEventListener('DOMContentLoaded', initDashboard);
})();

