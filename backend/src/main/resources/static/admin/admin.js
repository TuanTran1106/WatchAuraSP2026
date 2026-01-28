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

let invoices = [];
let selectedInvoice = null;

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
      <h2 class="card__title">Hóa đơn</h2>
      <div id="ordersTableContainer"></div>
      <div id="orderDetailContainer" style="margin-top: 12px;"></div>
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


function getStatusBadgeClass(trangThaiDonHang) {
  if (!trangThaiDonHang) return "";
  if (trangThaiDonHang === "CHO_XU_LY" || trangThaiDonHang === "CHO_XAC_NHAN") {
    return "badge badge--warning";
  }
  if (trangThaiDonHang === "DA_XAC_NHAN" || trangThaiDonHang === "DA_THANH_TOAN") {
    return "badge badge--success";
  }
  return "badge";
}

function getStatusLabel(trangThaiDonHang) {
  switch (trangThaiDonHang) {
    case "CHO_XU_LY":
      return "Chờ xử lý";
    case "CHO_XAC_NHAN":
      return "Chờ xác nhận";
    case "DA_XAC_NHAN":
      return "Đã xác nhận";
    case "DA_THANH_TOAN":
      return "Đã thanh toán";
    default:
      return trangThaiDonHang || "Không rõ";
  }
}

function formatCurrency(value) {
  if (value == null) return "-";
  try {
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND"
    }).format(value);
  } catch (e) {
    return value;
  }
}

function formatDateTime(isoString) {
  if (!isoString) return "-";
  try {
    const d = new Date(isoString);
    const date = d.toLocaleDateString("vi-VN");
    const time = d.toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" });
    return `${date} ${time}`;
  } catch (e) {
    return isoString;
  }
}

function renderInvoiceTable(tableContainer, detailContainer) {
  if (!tableContainer) return;

  if (!Array.isArray(invoices) || invoices.length === 0) {
    tableContainer.innerHTML =
      '<p style="font-size: 13px; color: var(--text-soft);">Chưa có hóa đơn nào.</p>';
    if (detailContainer) {
      detailContainer.innerHTML =
        '<p style="font-size: 13px; color: var(--text-soft);">Chọn "Xem" để xem chi tiết hóa đơn.</p>';
    }
    return;
  }

  const rowsHtml = invoices
    .map(
      (hd) => `
      <tr>
        <td>${hd.maDonHang || "-"}</td>
        <td>${hd.tenKhachHang || "-"}</td>
        <td>${formatDateTime(hd.ngayDat)}</td>
        <td>${formatCurrency(hd.tongTienThanhToan)}</td>
        <td>
          <span class="${getStatusBadgeClass(hd.trangThaiDonHang)}">
            ${getStatusLabel(hd.trangThaiDonHang)}
          </span>
        </td>
        <td>
          <button class="btn btn--primary btn--sm" data-invoice-view data-id="${hd.id}">
            Xem
          </button>
        </td>
      </tr>
    `
    )
    .join("");

  tableContainer.innerHTML = `
    <table class="table">
      <thead>
        <tr>
          <th>Mã hóa đơn</th>
          <th>Khách hàng</th>
          <th>Ngày tạo</th>
          <th>Tổng tiền</th>
          <th>Trạng thái</th>
          <th>Hành động</th>
        </tr>
      </thead>
      <tbody>
        ${rowsHtml}
      </tbody>
    </table>
  `;

  tableContainer.querySelectorAll("[data-invoice-view]").forEach((btn) => {
    btn.addEventListener("click", () => {
      const id = btn.getAttribute("data-id");
      if (!id) return;
      const numericId = Number(id);
      loadInvoiceDetail(numericId, detailContainer);
    });
  });
}

function renderInvoiceDetail(invoice, detailContainer) {
  if (!detailContainer) return;
  if (!invoice) {
    detailContainer.innerHTML =
      '<p style="font-size: 13px; color: var(--text-soft);">Chọn "Xem" để xem chi tiết hóa đơn.</p>';
    return;
  }

  const status = invoice.trangThaiDonHang;
  const canConfirm = status === "CHO_XAC_NHAN" || status === "CHO_XU_LY";
  const canComplete = status === "DA_XAC_NHAN";

  detailContainer.innerHTML = `
    <section class="card">
      <h3 class="card__title">Chi tiết hóa đơn</h3>
      <div class="invoice-detail">
        <div class="invoice-detail__row">
          <span class="invoice-detail__label">Mã hóa đơn:</span>
          <span class="invoice-detail__value">${invoice.maDonHang || "-"}</span>
        </div>
        <div class="invoice-detail__row">
          <span class="invoice-detail__label">Khách hàng:</span>
          <span class="invoice-detail__value">${invoice.tenKhachHang || "-"}</span>
        </div>
        <div class="invoice-detail__row">
          <span class="invoice-detail__label">Tổng tiền:</span>
          <span class="invoice-detail__value">${formatCurrency(invoice.tongTienThanhToan)}</span>
        </div>
        <div class="invoice-detail__row">
          <span class="invoice-detail__label">Trạng thái:</span>
          <span class="invoice-detail__value">
            <span class="${getStatusBadgeClass(invoice.trangThaiDonHang)}">
              ${getStatusLabel(invoice.trangThaiDonHang)}
            </span>
          </span>
        </div>
      </div>
      <div class="invoice-detail__actions">
        ${
          canConfirm
            ? `<button class="btn btn--primary" id="btnConfirmOrder" data-id="${invoice.id}">
                Xác nhận đơn
              </button>`
            : canComplete
              ? `<button class="btn btn--primary" id="btnCompleteOrder" data-id="${invoice.id}">
                  Hoàn thành
                </button>`
              : `<span class="invoice-detail__note">Không có hành động khả dụng.</span>`
        }
      </div>
    </section>
  `;

  if (canConfirm) {
    const btnConfirm = document.getElementById("btnConfirmOrder");
    if (btnConfirm) {
      btnConfirm.addEventListener("click", () => {
        const id = btnConfirm.getAttribute("data-id");
        if (!id) return;
        updateInvoiceStatus(Number(id), detailContainer, "DA_XAC_NHAN", btnConfirm);
      });
    }
  } else if (canComplete) {
    const btnComplete = document.getElementById("btnCompleteOrder");
    if (btnComplete) {
      btnComplete.addEventListener("click", () => {
        const id = btnComplete.getAttribute("data-id");
        if (!id) return;
        updateInvoiceStatus(Number(id), detailContainer, "DA_THANH_TOAN", btnComplete);
      });
    }
  }
}

function loadInvoiceDetail(id, detailContainer) {
  if (!detailContainer) return;
  detailContainer.innerHTML =
    '<p style="font-size: 13px; color: var(--text-soft);">Đang tải chi tiết hóa đơn...</p>';

  fetch(`/api/hoa-don/${id}`)
    .then((res) => {
      if (!res.ok) {
        throw new Error("Không tải được chi tiết hóa đơn");
      }
      return res.json();
    })
    .then((data) => {
      selectedInvoice = data;
      renderInvoiceDetail(selectedInvoice, detailContainer);
    })
    .catch(() => {
      detailContainer.innerHTML =
        '<p style="font-size: 13px; color: #ef4444;">Lỗi khi tải chi tiết hóa đơn.</p>';
    });
}

function updateInvoiceStatus(id, detailContainer, newStatus, btn) {
  if (btn) {
    btn.disabled = true;
    btn.textContent = "Đang xử lý...";
  }

  fetch(`/api/hoa-don/${id}/trang-thai?trangThaiDonHang=${encodeURIComponent(newStatus)}`, {
    method: "PUT"
  })
    .then((res) => {
      if (!res.ok) {
        throw new Error("Không thể cập nhật trạng thái");
      }
      return res.json();
    })
    .then((updated) => {
      selectedInvoice = updated;
      invoices = invoices.map((hd) => (hd.id === updated.id ? updated : hd));

      const tableContainer = document.getElementById("ordersTableContainer");
      renderInvoiceTable(tableContainer, detailContainer);
      renderInvoiceDetail(selectedInvoice, detailContainer);
    })
    .catch(() => {
      if (btn) {
        btn.disabled = false;
        btn.textContent = "Thử lại";
      }
      if (detailContainer) {
        detailContainer.insertAdjacentHTML(
          "beforeend",
          '<p style="font-size: 13px; color: #ef4444; margin-top: 6px;">Lỗi khi cập nhật trạng thái đơn.</p>'
        );
      }
    });
}

function initOrdersPage() {
  const tableContainer = document.getElementById("ordersTableContainer");
  const detailContainer = document.getElementById("orderDetailContainer");

  if (!tableContainer || !detailContainer) return;

  tableContainer.innerHTML =
    '<p style="font-size: 13px; color: var(--text-soft);">Đang tải dữ liệu hóa đơn...</p>';
  detailContainer.innerHTML =
    '<p style="font-size: 13px; color: var(--text-soft);">Chọn "Xem" để xem chi tiết hóa đơn.</p>';

  fetch("/api/hoa-don")
    .then((res) => {
      if (!res.ok) {
        throw new Error("Không tải được danh sách hóa đơn");
      }
      return res.json();
    })
    .then((data) => {
      invoices = Array.isArray(data) ? data : [];
      renderInvoiceTable(tableContainer, detailContainer);
    })
    .catch(() => {
      tableContainer.innerHTML =
        '<p style="font-size: 13px; color: #ef4444;">Lỗi khi tải danh sách hóa đơn.</p>';
    });
}

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
    if (pageTitle) pageTitle.textContent = page.charAt(0).toUpperCase() + page.slice(1);
    if (content && pages[page]) {
      content.innerHTML = pages[page];
      if (page === "orders") {
        initOrdersPage();
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

