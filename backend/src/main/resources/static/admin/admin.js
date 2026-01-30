// Toggle sidebar (mobile)
const toggleBtn = document.getElementById("sidebarToggle");
const sidebar = document.querySelector(".sidebar");

if (toggleBtn && sidebar) {
  toggleBtn.addEventListener("click", () => {
    sidebar.classList.toggle("sidebar--open");
  });
}

// Simple "routing" theo sidebar (thay đổi title + content demo)
const navLinks = document.querySelectorAll(".sidebar__link");
const pageTitle = document.getElementById("pageTitle");
const content = document.getElementById("content");

const pages = {
  dashboard: `
    <div class="content__grid">
      <section class="card">
        <h2 class="card__title">Total Users</h2>
        <p class="card__value">1,234</p>
      </section>
      <section class="card">
        <h2 class="card__title">Total Orders</h2>
        <p class="card__value">567</p>
      </section>
      <section class="card">
        <h2 class="card__title">Revenue</h2>
        <p class="card__value">$12,345</p>
      </section>
      <section class="card">
        <h2 class="card__title">Conversion</h2>
        <p class="card__value">4.5%</p>
      </section>
    </div>
    <section class="card card--full">
      <h2 class="card__title">Recent Orders</h2>
      <table class="table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Customer</th>
            <th>Total</th>
            <th>Status</th>
            <th>Date</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>#001</td>
            <td>John Doe</td>
            <td>$120</td>
            <td><span class="badge badge--success">Completed</span></td>
            <td>2026-01-25</td>
          </tr>
          <tr>
            <td>#002</td>
            <td>Jane Smith</td>
            <td>$80</td>
            <td><span class="badge badge--warning">Pending</span></td>
            <td>2026-01-25</td>
          </tr>
        </tbody>
      </table>
    </section>
  `,
  users: `
    <section class="card card--full">
      <div class="card__header-row">
        <div class="card__search-group">
          <input
            type="text"
            id="kh-search"
            class="input input--sm input--search-wide"
            placeholder="Tìm theo mã, tên, email, SĐT..."
          />
          <select id="kh-status-filter" class="input input--sm">
            <option value="all">Tất cả</option>
            <option value="active">Đang hoạt động</option>
            <option value="inactive">Ngưng hoạt động</option>
          </select>
        </div>
        <div class="card__actions">
          <button type="button" class="btn btn--primary btn--sm" id="kh-add-btn">
            +
          </button>
        </div>
      </div>

      <form id="kh-form" class="kh-form kh-form--hidden" data-visible="false">
        <input type="hidden" id="kh-id" />
        <div class="form-grid">
          <div class="form-control">
            <label for="kh-ma">Mã khách hàng</label>
            <input id="kh-ma" type="text" required />
          </div>
          <div class="form-control">
            <label for="kh-ten">Tên khách hàng</label>
            <input id="kh-ten" type="text" required />
          </div>
          <div class="form-control">
            <label for="kh-email">Email</label>
            <input id="kh-email" type="email" />
          </div>
          <div class="form-control">
            <label for="kh-sdt">Số điện thoại</label>
            <input id="kh-sdt" type="text" />
          </div>
          <div class="form-control">
            <label for="kh-matkhau">Mật khẩu</label>
            <input id="kh-matkhau" type="password" required />
          </div>
          <div class="form-control">
            <label for="kh-gioitinh">Giới tính</label>
            <select id="kh-gioitinh">
              <option value="">-- Chọn --</option>
              <option value="Nam">Nam</option>
              <option value="Nữ">Nữ</option>
              <option value="Khác">Khác</option>
            </select>
          </div>
          <div class="form-control">
            <label for="kh-trangthai">Trạng thái</label>
            <select id="kh-trangthai">
              <option value="true">Đang hoạt động</option>
              <option value="false">Ngưng hoạt động</option>
            </select>
          </div>
        </div>
        <div class="kh-form__actions">
          <p class="form-error" id="kh-error" hidden></p>
          <div class="kh-form__buttons">
            <button type="button" class="btn btn--ghost btn--sm" id="kh-reset-btn">Làm mới</button>
            <button type="submit" class="btn btn--primary btn--sm" id="kh-save-btn">Lưu</button>
          </div>
        </div>
      </form>
    </section>

    <section class="card card--full">
      <div class="card__header-row">
        <div>
          <h2 class="card__title">Danh sách khách hàng</h2>
        </div>
      </div>
      <div class="table-wrapper">
        <table class="table">
          <thead>
            <tr>
              <th>Mã</th>
              <th>Tên</th>
              <th>Email</th>
              <th>SĐT</th>
              <th>Giới tính</th>
              <th>Trạng thái</th>
              <th>Thao tác</th>
            </tr>
          </thead>
          <tbody id="kh-table-body">
            <tr>
              <td colspan="7" style="text-align:center; padding: 16px;">Đang tải dữ liệu...</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  `,
  orders: `
    <section class="card card--full">
      <div class="card__header-row">
        <div class="card__search-group">
          <input
            type="text"
            id="hd-search"
            class="input input--sm input--search-wide"
            placeholder="Tìm theo mã đơn, tên khách, SĐT..."
          />
          <select id="hd-status-filter" class="input input--sm">
            <option value="all">Tất cả trạng thái</option>
            <option value="CHO_XAC_NHAN">Chờ xác nhận</option>
            <option value="DANG_XU_LY">Đang xử lý</option>
            <option value="DANG_GIAO">Đang giao</option>
            <option value="DA_GIAO">Đã giao</option>
            <option value="DA_HUY">Đã hủy</option>
          </select>
        </div>
      </div>

      <div class="table-wrapper">
        <table class="table">
          <thead>
            <tr>
              <th>Mã đơn</th>
              <th>Khách hàng</th>
              <th>SĐT</th>
              <th>Tổng thanh toán</th>
              <th>Trạng thái</th>
              <th>Ngày đặt</th>
              <th>Thao tác</th>
            </tr>
          </thead>
          <tbody id="hd-table-body">
            <tr>
              <td colspan="7" style="text-align:center; padding: 16px;">Đang tải dữ liệu...</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <section class="card card--full" id="hd-detail-box">
      <div class="card__header-row">
        <div>
          <h3 class="card__title" id="hd-detail-title">Chi tiết hóa đơn</h3>
        </div>
      </div>
      <div class="hd-detail__body" id="hd-detail-body">
        <p style="font-size:12px; color: var(--text-soft); margin: 0;">
          Chọn một hóa đơn bên trên để xem chi tiết.
        </p>
      </div>
    </section>
  `,
  products: `
    <section class="card card--full">
      <div class="card__header-row">
        <div class="card__search-group">
          <input
            type="text"
            id="sp-search"
            class="input input--sm input--search-wide"
            placeholder="Tìm theo mã, tên sản phẩm..."
          />
          <select id="sp-category-filter" class="input input--sm">
            <option value="all">Tất cả danh mục</option>
          </select>
        </div>
        <div class="card__actions">
          <button type="button" class="btn btn--primary btn--sm" id="sp-add-btn">
            +
          </button>
        </div>
      </div>

      <form id="sp-form" class="sp-form sp-form--hidden" data-visible="false">
        <input type="hidden" id="sp-id" />
        <div class="form-grid">
          <div class="form-control">
            <label for="sp-ma">Mã sản phẩm</label>
            <input id="sp-ma" type="text" required />
          </div>
          <div class="form-control">
            <label for="sp-ten">Tên sản phẩm</label>
            <input id="sp-ten" type="text" required />
          </div>
          <div class="form-control">
            <label for="sp-giaban">Giá bán</label>
            <input id="sp-giaban" type="number" min="0" step="1000" />
          </div>
          <div class="form-control">
            <label for="sp-soluongton">Số lượng tồn</label>
            <input id="sp-soluongton" type="number" min="0" step="1" value="0" />
          </div>
          <div class="form-control">
            <label for="sp-danhmuc">Danh mục</label>
            <select id="sp-danhmuc" required>
              <option value="">-- Chọn danh mục --</option>
            </select>
          </div>
          <div class="form-control">
            <label for="sp-mausac">Màu sắc</label>
            <select id="sp-mausac">
              <option value="">-- Chọn --</option>
            </select>
          </div>
          <div class="form-control">
            <label for="sp-kichthuoc">Kích thước</label>
            <select id="sp-kichthuoc">
              <option value="">-- Chọn --</option>
            </select>
          </div>
          <div class="form-control">
            <label for="sp-chatlieuday">Chất liệu dây</label>
            <select id="sp-chatlieuday">
              <option value="">-- Chọn --</option>
            </select>
          </div>
          <div class="form-control">
            <label for="sp-loaimay">Loại máy</label>
            <select id="sp-loaimay">
              <option value="">-- Chọn --</option>
            </select>
          </div>
          <div class="form-control">
            <label for="sp-phongcach">Phong cách</label>
            <input id="sp-phongcach" type="text" />
          </div>
          <div class="form-control">
            <label for="sp-duongkinh">Đường kính (mm)</label>
            <input id="sp-duongkinh" type="number" min="0" step="0.1" />
          </div>
          <div class="form-control">
            <label for="sp-dochiunuoc">Độ chịu nước (m)</label>
            <input id="sp-dochiunuoc" type="number" min="0" step="1" />
          </div>
          <div class="form-control">
            <label for="sp-berongday">Bề rộng dây (mm)</label>
            <input id="sp-berongday" type="number" min="0" step="0.1" />
          </div>
          <div class="form-control">
            <label for="sp-trongluong">Trọng lượng (g)</label>
            <input id="sp-trongluong" type="number" min="0" step="0.1" />
          </div>
          <div class="form-control">
            <label for="sp-trangthai">Trạng thái</label>
            <select id="sp-trangthai">
              <option value="true">Đang bán</option>
              <option value="false">Ngưng</option>
            </select>
          </div>
          <div class="form-control" style="grid-column: 1 / -1;">
            <label for="sp-mota">Mô tả</label>
            <textarea
              id="sp-mota"
              rows="2"
              style="width: 100%; resize: vertical; background:#020617; border-radius:8px; border:1px solid var(--border-soft); padding:6px 8px; color:var(--text); font-size:13px;"
            ></textarea>
          </div>
        </div>
        <div class="sp-form__actions">
          <p class="form-error" id="sp-error" hidden></p>
          <div class="sp-form__buttons">
            <button type="button" class="btn btn--ghost btn--sm" id="sp-reset-btn">Làm mới</button>
            <button type="submit" class="btn btn--primary btn--sm" id="sp-save-btn">Lưu sản phẩm</button>
          </div>
        </div>
      </form>

      <div class="table-wrapper">
        <table class="table">
          <thead>
            <tr>
              <th>Mã</th>
              <th>Tên sản phẩm</th>
              <th>Danh mục</th>
              <th>Thương hiệu</th>
              <th>Phong cách</th>
              <th>Trạng thái</th>
              <th>Thao tác</th>
            </tr>
          </thead>
          <tbody id="sp-table-body">
            <tr>
              <td colspan="7" style="text-align:center; padding: 16px;">Đang tải dữ liệu...</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  `,
  categories: `
    <section class="card card--full">
      <div class="card__header-row">
        <div class="card__search-group">
          <input
            type="text"
            id="dm-search"
            class="input input--sm input--search-wide"
            placeholder="Tìm theo tên danh mục..."
          />
        </div>
        <div class="card__actions">
          <button type="button" class="btn btn--primary btn--sm" id="dm-add-btn">
            +
          </button>
        </div>
      </div>

      <form id="dm-form" class="dm-form dm-form--hidden" data-visible="false">
        <input type="hidden" id="dm-id" />
        <div class="form-grid">
          <div class="form-control" style="grid-column: 1 / -1;">
            <label for="dm-ten">Tên danh mục</label>
            <input id="dm-ten" type="text" required />
          </div>
        </div>
        <div class="dm-form__actions">
          <p class="form-error" id="dm-error" hidden></p>
          <div class="dm-form__buttons">
            <button type="button" class="btn btn--ghost btn--sm" id="dm-reset-btn">Làm mới</button>
            <button type="submit" class="btn btn--primary btn--sm" id="dm-save-btn">Lưu danh mục</button>
          </div>
        </div>
      </form>

      <div class="table-wrapper">
        <table class="table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Tên danh mục</th>
              <th>Thao tác</th>
            </tr>
          </thead>
          <tbody id="dm-table-body">
            <tr>
              <td colspan="3" style="text-align:center; padding: 16px;">Đang tải dữ liệu...</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  `,
  promotions: `<div></div>`,
  vouchers: `<div></div>`,
  blog: `<div></div>`
};

navLinks.forEach((link) => {
  link.addEventListener("click", (e) => {
    e.preventDefault();
    const page = link.getAttribute("data-page");

    // Cập nhật URL trên trình duyệt
    const newUrl = `/admin/${page}`;
    if (window.location.pathname !== newUrl) {
      window.history.pushState({ page }, "", newUrl);
    }

    // Active state
    navLinks.forEach((l) => l.classList.remove("sidebar__link--active"));
    link.classList.add("sidebar__link--active");

    // Đổi title + content
    if (pageTitle) pageTitle.textContent = page.charAt(0).toUpperCase() + page.slice(1);
    if (content && pages[page]) {
      content.innerHTML = pages[page];

      // ...existing code...
      if (page === "users" && typeof window.initKhachHangPage === "function") {
        window.initKhachHangPage();
      }
      if (page === "orders" && typeof window.initHoaDonPage === "function") {
        window.initHoaDonPage();
      }
      if (page === "products" && typeof window.initSanPhamPage === "function") {
        window.initSanPhamPage();
      }
      if (page === "categories" && typeof window.initDanhMucPage === "function") {
        window.initDanhMucPage();
      }
      if (page === "promotions" && typeof window.initKhuyenMaiPage === "function") {
        window.initKhuyenMaiPage();
      }
      if (page === "vouchers" && typeof window.initVoucherPage === "function") {
        window.initVoucherPage();
      }
      if (page === "blog" && typeof window.initBlogPage === "function") {
        window.initBlogPage();
      }
    }

    // Auto đóng sidebar trên mobile
    if (window.innerWidth <= 768 && sidebar) {
      sidebar.classList.remove("sidebar--open");
    }
  });
});


// Hàm render trang dựa trên URL (không cần click sidebar)
function renderPageFromPath() {
  const path = window.location.pathname.replace(/^\/admin\/?/, "");
  const page = path.split("/")[0] || "dashboard";
  // Active sidebar
  document.querySelectorAll(".sidebar__link").forEach(l => {
    if (l.getAttribute("data-page") === page) {
      l.classList.add("sidebar__link--active");
    } else {
      l.classList.remove("sidebar__link--active");
    }
  });
  // Đổi title + content
  if (pageTitle) pageTitle.textContent = page.charAt(0).toUpperCase() + page.slice(1);
  if (content && pages[page]) {
    content.innerHTML = pages[page];
    if (page === "users" && typeof window.initKhachHangPage === "function") {
      window.initKhachHangPage();
    }
    if (page === "orders" && typeof window.initHoaDonPage === "function") {
      window.initHoaDonPage();
    }
    if (page === "products" && typeof window.initSanPhamPage === "function") {
      window.initSanPhamPage();
    }
    if (page === "categories" && typeof window.initDanhMucPage === "function") {
      window.initDanhMucPage();
    }
    if (page === "promotions" && typeof window.initKhuyenMaiPage === "function") {
      window.initKhuyenMaiPage();
    }
    if (page === "vouchers" && typeof window.initVoucherPage === "function") {
      window.initVoucherPage();
    }
    if (page === "blog" && typeof window.initBlogPage === "function") {
      window.initBlogPage();
    }
  }
}
window.addEventListener("DOMContentLoaded", renderPageFromPath);
window.addEventListener("popstate", renderPageFromPath);

