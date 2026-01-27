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
      <h2 class="card__title">Users</h2>
      <p>Danh sách user (bạn có thể thay bằng bảng user thật, dữ liệu từ backend).</p>
    </section>
  `,
  orders: `
    <section class="card card--full">
      <h2 class="card__title">Hóa đơn</h2>
      <div id="ordersTableContainer"></div>
      <div id="orderDetailContainer" style="margin-top: 12px;"></div>
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
      }
    }

    // Auto đóng sidebar trên mobile
    if (window.innerWidth <= 768 && sidebar) {
      sidebar.classList.remove("sidebar--open");
    }
  });
});

