(function () {
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
})();
