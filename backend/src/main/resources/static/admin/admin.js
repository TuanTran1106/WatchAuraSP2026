(function () {
  /* Theme sáng/tối - lưu vào localStorage */
  var THEME_KEY = 'adminTheme';
  /** '1' = đóng panel, '0' = mở (desktop); mobile mặc định đóng nếu không có key */
  var SIDEBAR_COLLAPSED_KEY = 'adminSidebarCollapsed';
  var layout = document.querySelector('.admin-layout');
  var themeToggle = document.getElementById('adminThemeToggle');

  function getStoredTheme() {
    try {
      return localStorage.getItem(THEME_KEY) || 'light';
    } catch (e) {
      return 'light';
    }
  }

  function applyTheme(theme) {
    if (!layout) return;
    if (theme === 'dark') {
      layout.classList.add('theme-dark');
    } else {
      layout.classList.remove('theme-dark');
    }
  }

  function setTheme(theme) {
    try {
      localStorage.setItem(THEME_KEY, theme);
    } catch (e) {}
    applyTheme(theme);
  }

  if (layout) {
    applyTheme(getStoredTheme());
    if (themeToggle) {
      themeToggle.addEventListener('click', function () {
        var next = layout.classList.contains('theme-dark') ? 'light' : 'dark';
        setTheme(next);
      });
    }
  }

  /* Sidebar: desktop mặc định mở; mobile mặc định đóng + overlay khi mở */
  var sidebar = document.querySelector('.sidebar');
  var toggle = document.getElementById('sidebarToggle');

  function isMobileSidebar() {
    return window.matchMedia('(max-width: 768px)').matches;
  }

  function createSidebarOverlay() {
    var overlay = document.createElement('div');
    overlay.className = 'sidebar__overlay';
    overlay.setAttribute('aria-hidden', 'true');
    document.body.appendChild(overlay);
    return overlay;
  }

  var sidebarOverlay = document.querySelector('.sidebar__overlay') || createSidebarOverlay();

  function isSidebarVisible() {
    if (!sidebar) return false;
    if (isMobileSidebar()) {
      return sidebar.classList.contains('is-open');
    }
    return !sidebar.classList.contains('is-collapsed');
  }

  function setCollapsedPersist(collapsed) {
    try {
      localStorage.setItem(SIDEBAR_COLLAPSED_KEY, collapsed ? '1' : '0');
    } catch (e) {}
  }

  function openSidebar() {
    if (!sidebar) return;
    sidebar.classList.remove('is-collapsed');
    if (isMobileSidebar()) {
      sidebar.classList.add('is-open');
      sidebarOverlay.classList.add('is-active');
    } else {
      sidebar.classList.remove('is-open');
      sidebarOverlay.classList.remove('is-active');
    }
    setCollapsedPersist(false);
    if (toggle) toggle.setAttribute('aria-expanded', 'true');
    window.dispatchEvent(new CustomEvent('sidebarToggle', { detail: { collapsed: false } }));
  }

  function closeSidebar() {
    if (!sidebar) return;
    sidebar.classList.add('is-collapsed');
    sidebar.classList.remove('is-open');
    sidebarOverlay.classList.remove('is-active');
    setCollapsedPersist(true);
    if (toggle) toggle.setAttribute('aria-expanded', 'false');
    window.dispatchEvent(new CustomEvent('sidebarToggle', { detail: { collapsed: true } }));
  }

  function toggleSidebar() {
    if (isSidebarVisible()) {
      closeSidebar();
    } else {
      openSidebar();
    }
  }

  if (toggle && sidebar) {
    toggle.setAttribute('aria-controls', 'adminSidebarNav');
    toggle.setAttribute('aria-expanded', isSidebarVisible() ? 'true' : 'false');
    toggle.addEventListener('click', function (e) {
      e.preventDefault();
      toggleSidebar();
    });
  }

  sidebarOverlay.addEventListener('click', function () {
    if (isMobileSidebar()) closeSidebar();
  });

  (function restoreSidebarState() {
    try {
      var stored = localStorage.getItem(SIDEBAR_COLLAPSED_KEY);
      /* migrate từ key cũ adminSidebarOpen */
      if (stored === null) {
        var legacy = localStorage.getItem('adminSidebarOpen');
        if (legacy === 'false') stored = '1';
        else if (legacy === 'true') stored = '0';
      }
      if (stored === '1') {
        closeSidebar();
      } else if (stored === '0') {
        openSidebar();
      }
    } catch (e) {}
  })();

  window.addEventListener(
    'resize',
    function () {
      if (!sidebar) return;
      if (!isMobileSidebar()) {
        sidebarOverlay.classList.remove('is-active');
        sidebar.classList.remove('is-open');
      }
    },
    { passive: true }
  );

  /* Sidebar dropdown: Sản phẩm & Danh mục - giữ mở khi đang ở trang con */
  var productDropdown = document.getElementById('sidebarProductDropdown');
  var productTrigger = document.getElementById('sidebarProductTrigger');
  if (productTrigger && productDropdown) {
    var productPaths = [
      '/admin/san-pham',
      '/admin/danh-muc',
      '/admin/thuong-hieu',
      '/admin/mau-sac',
      '/admin/kich-thuoc',
      '/admin/loai-may',
      '/admin/chat-lieu-day'
    ];
    var pathname = window.location.pathname.replace(/\/$/, '');
    var isProductPage = productPaths.some(function (p) {
      return pathname === p || pathname.indexOf(p + '/') === 0;
    });
    if (isProductPage) {
      productDropdown.classList.add('is-open');
      productTrigger.setAttribute('aria-expanded', 'true');
    }
    productTrigger.addEventListener('click', function () {
      productDropdown.classList.toggle('is-open');
      var expanded = productDropdown.classList.contains('is-open');
      productTrigger.setAttribute('aria-expanded', expanded);
    });
  }

  /* -------- Core AJAX loader cho admin -------- */
  var adminContent = document.getElementById('adminContent');

  function showAdminLoading() {
    if (!adminContent) return;
    adminContent.classList.add('is-loading');
  }

  function hideAdminLoading() {
    if (!adminContent) return;
    adminContent.classList.remove('is-loading');
  }

  function showAdminError(message) {
    if (!adminContent) return;
    var box = document.createElement('div');
    box.className = 'message message--error';
    box.textContent = message || 'Đã xảy ra lỗi. Vui lòng thử lại.';
    adminContent.insertBefore(box, adminContent.firstChild || null);
  }

  function showAdminToast(message, type) {
    var t = type || 'success';
    var toast = document.createElement('div');
    toast.className = 'toast toast--' + t;
    toast.setAttribute('role', 'status');
    toast.textContent = message || '';
    document.body.appendChild(toast);
    setTimeout(function () {
      toast.classList.add('toast--leaving');
      setTimeout(function () {
        if (toast.parentNode) toast.parentNode.removeChild(toast);
      }, 300);
    }, 4000);
  }

  // Expose to window for use in other scripts
  window.showAdminToast = showAdminToast;

  function drainAdminToastPayload(container) {
    if (!container) return;
    var payload = container.querySelector('[data-admin-toast-message]');
    if (!payload) return;
    var msg = payload.getAttribute('data-admin-toast-message');
    var toastType = payload.getAttribute('data-admin-toast-type') || 'success';
    if (msg) showAdminToast(msg, toastType);
    payload.remove();
  }

  function replaceAdminContent(html) {
    if (!adminContent) return;
    adminContent.innerHTML = html;
    var toastPayload = adminContent.querySelector('[data-admin-toast-message]');
    var hadSuccessToast =
      toastPayload &&
      (toastPayload.getAttribute('data-admin-toast-type') || 'success') === 'success';
    drainAdminToastPayload(adminContent);

    // Form "them" bi thu (is-collapsed) khi id null — neu co loi validation, can mo de thay .form__error.
    // Sau luu thanh cong (toast success), mo de tiep tuc nhap. Khong mo khi chi tim kiem GET (khong toast, khong loi).
    var formConfigs = [
      { wrapper: '#formKhachHangWrapper', openBar: '#formKhachHangOpenBar' },
      { wrapper: '#formBlogWrapper', openBar: '#formBlogOpenBar' },
      { wrapper: '#formKhuyenMaiWrapper', openBar: '#formKhuyenMaiOpenBar' },
      { wrapper: '#formVoucherWrapper', openBar: '#formVoucherOpenBar' }
    ];

    formConfigs.forEach(function(cfg) {
      var formWrapper = adminContent.querySelector(cfg.wrapper);
      if (!formWrapper || !formWrapper.classList.contains('is-collapsed')) return;
      var hasErrors = formWrapper.querySelector('.form__error') !== null;
      if (!hasErrors && !hadSuccessToast) return;
      formWrapper.classList.remove('is-collapsed');
      var openBar = adminContent.querySelector(cfg.openBar);
      if (openBar) openBar.setAttribute('aria-hidden', 'true');
    });
  }

  function loadAdminContent(url, options) {
    if (!url) return;
    options = options || {};
    var fetchOptions = {
      method: options.method || 'GET',
      headers: options.headers || {}
    };

    fetchOptions.headers['X-Requested-With'] = 'XMLHttpRequest';

    if (options.body) {
      fetchOptions.body = options.body;
    }

    showAdminLoading();

    return fetch(url, fetchOptions)
      .then(function (res) {
        if (!res.ok) {
          throw new Error('Request failed with status ' + res.status);
        }
        return res.text();
      })
      .then(function (html) {
        replaceAdminContent(html);
      })
      .catch(function () {
        showAdminError('Không thể tải dữ liệu. Vui lòng thử lại.');
      })
      .finally(function () {
        hideAdminLoading();
      });
  }

  /* -------- Event delegation cho form & link AJAX -------- */
  function isAjaxForm(form) {
    return (
      form &&
      (form.getAttribute('data-ajax') === 'true' ||
        form.classList.contains('js-admin-ajax-form'))
    );
  }

  function isAjaxLink(el) {
    return (
      el &&
      el.tagName === 'A' &&
      (el.getAttribute('data-ajax') === 'true' ||
        el.classList.contains('js-admin-ajax-link'))
    );
  }

  /* -------- Confirm helper (integrates confirm-modal) -------- */
  function getConfirmOpts(form) {
    if (!form || !form.getAttribute) return null;
    var msg = form.getAttribute('data-confirm');
    if (!msg) return null;
    return {
      title: form.getAttribute('data-confirm-title') || 'Xác nhận',
      message: msg,
      confirmText: form.getAttribute('data-confirm-ok') || 'Xác nhận',
      cancelText: form.getAttribute('data-confirm-cancel') || 'Hủy',
      tone: form.getAttribute('data-confirm-tone') || 'info'
    };
  }

  /* -------- Submit handler (AJAX + confirm) -------- */
  function handleAdminSubmit(e) {
    var form = e.target;
    if (!isAjaxForm(form)) return;
    if (!adminContent) return;

    e.preventDefault();

    // Confirm modal flow
    if (form.__confirmPending) return;
    var confirmOpts = getConfirmOpts(form);
    if (confirmOpts) {
      if (!window.confirmModal) {
        console.warn('[admin.js] confirm-modal.js not loaded');
        return;
      }
      form.__confirmPending = true;
      window.confirmModal(confirmOpts).then(function (ok) {
        form.__confirmPending = false;
        if (!ok) return;
        doAdminSubmit(form);
      });
    } else {
      doAdminSubmit(form);
    }
  }

  function doAdminSubmit(form) {
    var method = (form.getAttribute('method') || 'GET').toUpperCase();
    var action = form.getAttribute('action') || window.location.pathname;
    if (method === 'GET') {
      var params = new URLSearchParams(new FormData(form)).toString();
      var url = action + (action.indexOf('?') === -1 ? '?' : '&') + params;
      loadAdminContent(url, { method: 'GET' });
    } else {
      loadAdminContent(action, { method: method, body: new FormData(form) });
    }
  }

  document.addEventListener('submit', handleAdminSubmit);

  /* -------- Click handler for AJAX links (with confirm) -------- */
  function handleAdminClick(e) {
    var target = e.target;
    while (target && target !== document) {
      if (isAjaxLink(target)) break;
      target = target.parentElement;
    }
    if (!target || target === document) return;
    if (!adminContent) return;

    var confirmOpts = getConfirmOpts(target);
    if (confirmOpts) {
      e.preventDefault();
      if (!window.confirmModal) {
        console.warn('[admin.js] confirm-modal.js not loaded');
        return;
      }
      var href = target.getAttribute('href');
      window.confirmModal(confirmOpts).then(function (ok) {
        if (!ok) return;
        loadAdminContent(href, { method: 'GET' });
      });
    } else {
      e.preventDefault();
      var href = target.getAttribute('href');
      if (!href) return;
      loadAdminContent(href, { method: 'GET' });
    }
  }

  document.addEventListener('click', handleAdminClick);

  /* -------- Form card đóng/mở: delegation (DOM thay sau AJAX vẫn hoạt động) -------- */
  document.addEventListener('click', function (e) {
    if (!adminContent || !adminContent.contains(e.target)) return;
    var t = e.target;
    if (!t.closest) return;

    if (t.closest('#formKhachHangToggle')) {
      var khW = adminContent.querySelector('#formKhachHangWrapper');
      var khBar = adminContent.querySelector('#formKhachHangOpenBar');
      if (khW) khW.classList.add('is-collapsed');
      if (khBar) khBar.setAttribute('aria-hidden', 'false');
      return;
    }
    if (t.closest('#formKhachHangOpenBtn')) {
      var khW2 = adminContent.querySelector('#formKhachHangWrapper');
      var khBar2 = adminContent.querySelector('#formKhachHangOpenBar');
      if (khW2) khW2.classList.remove('is-collapsed');
      if (khBar2) khBar2.setAttribute('aria-hidden', 'true');
      return;
    }

    if (t.closest('#formKhuyenMaiToggle')) {
      var kmW = adminContent.querySelector('#formKhuyenMaiWrapper');
      var kmBar = adminContent.querySelector('#formKhuyenMaiOpenBar');
      if (kmW) kmW.classList.add('is-collapsed');
      if (kmBar) kmBar.setAttribute('aria-hidden', 'false');
      return;
    }
    if (t.closest('#formKhuyenMaiOpenBtn')) {
      var kmW2 = adminContent.querySelector('#formKhuyenMaiWrapper');
      var kmBar2 = adminContent.querySelector('#formKhuyenMaiOpenBar');
      if (kmW2) kmW2.classList.remove('is-collapsed');
      if (kmBar2) kmBar2.setAttribute('aria-hidden', 'true');
      return;
    }

    if (t.closest('#formVoucherToggle')) {
      var vW = adminContent.querySelector('#formVoucherWrapper');
      var vBar = adminContent.querySelector('#formVoucherOpenBar');
      if (vW) vW.classList.add('is-collapsed');
      if (vBar) vBar.setAttribute('aria-hidden', 'false');
      return;
    }
    if (t.closest('#formVoucherOpenBtn')) {
      var vW2 = adminContent.querySelector('#formVoucherWrapper');
      var vBar2 = adminContent.querySelector('#formVoucherOpenBar');
      if (vW2) vW2.classList.remove('is-collapsed');
      if (vBar2) vBar2.setAttribute('aria-hidden', 'true');
      return;
    }

    if (t.closest('#formBlogToggle')) {
      var bW = adminContent.querySelector('#formBlogWrapper');
      var bBar = adminContent.querySelector('#formBlogOpenBar');
      if (bW) bW.classList.add('is-collapsed');
      if (bBar) bBar.setAttribute('aria-hidden', 'false');
      return;
    }
    if (t.closest('#formBlogOpenBtn')) {
      var bW2 = adminContent.querySelector('#formBlogWrapper');
      var bBar2 = adminContent.querySelector('#formBlogOpenBar');
      if (bW2) bW2.classList.remove('is-collapsed');
      if (bBar2) bBar2.setAttribute('aria-hidden', 'true');
      return;
    }
  });

  document.addEventListener('DOMContentLoaded', function () {
    drainAdminToastPayload(adminContent);
  });
})();
