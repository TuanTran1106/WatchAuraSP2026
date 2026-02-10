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
    var productPaths = ['/admin/san-pham', '/admin/danh-muc', '/admin/thuong-hieu', '/admin/mau-sac', '/admin/kich-thuoc', '/admin/loai-may', '/admin/chat-lieu-day'];
    var pathname = window.location.pathname.replace(/\/$/, '');
    var isProductPage = productPaths.some(function (p) { return pathname === p || pathname.indexOf(p + '/') === 0; });
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
