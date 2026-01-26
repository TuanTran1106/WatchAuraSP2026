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
      <h2 class="card__title">Users</h2>
      <p>Danh sách user (bạn có thể thay bằng bảng user thật, dữ liệu từ backend).</p>
    </section>
  `,
  orders: `
    <section class="card card--full">
      <h2 class="card__title">Orders</h2>
      <p>Danh sách đơn hàng.</p>
    </section>
  `,
  products: `
    <section class="card card--full">
      <h2 class="card__title">Products</h2>
      <p>Danh sách sản phẩm.</p>
    </section>
  `,
  settings: `
    <section class="card card--full">
      <h2 class="card__title">Settings</h2>
      <p>Các thiết lập hệ thống.</p>
    </section>
  `
};

navLinks.forEach((link) => {
  link.addEventListener("click", (e) => {
    e.preventDefault();
    const page = link.getAttribute("data-page");

    // Active state
    navLinks.forEach((l) => l.classList.remove("sidebar__link--active"));
    link.classList.add("sidebar__link--active");

    // Đổi title + content
    if (pageTitle) pageTitle.textContent = page.charAt(0).toUpperCase() + page.slice(1);
    if (content && pages[page]) content.innerHTML = pages[page];

    // Auto đóng sidebar trên mobile
    if (window.innerWidth <= 768 && sidebar) {
      sidebar.classList.remove("sidebar--open");
    }
  });
});

