const KhachHangModule = (() => {
  const API_BASE = "/api/khach-hang";

  let customers = [];

  const els = {};

  function qs(id) {
    return document.getElementById(id);
  }

  function mapEls() {
    els.search = qs("kh-search");
    els.statusFilter = qs("kh-status-filter");
    els.addBtn = qs("kh-add-btn");
    els.tableBody = qs("kh-table-body");
    els.form = qs("kh-form");
    els.id = qs("kh-id");
    els.ma = qs("kh-ma");
    els.ten = qs("kh-ten");
    els.email = qs("kh-email");
    els.sdt = qs("kh-sdt");
    els.matKhau = qs("kh-matkhau");
    els.gioiTinh = qs("kh-gioitinh");
    els.trangThai = qs("kh-trangthai");
    els.error = qs("kh-error");
    els.saveBtn = qs("kh-save-btn");
    els.resetBtn = qs("kh-reset-btn");
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

  function renderTable(list) {
    if (!els.tableBody) return;

    if (!list || list.length === 0) {
      els.tableBody.innerHTML =
        '<tr><td colspan="7" style="text-align:center; padding: 16px;">Không có dữ liệu</td></tr>';
      return;
    }

    const rows = list
      .map(
        (kh) => `
        <tr data-id="${kh.id}">
          <td>${kh.maNguoiDung || ""}</td>
          <td>${kh.tenNguoiDung || ""}</td>
          <td>${kh.email || ""}</td>
          <td>${kh.sdt || ""}</td>
          <td>${kh.gioiTinh || ""}</td>
          <td>
            <span class="badge ${
              kh.trangThai ? "badge--success" : "badge--warning"
            }">${kh.trangThai ? "Hoạt động" : "Ngưng"}</span>
          </td>
          <td>
            <button class="btn-link" data-action="edit">Sửa</button>
            <button class="btn-link btn-link--danger" data-action="delete">Xóa</button>
          </td>
        </tr>
      `
      )
      .join("");

    els.tableBody.innerHTML = rows;
  }

  async function loadCustomers() {
    if (!els.tableBody) return;
    els.tableBody.innerHTML =
      '<tr><td colspan="7" style="text-align:center; padding: 16px;">Đang tải dữ liệu...</td></tr>';
    try {
      customers = await fetchJSON(API_BASE);
      applyFilter();
    } catch (e) {
      console.error(e);
      els.tableBody.innerHTML =
        '<tr><td colspan="7" style="text-align:center; padding: 16px;">Lỗi tải dữ liệu</td></tr>';
    }
  }

  function applyFilter() {
    if (!els.search) {
      renderTable(customers);
      return;
    }
    const keyword = els.search.value.trim().toLowerCase();
    const statusValue = els.statusFilter ? els.statusFilter.value : "all";

    const filtered = customers.filter((kh) => {
      const str = `${kh.maNguoiDung || ""} ${kh.tenNguoiDung || ""} ${
        kh.email || ""
      } ${kh.sdt || ""}`.toLowerCase();
      const matchKeyword = !keyword || str.includes(keyword);

      const isActive = !!kh.trangThai;
      let matchStatus = true;
      if (statusValue === "active") matchStatus = isActive;
      else if (statusValue === "inactive") matchStatus = !isActive;

      return matchKeyword && matchStatus;
    });
    renderTable(filtered);
  }

  function fillForm(mode, kh = null) {
    els.error.hidden = true;
    els.error.textContent = "";

    if (mode === "create") {
      els.id.value = "";
      els.ma.value = "";
      els.ten.value = "";
      els.email.value = "";
      els.sdt.value = "";
      els.matKhau.value = "";
      els.gioiTinh.value = "";
      els.trangThai.value = "true";
    } else if (mode === "edit" && kh) {
      els.id.value = kh.id;
      els.ma.value = kh.maNguoiDung || "";
      els.ten.value = kh.tenNguoiDung || "";
      els.email.value = kh.email || "";
      els.sdt.value = kh.sdt || "";
      els.matKhau.value = kh.matKhau || "";
      els.gioiTinh.value = kh.gioiTinh || "";
      els.trangThai.value = String(kh.trangThai);
    }

    els.form.dataset.mode = mode;

    // Khi đi vào chế độ create/edit thì luôn mở form ra cho dễ thao tác
    if (els.form.classList.contains("kh-form--hidden")) {
      els.form.classList.remove("kh-form--hidden");
      els.form.dataset.visible = "true";
      if (els.addBtn) {
        els.addBtn.textContent = "Đóng form";
      }
    }
  }

  async function handleSave() {
    if (!els.form.checkValidity()) {
      els.form.reportValidity();
      return;
    }

    const payload = {
      maNguoiDung: els.ma.value.trim(),
      tenNguoiDung: els.ten.value.trim(),
      email: els.email.value.trim() || null,
      sdt: els.sdt.value.trim() || null,
      matKhau: els.matKhau.value,
      gioiTinh: els.gioiTinh.value || null,
      trangThai: els.trangThai.value === "true"
    };

    const mode = els.form.dataset.mode || "create";
    let url = API_BASE;
    let method = "POST";

    if (mode === "edit" && els.id.value) {
      url = `${API_BASE}/${els.id.value}`;
      method = "PUT";
    }

    try {
      const saved = await fetchJSON(url, {
        method,
        body: JSON.stringify(payload)
      });
      // Sau khi lưu xong thì reset form và reload danh sách
      fillForm("create");
      await loadCustomers();
    } catch (e) {
      console.error(e);
      els.error.hidden = false;
      els.error.textContent = e.message || "Không thể lưu khách hàng";
    }
  }

  async function handleDelete(id) {
    if (!id) return;
    const confirmDelete = window.confirm("Bạn có chắc muốn xóa khách hàng này?");
    if (!confirmDelete) return;

    try {
      await fetchJSON(`${API_BASE}/${id}`, { method: "DELETE" });
      await loadCustomers();
    } catch (e) {
      console.error(e);
      alert("Không thể xóa khách hàng");
    }
  }

  function bindEvents() {
    if (els.search) {
      els.search.addEventListener("input", () => applyFilter());
    }

    if (els.statusFilter) {
      els.statusFilter.addEventListener("change", () => applyFilter());
    }

    if (els.saveBtn) {
      els.form.addEventListener("submit", (e) => {
        e.preventDefault();
        handleSave();
      });
    }

    if (els.resetBtn) {
      els.resetBtn.addEventListener("click", (e) => {
        e.preventDefault();
        fillForm("create");
      });
    }

    if (els.addBtn && els.form) {
      els.addBtn.addEventListener("click", () => {
        const isVisible = els.form.dataset.visible === "true";
        if (isVisible) {
          els.form.classList.add("kh-form--hidden");
          els.form.dataset.visible = "false";
          if (els.addBtn) els.addBtn.textContent = "+";
        } else {
          els.form.classList.remove("kh-form--hidden");
          els.form.dataset.visible = "true";
          if (els.addBtn) els.addBtn.textContent = "×";
        }
      });
    }

    if (els.tableBody) {
      els.tableBody.addEventListener("click", (e) => {
        const btn = e.target.closest("button[data-action]");
        if (!btn) return;
        const action = btn.dataset.action;
        const tr = btn.closest("tr[data-id]");
        const id = tr ? tr.dataset.id : null;
        const kh = customers.find((x) => String(x.id) === String(id));

        if (action === "edit" && kh) {
          fillForm("edit", kh);
        } else if (action === "delete" && id) {
          handleDelete(id);
        }
      });
    }
  }

  function init() {
    mapEls();
    bindEvents();
    loadCustomers();
  }

  return { init };
})();

// Hàm global để admin.js gọi sau khi render trang Users
window.initKhachHangPage = () => {
  KhachHangModule.init();
};

