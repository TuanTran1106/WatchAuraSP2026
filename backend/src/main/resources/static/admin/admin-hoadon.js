const HoaDonModule = (() => {
  const API_BASE = "/api/hoa-don";

  let orders = [];

  const els = {};

  function qs(id) {
    return document.getElementById(id);
  }

  function mapEls() {
    els.search = qs("hd-search");
    els.statusFilter = qs("hd-status-filter");
    els.tableBody = qs("hd-table-body");
    els.detailTitle = qs("hd-detail-title");
    els.detailBody = qs("hd-detail-body");
  }

  async function fetchJSON(url, options = {}) {
    const res = await fetch(url, {
      headers: { "Content-Type": "application/json" },
      ...options
    });

    if (!res.ok) {
      const text = await res.text();
      throw new Error(text || "Đã xảy ra lỗi máy chủ");
    }

    if (res.status === 204) return null;
    return res.json();
  }

  function formatCurrency(value) {
    if (value == null) return "";
    try {
      return new Intl.NumberFormat("vi-VN", {
        style: "currency",
        currency: "VND"
      }).format(value);
    } catch {
      return value;
    }
  }

  function formatDate(value) {
    if (!value) return "";
    try {
      const d = new Date(value);
      if (Number.isNaN(d.getTime())) return value;
      return d.toLocaleString("vi-VN");
    } catch {
      return value;
    }
  }

  function mapStatus(trangThaiDonHang) {
    switch (trangThaiDonHang) {
      case "CHO_XAC_NHAN":
        return { label: "Chờ xác nhận", badge: "badge--warning" };
      case "DANG_XU_LY":
        return { label: "Đang xử lý", badge: "badge--warning" };
      case "DANG_GIAO":
        return { label: "Đang giao", badge: "badge--warning" };
      case "DA_GIAO":
        return { label: "Đã giao", badge: "badge--success" };
      case "DA_HUY":
        return { label: "Đã hủy", badge: "badge--danger" };
      default:
        return { label: trangThaiDonHang || "Không rõ", badge: "" };
    }
  }

  function renderTable(list) {
    if (!els.tableBody) return;

    if (!list || list.length === 0) {
      els.tableBody.innerHTML =
        '<tr><td colspan="7" style="text-align:center; padding: 16px;">Không có dữ liệu</td></tr>';
      return;
    }

    const rows = list
      .map((hd) => {
        const statusInfo = mapStatus(hd.trangThaiDonHang);
        return `
          <tr data-id="${hd.id}">
            <td>${hd.maDonHang || ""}</td>
            <td>${hd.tenKhachHang || ""}</td>
            <td>${hd.sdtKhachHang || ""}</td>
            <td>${formatCurrency(hd.tongTienThanhToan)}</td>
            <td>
              <span class="badge ${statusInfo.badge}">${statusInfo.label}</span>
            </td>
            <td>${formatDate(hd.ngayDat)}</td>
            <td>
              <button class="btn-link" data-action="view">Xem</button>
            </td>
          </tr>
        `;
      })
      .join("");

    els.tableBody.innerHTML = rows;
  }

  async function loadOrders() {
    if (!els.tableBody) return;
    els.tableBody.innerHTML =
      '<tr><td colspan="7" style="text-align:center; padding: 16px;">Đang tải dữ liệu...</td></tr>';
    try {
      orders = await fetchJSON(API_BASE);
      applyFilter();
    } catch (e) {
      console.error(e);
      els.tableBody.innerHTML =
        '<tr><td colspan="7" style="text-align:center; padding: 16px;">Lỗi tải dữ liệu</td></tr>';
    }
  }

  function applyFilter() {
    if (!els.search) {
      renderTable(orders);
      return;
    }
    const keyword = els.search.value.trim().toLowerCase();
    const statusValue = els.statusFilter ? els.statusFilter.value : "all";

    const filtered = orders.filter((hd) => {
      const str = `${hd.maDonHang || ""} ${hd.tenKhachHang || ""} ${
        hd.sdtKhachHang || ""
      }`.toLowerCase();
      const matchKeyword = !keyword || str.includes(keyword);

      let matchStatus = true;
      if (statusValue !== "all") {
        matchStatus = hd.trangThaiDonHang === statusValue;
      }

      return matchKeyword && matchStatus;
    });

    renderTable(filtered);
  }

  function buildItemsTable(items = []) {
    if (!items || items.length === 0) {
      return "<p>Không có sản phẩm trong hóa đơn.</p>";
    }

    const rows = items
      .map((item) => {
        return `
          <tr>
            <td>${item.tenSanPham || ""}</td>
            <td>${item.soLuong || 0}</td>
            <td>${formatCurrency(item.donGia)}</td>
            <td>${formatCurrency(item.thanhTien)}</td>
          </tr>
        `;
      })
      .join("");

    return `
      <div class="table-wrapper">
        <table class="table">
          <thead>
            <tr>
              <th>Sản phẩm</th>
              <th>Số lượng</th>
              <th>Đơn giá</th>
              <th>Thành tiền</th>
            </tr>
          </thead>
          <tbody>
            ${rows}
          </tbody>
        </table>
      </div>
    `;
  }

  function showDetail(id) {
    const hd = orders.find((x) => String(x.id) === String(id));
    if (!hd) return;

    const statusInfo = mapStatus(hd.trangThaiDonHang);
    if (els.detailTitle) {
      els.detailTitle.textContent = `Hóa đơn ${hd.maDonHang || ""}`;
    }

    const diaChi = hd.diaChiGiaoHang || {};

    const infoHtml = `
      <div class="hd-detail__grid">
        <div class="hd-detail__section">
          <h4>Thông tin đơn hàng</h4>
          <p><strong>Mã đơn:</strong> ${hd.maDonHang || ""}</p>
          <p><strong>Trạng thái:</strong> ${statusInfo.label}</p>
          <p><strong>Ngày đặt:</strong> ${formatDate(hd.ngayDat)}</p>
          <p><strong>Thanh toán:</strong> ${hd.phuongThucThanhToan || ""}</p>
          <p><strong>Loại hóa đơn:</strong> ${hd.loaiHoaDon || ""}</p>
        </div>
        <div class="hd-detail__section">
          <h4>Khách hàng & giao hàng</h4>
          <p><strong>Khách hàng:</strong> ${hd.tenKhachHang || ""}</p>
          <p><strong>SĐT:</strong> ${hd.sdtKhachHang || ""}</p>
          <p><strong>Địa chỉ:</strong> ${hd.diaChi || ""}</p>
          ${
            diaChi && (diaChi.diaChiCuThe || diaChi.phuongXa || diaChi.quanHuyen || diaChi.tinhThanh)
              ? `<p><strong>Địa chỉ giao hàng:</strong> ${[
                  diaChi.diaChiCuThe,
                  diaChi.phuongXa,
                  diaChi.quanHuyen,
                  diaChi.tinhThanh
                ]
                  .filter(Boolean)
                  .join(", ")}</p>`
              : ""
          }
          ${hd.ghiChu ? `<p><strong>Ghi chú:</strong> ${hd.ghiChu}</p>` : ""}
        </div>
      </div>
    `;

    const summaryHtml = `
      <div class="hd-detail__summary">
        <p><strong>Tạm tính:</strong> ${formatCurrency(hd.tongTienTamTinh)}</p>
        <p><strong>Giảm giá:</strong> ${formatCurrency(hd.tienGiam)}</p>
        <p><strong>Tổng thanh toán:</strong> ${formatCurrency(hd.tongTienThanhToan)}</p>
      </div>
    `;

    const bodyHtml = `
      ${infoHtml}
      <h4>Sản phẩm</h4>
      ${buildItemsTable(hd.items || [])}
      ${summaryHtml}
    `;

    if (els.detailBody) {
      els.detailBody.innerHTML = bodyHtml;
    }
    // Scroll xuống khu vực chi tiết cho user dễ thấy
    els.detailBody.scrollIntoView({ behavior: "smooth", block: "start" });
  }

  function bindEvents() {
    if (els.search) {
      els.search.addEventListener("input", () => applyFilter());
    }

    if (els.statusFilter) {
      els.statusFilter.addEventListener("change", () => applyFilter());
    }

    if (els.tableBody) {
      els.tableBody.addEventListener("click", (e) => {
        const btn = e.target.closest("button[data-action]");
        if (!btn) return;
        const action = btn.dataset.action;
        const tr = btn.closest("tr[data-id]");
        const id = tr ? tr.dataset.id : null;

        if (action === "view" && id) {
          showDetail(id);
        }
      });
    }
  }

  function init() {
    mapEls();
    bindEvents();
    loadOrders();
  }

  return { init };
})();

// Hàm global để admin.js gọi sau khi render trang Orders
window.initHoaDonPage = () => {
  HoaDonModule.init();
};

