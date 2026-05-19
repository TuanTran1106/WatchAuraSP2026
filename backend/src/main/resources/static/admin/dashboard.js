(function () {
  'use strict';

  var state = window.__watchAuraDashboardState || (window.__watchAuraDashboardState = {});
  var POLL_MS = 5 * 60 * 1000;
  var AUTO_INIT_ATTR = 'data-dashboard-bound';

  function qs(id, root) {
    return (root || document).getElementById(id);
  }
  function qsa(selector, root) {
    return Array.prototype.slice.call((root || document).querySelectorAll(selector));
  }
  function num(v) {
    var n = typeof v === 'string' ? parseFloat(v) : v;
    return isFinite(n) ? n : 0;
  }
  function fmtMoney(v) {
    return Math.round(num(v)).toLocaleString('vi-VN') + '₫';
  }
  function fmtCompact(v) {
    return Math.round(num(v)).toLocaleString('vi-VN');
  }
  function fmtPct(v) {
    return v === null || v === undefined ? '—' : (Math.round(num(v) * 10) / 10).toFixed(1) + '%';
  }
  function root() {
    return document.getElementById('dashboardRoot');
  }
  function isDashboardPage() {
    return !!root();
  }

  function kpiNumericRaw(data) {
    if (!data) return 0;
    if (data.value !== undefined && data.value !== null) return data.value;
    if (data.current !== undefined && data.current !== null) return data.current;
    return 0;
  }

  function badgeClassForPct(pct) {
    if (pct === null || pct === undefined || (typeof pct === 'number' && isNaN(pct))) {
      return 'badge rounded-pill bg-secondary-subtle text-secondary border border-secondary-subtle';
    }
    if (pct > 0) return 'badge rounded-pill bg-success-subtle text-success border border-success-subtle';
    if (pct < 0) return 'badge rounded-pill bg-danger-subtle text-danger border border-danger-subtle';
    return 'badge rounded-pill bg-secondary-subtle text-secondary border border-secondary-subtle';
  }

  function resolveKpiCompare(data) {
    if (!data) return { pct: null, compareText: '—', subtitleExtra: '' };
    if (data.percentChange !== undefined && data.percentChange !== null) {
      return {
        pct: data.percentChange,
        compareText: fmtPct(data.percentChange),
        subtitleExtra: data.compareLabel || ''
      };
    }
    var pc = data.periodCompare;
    if (pc && pc.percentChange !== undefined && pc.percentChange !== null) {
      return {
        pct: pc.percentChange,
        compareText: fmtPct(pc.percentChange),
        subtitleExtra: pc.compareLabel || ''
      };
    }
    return { pct: null, compareText: '—', subtitleExtra: '' };
  }

  function destroyCharts() {
    ['revenue', 'orders', 'topProducts'].forEach(function (k) {
      if (state[k] && typeof state[k].destroy === 'function') state[k].destroy();
      state[k] = null;
    });
  }

  function setLoading(isLoading) {
    qsa('[data-dashboard-skeleton]').forEach(function (el) {
      el.style.display = isLoading ? '' : 'none';
    });
    var r = root();
    if (r) r.classList.toggle('is-loading', !!isLoading);
  }

  function escapeHtml(str) {
    return String(str == null ? '' : str)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;')
      .replace(/'/g, '&#39;');
  }

  function setKpi(cardId, value, compareText, subtitle, pctRaw) {
    var card = qs(cardId);
    if (!card) return;
    var valueEl = card.querySelector('[data-kpi-value]');
    var compareEl = card.querySelector('[data-kpi-compare]');
    var subtitleEl = card.querySelector('[data-kpi-subtitle]');
    if (valueEl) valueEl.textContent = value;
    if (compareEl) {
      compareEl.textContent = compareText || '—';
      compareEl.className = badgeClassForPct(pctRaw) + ' w-fit mb-2 align-self-start';
    }
    if (subtitleEl) subtitleEl.textContent = subtitle || '';
  }

  function setKpiHeading(cardId, text) {
    var card = qs(cardId);
    if (!card || !text) return;
    var h = card.querySelector('[data-kpi-heading]');
    if (h) h.textContent = text;
  }

  /** Ưu tiên % so với kỳ trước (periodCompare) cho badge */
  function resolveKpiComparePeriodFirst(data) {
    if (!data) return { pct: null, compareText: '—', subtitleExtra: '' };
    var pc = data.periodCompare;
    if (pc && pc.percentChange !== undefined && pc.percentChange !== null) {
      return {
        pct: pc.percentChange,
        compareText: fmtPct(pc.percentChange),
        subtitleExtra: pc.compareLabel || 'So với kỳ trước'
      };
    }
    return resolveKpiCompare(data);
  }

  function renderKpis(payload) {
    var kpi = payload && payload.kpi ? payload.kpi : {};
    var primaryLabel = payload && payload.revenuePrimaryLabel ? payload.revenuePrimaryLabel : 'theo kỳ';
    setKpiHeading('dashKpiRevenueToday', 'Doanh thu ' + primaryLabel);
    var scope = payload && payload.revenueScopeText ? payload.revenueScopeText : 'trong kỳ';
    setKpiHeading('dashKpiRevenue', 'Doanh thu ' + scope + ' (thực thu)');

    var rows = [
      ['dashKpiRevenueToday', kpi.revenuePrimary, fmtMoney, false],
      ['dashKpiRevenue', kpi.revenue, fmtMoney, true],
      ['dashKpiOrders', kpi.orders, fmtCompact, true],
      ['dashKpiCustomers', kpi.customers, fmtCompact, true],
      ['dashKpiProducts', kpi.products, fmtCompact, false],
      ['dashKpiLowStock', kpi.lowStock, fmtCompact, false],
      ['dashKpiPromotions', kpi.promotions, fmtCompact, false],
      ['dashKpiVouchers', kpi.vouchers, fmtCompact, false]
    ];
    rows.forEach(function (item) {
      var data = item[1] || {};
      var usePeriod = item[3];
      var cmp = usePeriod ? resolveKpiComparePeriodFirst(data) : resolveKpiCompare(data);
      var subtitle = data.subtitle || cmp.subtitleExtra || '';
      var rawVal = kpiNumericRaw(data);
      setKpi(item[0], item[2](rawVal), cmp.compareText, subtitle, cmp.pct);
    });

    // Revenue method breakdown subtitle
    var revenueCard = qs('dashKpiRevenue');
    if (revenueCard && kpi.revenue && kpi.revenue.methodSubtitle) {
      var methodEl = revenueCard.querySelector('[data-kpi-method]');
      if (methodEl) {
        methodEl.textContent = kpi.revenue.methodSubtitle;
      }
    }
  }

  function renderLowStockList(items, emptyText) {
    var el = qs('dashLowStockList');
    if (!el) return;
    if (!items || !items.length) {
      el.innerHTML = '<div class="text-muted small">' + escapeHtml(emptyText || 'Chưa có dữ liệu.') + '</div>';
      return;
    }
    el.innerHTML =
      '<ul class="list-group list-group-flush">' +
      items
        .map(function (it) {
          var badgeTxt = it.meta || it.subtitle || 'Low';
          return (
            '<li class="list-group-item px-0 d-flex justify-content-between align-items-start border-light">' +
            '<div class="me-2 min-w-0">' +
            '<div class="fw-semibold small text-truncate">' +
            escapeHtml(it.title || '-') +
            '</div>' +
            '<div class="text-muted small text-truncate">' +
            escapeHtml(it.subtitle || '') +
            '</div></div>' +
            '<span class="badge bg-danger rounded-pill flex-shrink-0 ms-2">' +
            escapeHtml(badgeTxt) +
            '</span></li>'
          );
        })
        .join('') +
      '</ul>';
  }

  function fmtActivityDate(meta) {
    if (!meta) return '';
    try {
      var s = String(meta).trim();
      var d = new Date(s);
      if (isNaN(d.getTime())) return s;
      if (s.indexOf('T') >= 0) {
        return d.toLocaleString('vi-VN', {
          day: '2-digit',
          month: 'numeric',
          year: 'numeric',
          hour: '2-digit',
          minute: '2-digit'
        });
      }
      return d.toLocaleDateString('vi-VN');
    } catch (e) {
      return String(meta);
    }
  }

  function renderActivityList(rootId, items, emptyText, iconClass) {
    var el = qs(rootId);
    if (!el) return;
    iconClass = iconClass || 'fa-circle';
    if (!items || !items.length) {
      el.innerHTML = '<div class="text-muted small">' + escapeHtml(emptyText || 'Chưa có dữ liệu.') + '</div>';
      return;
    }
    el.innerHTML = items
      .map(function (it, idx) {
        var last = idx === items.length - 1;
        var borderCls = last ? '' : ' mb-3 pb-3 border-bottom border-light';
        return (
          '<div class="d-flex align-items-start' +
          borderCls +
          '">' +
          '<div class="rounded-circle bg-primary bg-opacity-10 text-primary d-inline-flex align-items-center justify-content-center flex-shrink-0 me-3" style="width:40px;height:40px;">' +
          '<i class="fas ' +
          iconClass +
          ' fa-sm"></i></div>' +
          '<div class="flex-grow-1 min-w-0">' +
          '<div class="fw-semibold small">' +
          escapeHtml(it.title || '-') +
          '</div>' +
          '<div class="text-muted small text-truncate">' +
          escapeHtml(it.subtitle || '') +
          '</div>' +
          '<div class="text-muted small">' +
          escapeHtml(fmtActivityDate(it.meta)) +
          '</div></div></div>'
        );
      })
      .join('');
  }

  function stripLastInsightBorder(ul) {
    if (!ul || !ul.lastElementChild) return;
    ul.lastElementChild.classList.remove('mb-2', 'pb-2', 'border-bottom', 'border-dark', 'border-opacity-10');
  }

  function renderInsights(payload) {
    var goodCard = qs('dashInsightsGoodCard');
    var warnCard = qs('dashInsightsWarnCard');
    var dangerCard = qs('dashInsightsDangerCard');
    var groups = {
      good: qs('dashInsightsGood'),
      warn: qs('dashInsightsWarn'),
      danger: qs('dashInsightsDanger')
    };
    if (groups.good) groups.good.innerHTML = '';
    if (groups.warn) groups.warn.innerHTML = '';
    if (groups.danger) groups.danger.innerHTML = '';
    var items = [].concat((payload && payload.insights) || [], (payload && payload.warnings) || []);
    items.forEach(function (item) {
      var tone = item && item.tone ? item.tone : 'good';
      var target = tone === 'danger' ? groups.danger : tone === 'info' ? groups.warn : groups.good;
      if (!target) return;
      var li = document.createElement('li');
      li.className = 'mb-2 pb-2 border-bottom border-dark border-opacity-10';
      li.innerHTML = item && item.text ? item.text : '';
      target.appendChild(li);
    });
    stripLastInsightBorder(groups.good);
    stripLastInsightBorder(groups.warn);
    stripLastInsightBorder(groups.danger);

    if (goodCard) goodCard.classList.toggle('d-none', !(groups.good && groups.good.children.length));
    if (warnCard) warnCard.classList.toggle('d-none', !(groups.warn && groups.warn.children.length));
    if (dangerCard) dangerCard.classList.toggle('d-none', !(groups.danger && groups.danger.children.length));
  }

  function renderCharts(payload) {
    var revenueCanvas = qs('dashRevenueChart');
    var orderCanvas = qs('dashOrderChart');
    var topCanvas = qs('dashTopProductChart');
    if (!window.Chart || typeof window.Chart !== 'function') {
      console.warn('[dashboard] Chart.js chưa load (window.Chart không có).');
      return;
    }
    if (!revenueCanvas || !orderCanvas || !topCanvas) return;
    destroyCharts();

    var revenueRows = (payload && payload.revenueSeries) || (payload && payload.revenueDaily) || [];

    var chartTitleEl = qs('dashRevenueChartTitle');
    if (chartTitleEl && payload.chartTitle) chartTitleEl.textContent = payload.chartTitle;
    var topRows = (payload && payload.topProducts) || [];
    var orderStatus = payload && payload.orderStatus ? payload.orderStatus : {};

    state.revenue = new window.Chart(revenueCanvas.getContext('2d'), {
      type: 'line',
      data: {
        labels: revenueRows.map(function (x) {
          return x.label;
        }),
        datasets: [
          {
            label: 'Thực thu (thu gộp − hoàn trả)',
            data: revenueRows.map(function (x) {
              return x.value;
            }),
            borderColor: '#2563eb',
            backgroundColor: 'rgba(37,99,235,.10)',
            tension: 0.25,
            fill: true
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } }
      }
    });

    var donutLabels = orderStatus.labels || ['Đang xử lý', 'Đã giao', 'Đã hủy'];
    var donutColors = orderStatus.colors || ['#2563eb', '#22c55e', '#ef4444'];
    state.orders = new window.Chart(orderCanvas.getContext('2d'), {
      type: 'doughnut',
      data: {
        labels: donutLabels,
        datasets: [
          {
            data: [
              orderStatus.processingCount || 0,
              orderStatus.deliveredCount || 0,
              orderStatus.canceledCount || 0
            ],
            backgroundColor: donutColors
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { position: 'bottom' } }
      }
    });

    state.topProducts = new window.Chart(topCanvas.getContext('2d'), {
      type: 'bar',
      data: {
        labels: topRows.map(function (x) {
          var rank = x.rank ? '#' + x.rank + ' ' : '';
          return rank + x.name;
        }),
        datasets: [
          {
            label: 'Doanh thu',
            data: topRows.map(function (x) {
              return x.revenue;
            }),
            backgroundColor: '#0f766e'
          },
          {
            label: 'Số lượng bán',
            data: topRows.map(function (x) {
              return x.quantity;
            }),
            backgroundColor: '#2563eb',
            hidden: true
          }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        indexAxis: 'y',
        plugins: {
          legend: { display: true, position: 'bottom' },
          tooltip: {
            callbacks: {
              afterBody: function (context) {
                var idx = context[0].dataIndex;
                var item = topRows[idx];
                if (!item) return '';
                return 'SL: ' + (item.quantity || 0) + ' · Tỷ trọng: ' + (item.sharePercent ? item.sharePercent.toFixed(1) : '0') + '%';
              }
            }
          }
        }
      }
    });
  }

  function createAborter() {
    if (state.abortController) state.abortController.abort();
    state.abortController = new AbortController();
    return state.abortController;
  }

  function pad2(n) {
    return n < 10 ? '0' + n : '' + n;
  }

  function toYMD(d) {
    return d.getFullYear() + '-' + pad2(d.getMonth() + 1) + '-' + pad2(d.getDate());
  }

  function mondayOfDate(d) {
    var x = new Date(d.getFullYear(), d.getMonth(), d.getDate());
    var diff = (x.getDay() + 6) % 7;
    x.setDate(x.getDate() - diff);
    return x;
  }

  function applyPresetDates() {
    var rangeSel = qs('dashRangeSelect');
    var from = qs('dashFromDate');
    var to = qs('dashToDate');
    if (!rangeSel || !from || !to) return;

    var now = new Date();
    var today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    var v = rangeSel.value;

    if (v === 'custom') {
      from.disabled = false;
      to.disabled = false;
      if (!from.value || !to.value) {
        var s0 = new Date(today);
        s0.setDate(s0.getDate() - 6);
        from.value = toYMD(s0);
        to.value = toYMD(today);
      }
      return;
    }

    from.disabled = true;
    to.disabled = true;

    var start;
    var end = new Date(today);

    if (v === 'today') {
      start = new Date(today);
    } else if (v === 'yesterday') {
      start = new Date(today);
      start.setDate(start.getDate() - 1);
      end = new Date(start);
    } else if (v === 'last7') {
      start = new Date(today);
      start.setDate(start.getDate() - 6);
    } else if (v === 'last30') {
      start = new Date(today);
      start.setDate(start.getDate() - 29);
    } else if (v === 'this_week') {
      start = mondayOfDate(today);
    } else if (v === 'this_month') {
      start = new Date(today.getFullYear(), today.getMonth(), 1);
    } else {
      start = new Date(today);
      start.setDate(start.getDate() - 6);
    }

    from.value = toYMD(start);
    to.value = toYMD(end);
  }

  function buildUrl() {
    var rangeSel = qs('dashRangeSelect');
    var from = qs('dashFromDate');
    var to = qs('dashToDate');
    var params = new URLSearchParams();
    if (rangeSel) params.set('rangePreset', rangeSel.value);
    if (rangeSel && rangeSel.value === 'custom') {
      if (from && from.value) params.set('fromDate', from.value);
      if (to && to.value) params.set('toDate', to.value);
    }
    return '/admin/dashboard/data?' + params.toString();
  }

  function syncDateInputs() {
    applyPresetDates();
  }

  function fetchDashboard() {
    if (!isDashboardPage()) return Promise.resolve();
    state.pendingChartPayload = null;
    setLoading(true);
    var controller = createAborter();
    return fetch(buildUrl(), { signal: controller.signal, headers: { 'X-Requested-With': 'XMLHttpRequest' } })
      .then(function (res) {
        return res.ok ? res.json() : Promise.reject(new Error('Request failed'));
      })
      .then(function (payload) {
        if (!isDashboardPage()) return;
        renderKpis(payload);
        renderLowStockList(payload.lowStock || [], 'Chưa có sản phẩm sắp hết hàng.');
        renderActivityList('dashRecentOrders', payload.recentOrders || [], 'Chưa có đơn hàng.', 'fa-shopping-bag');
        renderActivityList('dashRecentCustomers', payload.recentCustomers || [], 'Chưa có khách hàng mới.', 'fa-user');
        renderActivityList('dashExpiringCampaigns', payload.expiringCampaigns || [], 'Chưa có chương trình sắp hết hạn.', 'fa-tags');
        renderInsights(payload);
        state.pendingChartPayload = payload;
      })
      .catch(function (err) {
        if (err && err.name === 'AbortError') return;
        var r = root();
        if (r && !r.querySelector('.message--error')) {
          r.insertAdjacentHTML(
            'afterbegin',
            '<div class="alert alert-danger m-2" role="alert">Không thể tải dashboard. Vui lòng thử lại.</div>'
          );
        }
      })
      .finally(function () {
        setLoading(false);
        var p = state.pendingChartPayload;
        state.pendingChartPayload = null;
        if (p && isDashboardPage()) {
          requestAnimationFrame(function () {
            renderCharts(p);
          });
        }
      });
  }

  function bind() {
    var r = root();
    if (!r || r.getAttribute(AUTO_INIT_ATTR) === '1') return;
    r.setAttribute(AUTO_INIT_ATTR, '1');

    var rangeSel = qs('dashRangeSelect');
    var from = qs('dashFromDate');
    var to = qs('dashToDate');
    var refreshBtn = qs('dashRefreshBtn');

    if (rangeSel)
      rangeSel.addEventListener('change', function () {
        applyPresetDates();
        fetchDashboard();
      });
    if (from) from.addEventListener('change', fetchDashboard);
    if (to) to.addEventListener('change', fetchDashboard);
    if (refreshBtn) refreshBtn.addEventListener('click', fetchDashboard);

    syncDateInputs();
    fetchDashboard();

    clearInterval(state.timer);
    state.timer = setInterval(fetchDashboard, POLL_MS);
  }

  document.addEventListener('DOMContentLoaded', bind);
  document.addEventListener('dashboard:mount', bind);
  document.addEventListener('admin:content-replaced', bind);
})();
