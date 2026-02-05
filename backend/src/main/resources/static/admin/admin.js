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

  var formSanPhamWrapper = document.getElementById('formSanPhamWrapper');
  var formSanPhamCloseBtn = document.getElementById('formSanPhamToggle');
  var formSanPhamOpenBtn = document.getElementById('formSanPhamOpenBtn');
  if (formSanPhamWrapper && formSanPhamCloseBtn) {
    formSanPhamCloseBtn.addEventListener('click', function () {
      formSanPhamWrapper.classList.add('is-collapsed');
      var bar = document.getElementById('formSanPhamOpenBar');
      if (bar) bar.setAttribute('aria-hidden', 'false');
    });
  }
  if (formSanPhamWrapper && formSanPhamOpenBtn) {
    formSanPhamOpenBtn.addEventListener('click', function () {
      formSanPhamWrapper.classList.remove('is-collapsed');
      var bar = document.getElementById('formSanPhamOpenBar');
      if (bar) bar.setAttribute('aria-hidden', 'true');
    });
  }

  var formDanhMucWrapper = document.getElementById('formDanhMucWrapper');
  var formDanhMucCloseBtn = document.getElementById('formDanhMucToggle');
  var formDanhMucOpenBtn = document.getElementById('formDanhMucOpenBtn');
  if (formDanhMucWrapper && formDanhMucCloseBtn) {
    formDanhMucCloseBtn.addEventListener('click', function () {
      formDanhMucWrapper.classList.add('is-collapsed');
      var bar = document.getElementById('formDanhMucOpenBar');
      if (bar) bar.setAttribute('aria-hidden', 'false');
    });
  }
  if (formDanhMucWrapper && formDanhMucOpenBtn) {
    formDanhMucOpenBtn.addEventListener('click', function () {
      formDanhMucWrapper.classList.remove('is-collapsed');
      var bar = document.getElementById('formDanhMucOpenBar');
      if (bar) bar.setAttribute('aria-hidden', 'true');
    });
  }

  var formThuongHieuWrapper = document.getElementById('formThuongHieuWrapper');
  var formThuongHieuCloseBtn = document.getElementById('formThuongHieuToggle');
  var formThuongHieuOpenBtn = document.getElementById('formThuongHieuOpenBtn');
  if (formThuongHieuWrapper && formThuongHieuCloseBtn) {
    formThuongHieuCloseBtn.addEventListener('click', function () {
      formThuongHieuWrapper.classList.add('is-collapsed');
      var bar = document.getElementById('formThuongHieuOpenBar');
      if (bar) bar.setAttribute('aria-hidden', 'false');
    });
  }
  if (formThuongHieuWrapper && formThuongHieuOpenBtn) {
    formThuongHieuOpenBtn.addEventListener('click', function () {
      formThuongHieuWrapper.classList.remove('is-collapsed');
      var bar = document.getElementById('formThuongHieuOpenBar');
      if (bar) bar.setAttribute('aria-hidden', 'true');
    });
  }

  var formKhuyenMaiWrapper = document.getElementById('formKhuyenMaiWrapper');
  var formKhuyenMaiCloseBtn = document.getElementById('formKhuyenMaiToggle');
  var formKhuyenMaiOpenBtn = document.getElementById('formKhuyenMaiOpenBtn');
  if (formKhuyenMaiWrapper && formKhuyenMaiCloseBtn) {
    formKhuyenMaiCloseBtn.addEventListener('click', function () {
      formKhuyenMaiWrapper.classList.add('is-collapsed');
      var bar = document.getElementById('formKhuyenMaiOpenBar');
      if (bar) bar.setAttribute('aria-hidden', 'false');
    });
  }
  if (formKhuyenMaiWrapper && formKhuyenMaiOpenBtn) {
    formKhuyenMaiOpenBtn.addEventListener('click', function () {
      formKhuyenMaiWrapper.classList.remove('is-collapsed');
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

  function bindFormToggle(wrapperId, closeBtnId, openBtnId, openBarId) {
    var wrapper = document.getElementById(wrapperId);
    var closeBtn = document.getElementById(closeBtnId);
    var openBtn = document.getElementById(openBtnId);
    if (wrapper && closeBtn) {
      closeBtn.addEventListener('click', function () {
        wrapper.classList.add('is-collapsed');
        var bar = document.getElementById(openBarId);
        if (bar) bar.setAttribute('aria-hidden', 'false');
      });
    }
    if (wrapper && openBtn) {
      openBtn.addEventListener('click', function () {
        wrapper.classList.remove('is-collapsed');
        var bar = document.getElementById(openBarId);
        if (bar) bar.setAttribute('aria-hidden', 'true');
      });
    }
  }
  bindFormToggle('formMauSacWrapper', 'formMauSacToggle', 'formMauSacOpenBtn', 'formMauSacOpenBar');
  bindFormToggle('formChatLieuDayWrapper', 'formChatLieuDayToggle', 'formChatLieuDayOpenBtn', 'formChatLieuDayOpenBar');
  bindFormToggle('formKichThuocWrapper', 'formKichThuocToggle', 'formKichThuocOpenBtn', 'formKichThuocOpenBar');
  bindFormToggle('formLoaiMayWrapper', 'formLoaiMayToggle', 'formLoaiMayOpenBtn', 'formLoaiMayOpenBar');

})();
