(function () {
  var sidebar = document.querySelector('.sidebar');
  var toggle = document.getElementById('sidebarToggle');
  if (toggle && sidebar) {
    toggle.addEventListener('click', function () {
      sidebar.classList.toggle('is-open');
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
