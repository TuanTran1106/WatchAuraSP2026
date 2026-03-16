(function () {
  /* Theme sáng/tối - lưu vào localStorage */
  var THEME_KEY = 'adminTheme';
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

  var sidebar = document.querySelector('.sidebar');
  var toggle = document.getElementById('sidebarToggle');
  if (toggle && sidebar) {
    toggle.addEventListener('click', function () {
      sidebar.classList.toggle('is-open');
    });
  }

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

  function replaceAdminContent(html) {
    if (!adminContent) return;
    adminContent.innerHTML = html;
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

  document.addEventListener('submit', function (e) {
    var form = e.target;
    if (!isAjaxForm(form)) {
      return;
    }

    if (!adminContent) {
      return;
    }

    e.preventDefault();

    var method = (form.getAttribute('method') || 'GET').toUpperCase();
    var action = form.getAttribute('action') || window.location.pathname;

    if (method === 'GET') {
      var params = new URLSearchParams(new FormData(form)).toString();
      var url = action + (action.indexOf('?') === -1 ? '?' : '&') + params;
      loadAdminContent(url, { method: 'GET' });
    } else {
      var formData = new FormData(form);
      loadAdminContent(action, {
        method: method,
        body: formData
      });
    }
  });

  document.addEventListener('click', function (e) {
    var target = e.target;
    while (target && target !== document) {
      if (isAjaxLink(target)) {
        break;
      }
      target = target.parentElement;
    }

    if (!target || target === document) {
      return;
    }

    if (!adminContent) {
      return;
    }

    e.preventDefault();

    var href = target.getAttribute('href');
    if (!href) {
      return;
    }

    loadAdminContent(href, { method: 'GET' });
  });

  /* -------- Toggler form hiện có (giữ logic cũ) -------- */
  var formWrapper = document.getElementById('formKhachHangWrapper');
  var formCloseBtn = document.getElementById('formKhachHangToggle');
  var formOpenBtn = document.getElementById('formKhachHangOpenBtn');
  if (formWrapper && formCloseBtn) {
    formCloseBtn.addEventListener('click', function () {
      formWrapper.classList.add('is-collapsed');
      var bar = document.getElementById('formKhachHangOpenBar');
      if (bar) bar.setAttribute('aria-hidden', 'false');
    });
  }
  if (formWrapper && formOpenBtn) {
    formOpenBtn.addEventListener('click', function () {
      formWrapper.classList.remove('is-collapsed');
      var bar = document.getElementById('formKhachHangOpenBar');
      if (bar) bar.setAttribute('aria-hidden', 'true');
    });
  }

  var formKmWrapper = document.getElementById('formKhuyenMaiWrapper');
  var formKmCloseBtn = document.getElementById('formKhuyenMaiToggle');
  var formKmOpenBtn = document.getElementById('formKhuyenMaiOpenBtn');
  if (formKmWrapper && formKmCloseBtn) {
    formKmCloseBtn.addEventListener('click', function () {
      formKmWrapper.classList.add('is-collapsed');
      var bar = document.getElementById('formKhuyenMaiOpenBar');
      if (bar) bar.setAttribute('aria-hidden', 'false');
    });
  }
  if (formKmWrapper && formKmOpenBtn) {
    formKmOpenBtn.addEventListener('click', function () {
      formKmWrapper.classList.remove('is-collapsed');
      var bar = document.getElementById('formKhuyenMaiOpenBar');
      if (bar) bar.setAttribute('aria-hidden', 'true');
    });
  }

  var formVoucherWrapper = document.getElementById('formVoucherWrapper');
  var formVoucherCloseBtn = document.getElementById('formVoucherToggle');
  var formVoucherOpenBtn = document.getElementById('formVoucherOpenBtn');
  if (formVoucherWrapper && formVoucherCloseBtn) {
    formVoucherCloseBtn.addEventListener('click', function () {
      formVoucherWrapper.classList.add('is-collapsed');
      var bar = document.getElementById('formVoucherOpenBar');
      if (bar) bar.setAttribute('aria-hidden', 'false');
    });
  }
  if (formVoucherWrapper && formVoucherOpenBtn) {
    formVoucherOpenBtn.addEventListener('click', function () {
      formVoucherWrapper.classList.remove('is-collapsed');
      var bar = document.getElementById('formVoucherOpenBar');
      if (bar) bar.setAttribute('aria-hidden', 'true');
    });
  }

  var formBlogWrapper = document.getElementById('formBlogWrapper');
  var formBlogCloseBtn = document.getElementById('formBlogToggle');
  var formBlogOpenBtn = document.getElementById('formBlogOpenBtn');
  if (formBlogWrapper && formBlogCloseBtn) {
    formBlogCloseBtn.addEventListener('click', function () {
      formBlogWrapper.classList.add('is-collapsed');
      var bar = document.getElementById('formBlogOpenBar');
      if (bar) bar.setAttribute('aria-hidden', 'false');
    });
  }
  if (formBlogWrapper && formBlogOpenBtn) {
    formBlogOpenBtn.addEventListener('click', function () {
      formBlogWrapper.classList.remove('is-collapsed');
      var bar = document.getElementById('formBlogOpenBar');
      if (bar) bar.setAttribute('aria-hidden', 'true');
    });
  }
})();
