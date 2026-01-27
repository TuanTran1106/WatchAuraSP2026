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
      <div class="card__header">
        <h2 class="card__title">Quản lý khách hàng</h2>
      </div>

      <div class="card__toolbar">
        <div class="card__toolbar-left">
          <input
            type="text"
            id="userSearch"
            class="input"
            placeholder="Tìm theo tên, email, SĐT..."
          />
        </div>
        <div class="card__toolbar-right">
          <button
            type="button"
            id="toggleUserFormBtn"
            class="btn btn--icon"
            title="Thêm khách hàng"
          >
            +
          </button>
        </div>
      </div>

      <form id="userForm" class="form-grid" hidden>
        <input type="hidden" id="userId" />
        <div class="form-group">
          <label for="maNguoiDung">Mã khách hàng</label>
          <input type="text" id="maNguoiDung" required />
        </div>
        <div class="form-group">
          <label for="tenNguoiDung">Họ tên</label>
          <input type="text" id="tenNguoiDung" required />
        </div>
        <div class="form-group">
          <label for="email">Email</label>
          <input type="email" id="email" />
        </div>
        <div class="form-group">
          <label for="sdt">Số điện thoại</label>
          <input type="text" id="sdt" />
        </div>
        <div class="form-group">
          <label for="matKhau">Mật khẩu</label>
          <input type="password" id="matKhau" required />
        </div>
        <div class="form-group">
          <label for="gioiTinh">Giới tính</label>
          <select id="gioiTinh">
            <option value="">Chọn</option>
            <option value="Nam">Nam</option>
            <option value="Nữ">Nữ</option>
            <option value="Khác">Khác</option>
          </select>
        </div>
        <div class="form-group">
          <label for="ngaySinh">Ngày sinh</label>
          <input type="date" id="ngaySinh" />
        </div>
        <div class="form-group">
          <label for="trangThai">Trạng thái</label>
          <select id="trangThai">
            <option value="true">Hoạt động</option>
            <option value="false">Khóa</option>
          </select>
        </div>
        <div class="form-actions">
          <button type="submit" class="btn btn--primary">Lưu</button>
        </div>
      </form>

      <div class="table-wrapper">
        <table class="table" id="usersTable">
          <thead>
            <tr>
              <th>Mã</th>
              <th>Họ tên</th>
              <th>Email</th>
              <th>SĐT</th>
              <th>Giới tính</th>
              <th>Ngày sinh</th>
              <th>Trạng thái</th>
              <th>Thao tác</th>
            </tr>
          </thead>
          <tbody id="usersTableBody"></tbody>
        </table>
      </div>
    </section>
  `,
  orders: `
    <section class="card card--full">
      <h2 class="card__title">Đơn Hàng</h2>
      <p>Danh sách đơn hàng.</p>
    </section>
  `,
  products: `
    <section class="card card--full">
      <h2 class="card__title">Sản Phẩm</h2>
      <p>Danh sách sản phẩm.</p>
    </section>
  `,
  settings: `
    <section class="card card--full">
      <h2 class="card__title">Cài Đặt</h2>
      <p>Các thiết lập hệ thống.</p>
    </section>
  `
};

// ---------------------- Users (Khách hàng) page logic ----------------------

function getToastContainer() {
  let container = document.querySelector(".toast-container");
  if (!container) {
    container = document.createElement("div");
    container.className = "toast-container";
    document.body.appendChild(container);
  }
  return container;
}

function showToast(type, message) {
  const container = getToastContainer();
  const toast = document.createElement("div");
  toast.className = `toast toast--${type}`;

  const iconSpan = document.createElement("span");
  iconSpan.className = "toast__icon";
  iconSpan.textContent = type === "success" ? "✓" : "⚠";

  const textSpan = document.createElement("span");
  textSpan.textContent = message;

  toast.appendChild(iconSpan);
  toast.appendChild(textSpan);
  container.appendChild(toast);

  setTimeout(() => {
    toast.remove();
    if (!container.childElementCount) {
      container.remove();
    }
  }, 3000);
}

let customersCache = [];

async function fetchCustomers() {
  try {
    const res = await fetch("/api/khach-hang");
    if (!res.ok) throw new Error("Không thể tải danh sách khách hàng");
    const data = await res.json();
    customersCache = Array.isArray(data) ? data : [];
    renderCustomers(customersCache);
  } catch (error) {
    console.error(error);
    showToast("error", "Lỗi khi tải danh sách khách hàng.");
  }
}

function renderCustomers(list) {
  const tbody = document.getElementById("usersTableBody");
  if (!tbody) return;

  if (!list.length) {
    tbody.innerHTML = `
      <tr>
        <td colspan="8" style="text-align:center;">Không có khách hàng.</td>
      </tr>
    `;
    return;
  }

  tbody.innerHTML = list
    .map((c) => {
      const ngaySinhDisplay = c.ngaySinh ? c.ngaySinh : "";
      const trangThaiText = c.trangThai ? "Hoạt động" : "Khóa";
      return `
        <tr data-id="${c.id}">
          <td>${c.maNguoiDung ?? ""}</td>
          <td>${c.tenNguoiDung ?? ""}</td>
          <td>${c.email ?? ""}</td>
          <td>${c.sdt ?? ""}</td>
          <td>${c.gioiTinh ?? ""}</td>
          <td>${ngaySinhDisplay}</td>
          <td>${trangThaiText}</td>
          <td>
            <button type="button" class="btn btn--small btn-edit" data-id="${c.id}">Sửa</button>
          </td>
        </tr>
      `;
    })
    .join("");
}

function fillUserForm(customer) {
  const idEl = document.getElementById("userId");
  const maEl = document.getElementById("maNguoiDung");
  const tenEl = document.getElementById("tenNguoiDung");
  const emailEl = document.getElementById("email");
  const sdtEl = document.getElementById("sdt");
  const matKhauEl = document.getElementById("matKhau");
  const gioiTinhEl = document.getElementById("gioiTinh");
  const ngaySinhEl = document.getElementById("ngaySinh");
  const trangThaiEl = document.getElementById("trangThai");

  if (!idEl) return;

  idEl.value = customer.id ?? "";
  if (maEl) maEl.value = customer.maNguoiDung ?? "";
  if (tenEl) tenEl.value = customer.tenNguoiDung ?? "";
  if (emailEl) emailEl.value = customer.email ?? "";
  if (sdtEl) sdtEl.value = customer.sdt ?? "";
  if (matKhauEl) matKhauEl.value = customer.matKhau ?? "";
  if (gioiTinhEl) gioiTinhEl.value = customer.gioiTinh ?? "";
  if (ngaySinhEl) ngaySinhEl.value = customer.ngaySinh ?? "";
  if (trangThaiEl)
    trangThaiEl.value =
      customer.trangThai === null || customer.trangThai === undefined
        ? "true"
        : String(customer.trangThai);
}

function resetUserForm() {
  const form = document.getElementById("userForm");
  const idEl = document.getElementById("userId");
  if (form) form.reset();
  if (idEl) idEl.value = "";
}

function initUsersPage() {
  fetchCustomers();

  const form = document.getElementById("userForm");
  const searchInput = document.getElementById("userSearch");
  const toggleFormBtn = document.getElementById("toggleUserFormBtn");
  const tbody = document.getElementById("usersTableBody");

  if (toggleFormBtn && form) {
    toggleFormBtn.addEventListener("click", () => {
      const willShow = form.hidden;
      if (willShow) {
        resetUserForm();
        form.hidden = false;
        form.scrollIntoView({ behavior: "smooth", block: "start" });
      } else {
        form.hidden = true;
      }
    });
  }

  if (form) {
    form.addEventListener("submit", async (e) => {
      e.preventDefault();

      const id = document.getElementById("userId").value;
      const maNguoiDung = document.getElementById("maNguoiDung").value.trim();
      const tenNguoiDung = document.getElementById("tenNguoiDung").value.trim();
      const email = document.getElementById("email").value.trim();
      const sdt = document.getElementById("sdt").value.trim();
      const matKhau = document.getElementById("matKhau").value;
      const gioiTinh = document.getElementById("gioiTinh").value;
      const ngaySinh = document.getElementById("ngaySinh").value;
      const trangThai = document.getElementById("trangThai").value === "true";

      if (!maNguoiDung || !tenNguoiDung || !matKhau) {
        showToast("error", "Vui lòng nhập đủ Mã KH, Họ tên và Mật khẩu.");
        return;
      }

      const payload = {
        id: id || null,
        maNguoiDung,
        tenNguoiDung,
        email: email || null,
        sdt: sdt || null,
        matKhau,
        gioiTinh: gioiTinh || null,
        ngaySinh: ngaySinh || null,
        hinhAnh: null,
        trangThai,
        chucVu: null
      };

      try {
        let res;
        if (id) {
          res = await fetch(`/api/khach-hang/${id}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
          });
        } else {
          res = await fetch("/api/khach-hang", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
          });
        }

        if (!res.ok) throw new Error("Lưu khách hàng thất bại");

        await fetchCustomers();
        resetUserForm();
        showToast("success", "Lưu khách hàng thành công.");
      } catch (error) {
        console.error(error);
        showToast("error", "Đã xảy ra lỗi khi lưu khách hàng.");
      }
    });
  }

  if (searchInput) {
    searchInput.addEventListener("input", () => {
      const keyword = searchInput.value.toLowerCase();
      const filtered = customersCache.filter((c) => {
        const name = (c.tenNguoiDung || "").toLowerCase();
        const email = (c.email || "").toLowerCase();
        const phone = (c.sdt || "").toLowerCase();
        return (
          name.includes(keyword) ||
          email.includes(keyword) ||
          phone.includes(keyword)
        );
      });
      renderCustomers(filtered);
    });
  }

  if (tbody) {
    tbody.addEventListener("click", async (e) => {
      const target = e.target;
      if (!(target instanceof HTMLElement)) return;

      const id = target.getAttribute("data-id");
      if (!id) return;

      if (target.classList.contains("btn-edit")) {
        const customer = customersCache.find((c) => String(c.id) === id);
        if (customer) {
          const form = document.getElementById("userForm");
          if (form && form.hidden) {
            form.hidden = false;
          }
          fillUserForm(customer);
          if (form) {
            form.scrollIntoView({ behavior: "smooth", block: "start" });
          }
        }
      }

    });
  }
}

// ---------------------- Sidebar navigation / simple routing ----------------------

navLinks.forEach((link) => {
  link.addEventListener("click", (e) => {
    e.preventDefault();
    const page = link.getAttribute("data-page");

    // Active state
    navLinks.forEach((l) => l.classList.remove("sidebar__link--active"));
    link.classList.add("sidebar__link--active");

    // Đổi title + content
    if (pageTitle)
      pageTitle.textContent =
        page === "users"
          ? "Khách hàng"
          : page.charAt(0).toUpperCase() + page.slice(1);
    if (content && pages[page]) {
      content.innerHTML = pages[page];

      // Khởi tạo logic riêng cho từng trang nếu cần
      if (page === "users") {
        initUsersPage();
      }
    }

    // Auto đóng sidebar trên mobile
    if (window.innerWidth <= 768 && sidebar) {
      sidebar.classList.remove("sidebar--open");
    }
  });
});

