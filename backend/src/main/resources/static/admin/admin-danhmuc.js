const DanhMucModule = (() => {
  const API_BASE = "/api/danh-muc";

  let categories = [];

  const els = {};

  function qs(id) {
    return document.getElementById(id);
  }

  function mapEls() {
    els.search = qs("dm-search");
    els.addBtn = qs("dm-add-btn");
    els.tableBody = qs("dm-table-body");
    els.form = qs("dm-form");
    els.id = qs("dm-id");
    els.ten = qs("dm-ten");
    els.error = qs("dm-error");
    els.saveBtn = qs("dm-save-btn");
    els.resetBtn = qs("dm-reset-btn");
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
        '<tr><td colspan="3" style="text-align:center; padding: 16px;">Không có dữ liệu</td></tr>';
      return;
    }

    const rows = list
      .map(
        (dm) => `
        <tr data-id="${dm.id}">
          <td>${dm.id}</td>
          <td>${dm.tenDanhMuc || ""}</td>
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

  async function loadCategories() {
    if (!els.tableBody) return;
    els.tableBody.innerHTML =
      '<tr><td colspan="3" style="text-align:center; padding: 16px;">Đang tải dữ liệu...</td></tr>';
    try {
      categories = await fetchJSON(API_BASE);
      applyFilter();
    } catch (e) {
      console.error(e);
      els.tableBody.innerHTML =
        '<tr><td colspan="3" style="text-align:center; padding: 16px;">Lỗi tải dữ liệu</td></tr>';
    }
  }

  function applyFilter() {
    if (!els.search) {
      renderTable(categories);
      return;
    }
    const keyword = els.search.value.trim().toLowerCase();
    const filtered = categories.filter((dm) =>
      (dm.tenDanhMuc || "").toLowerCase().includes(keyword)
    );
    renderTable(filtered);
  }

  async function deleteCategory(id) {
    if (!id) return;
    const confirmDelete = window.confirm("Bạn có chắc muốn xóa danh mục này?");
    if (!confirmDelete) return;

    try {
      await fetchJSON(`${API_BASE}/${id}`, { method: "DELETE" });
      await loadCategories();
    } catch (e) {
      console.error(e);
      window.alert("Không thể xóa danh mục");
    }
  }

  function bindEvents() {
    if (els.search) {
      els.search.addEventListener("input", () => applyFilter());
    }

    if (els.tableBody) {
      els.tableBody.addEventListener("click", (e) => {
        const btn = e.target.closest("button[data-action]");
        if (!btn) return;
        const action = btn.dataset.action;
        const tr = btn.closest("tr[data-id]");
        const id = tr ? tr.dataset.id : null;

        if (action === "edit" && id) {
          const dm = categories.find((x) => String(x.id) === String(id));
          if (!dm || !els.form) return;
          if (els.error) {
            els.error.hidden = true;
            els.error.textContent = "";
          }
          if (els.id) els.id.value = dm.id;
          if (els.ten) els.ten.value = dm.tenDanhMuc || "";
          els.form.dataset.mode = "edit";
          els.form.classList.remove("dm-form--hidden");
          els.form.dataset.visible = "true";
          if (els.addBtn) els.addBtn.textContent = "×";
        } else if (action === "delete" && id) {
          deleteCategory(id);
        }
      });
    }

    if (els.form && els.saveBtn) {
      els.form.addEventListener("submit", (e) => {
        e.preventDefault();
        if (!els.form.checkValidity()) {
          els.form.reportValidity();
          return;
        }

        const name = els.ten ? els.ten.value.trim() : "";
        if (!name) return;

        const mode = els.form.dataset.mode || "create";
        let url = API_BASE;
        let method = "POST";
        let body = { tenDanhMuc: name };

        if (mode === "edit" && els.id && els.id.value) {
          url = `${API_BASE}/${els.id.value}`;
          method = "PUT";
          const current = categories.find((x) => String(x.id) === String(els.id.value));
          body = { ...(current || {}), tenDanhMuc: name };
        }

        fetchJSON(url, {
          method,
          body: JSON.stringify(body)
        })
          .then(() => {
            if (els.error) {
              els.error.hidden = true;
              els.error.textContent = "";
            }
            if (els.id) els.id.value = "";
            if (els.ten) els.ten.value = "";
            els.form.dataset.mode = "create";
            return loadCategories();
          })
          .catch((err) => {
            console.error(err);
            if (els.error) {
              els.error.hidden = false;
              els.error.textContent = "Không thể lưu danh mục";
            }
          });
      });
    }

    if (els.resetBtn) {
      els.resetBtn.addEventListener("click", (e) => {
        e.preventDefault();
        if (els.id) els.id.value = "";
        if (els.ten) els.ten.value = "";
        if (els.error) {
          els.error.hidden = true;
          els.error.textContent = "";
        }
        if (els.form) els.form.dataset.mode = "create";
      });
    }

    if (els.addBtn && els.form) {
      els.addBtn.addEventListener("click", () => {
        const isVisible = els.form.dataset.visible === "true";
        if (isVisible) {
          els.form.classList.add("dm-form--hidden");
          els.form.dataset.visible = "false";
          els.addBtn.textContent = "+";
        } else {
          if (els.id) els.id.value = "";
          if (els.ten) els.ten.value = "";
          els.form.dataset.mode = "create";
          els.form.classList.remove("dm-form--hidden");
          els.form.dataset.visible = "true";
          els.addBtn.textContent = "×";
        }
      });
    }
  }

  function init() {
    mapEls();
    bindEvents();
    loadCategories();
  }

  return { init };
})();

// Hàm global để admin.js gọi sau khi render trang Danh mục
window.initDanhMucPage = () => {
  DanhMucModule.init();
};

