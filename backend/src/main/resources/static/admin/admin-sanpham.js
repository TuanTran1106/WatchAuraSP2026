const SanPhamModule = (() => {
  const API_BASE = "/api/san-pham";
  const CATEGORY_API = "/api/danh-muc";
  const SPCT_API = "/api/san-pham-chi-tiet";
  const MAU_SAC_API = "/api/mau-sac";
  const KICH_THUOC_API = "/api/kich-thuoc";
  const CHAT_LIEU_DAY_API = "/api/chat-lieu-day";
  const LOAI_MAY_API = "/api/loai-may";

  let products = [];
  let categories = [];
  let mauSacs = [];
  let kichThuocs = [];
  let chatLieuDays = [];
  let loaiMays = [];
  let currentSpctId = null; // Lưu ID sản phẩm chi tiết hiện tại khi edit

  const els = {};

  function qs(id) {
    return document.getElementById(id);
  }

  function mapEls() {
    els.search = qs("sp-search");
    els.categoryFilter = qs("sp-category-filter");
    els.addBtn = qs("sp-add-btn");
    els.tableBody = qs("sp-table-body");
    els.form = qs("sp-form");
    els.id = qs("sp-id");
    els.ma = qs("sp-ma");
    els.ten = qs("sp-ten");
    els.giaBan = qs("sp-giaban");
    els.soLuongTon = qs("sp-soluongton");
    els.danhMuc = qs("sp-danhmuc");
    els.mauSac = qs("sp-mausac");
    els.kichThuoc = qs("sp-kichthuoc");
    els.chatLieuDay = qs("sp-chatlieuday");
    els.loaiMay = qs("sp-loaimay");
    els.phongCach = qs("sp-phongcach");
    els.moTa = qs("sp-mota");
    els.duongKinh = qs("sp-duongkinh");
    els.doChiuNuoc = qs("sp-dochiunuoc");
    els.beRongDay = qs("sp-berongday");
    els.trongLuong = qs("sp-trongluong");
    els.trangThai = qs("sp-trangthai");
    els.error = qs("sp-error");
    els.saveBtn = qs("sp-save-btn");
    els.resetBtn = qs("sp-reset-btn");
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

  function renderCategoryOptions() {
    const opts = categories.map((dm) => `<option value="${dm.id}">${dm.tenDanhMuc}</option>`);

    if (els.categoryFilter) {
      const filterOptions = ['<option value="all">Tất cả danh mục</option>', ...opts];
      els.categoryFilter.innerHTML = filterOptions.join("");
    }

    if (els.danhMuc) {
      const formOptions = ['<option value="">-- Chọn danh mục --</option>', ...opts];
      els.danhMuc.innerHTML = formOptions.join("");
    }
  }

  function renderOptionList(selectEl, list, getId, getLabel, placeholder = "-- Chọn --") {
    if (!selectEl) return;
    const opts = [
      `<option value="">${placeholder}</option>`,
      ...list.map((x) => `<option value="${getId(x)}">${getLabel(x)}</option>`)
    ];
    selectEl.innerHTML = opts.join("");
  }

  function renderSpctOptions() {
    renderOptionList(els.mauSac, mauSacs, (x) => x.id, (x) => x.tenMauSac, "-- Chọn màu --");
    renderOptionList(els.kichThuoc, kichThuocs, (x) => x.id, (x) => x.tenKichThuoc, "-- Chọn kích thước --");
    renderOptionList(els.chatLieuDay, chatLieuDays, (x) => x.id, (x) => x.tenChatLieu, "-- Chọn chất liệu --");
    renderOptionList(els.loaiMay, loaiMays, (x) => x.id, (x) => x.tenLoaiMay, "-- Chọn loại máy --");
  }

  function renderTable(list) {
    if (!els.tableBody) return;

    if (!list || list.length === 0) {
      els.tableBody.innerHTML =
        '<tr><td colspan="7" style="text-align:center; padding: 16px;">Không có dữ liệu</td></tr>';
      return;
    }

    const rows = list
      .map((sp) => {
        const danhMuc = sp.tenDanhMuc || "";
        const thuongHieu = sp.tenThuongHieu || "";
        const phongCach = sp.phongCach || "";
        const isActive = !!sp.trangThai;
        return `
          <tr data-id="${sp.id}">
            <td>${sp.maSanPham || ""}</td>
            <td>${sp.tenSanPham || ""}</td>
            <td>${danhMuc}</td>
            <td>${thuongHieu}</td>
            <td>${phongCach}</td>
            <td>
              <span class="badge ${isActive ? "badge--success" : "badge--warning"}">
                ${isActive ? "Đang bán" : "Ngưng"}
              </span>
            </td>
            <td>
              <button class="btn-link" data-action="edit">Sửa</button>
              <button class="btn-link btn-link--danger" data-action="delete">Xóa</button>
            </td>
          </tr>
        `;
      })
      .join("");

    els.tableBody.innerHTML = rows;
  }

  async function loadCategories() {
    try {
      categories = await fetchJSON(CATEGORY_API);
      renderCategoryOptions();
    } catch (e) {
      console.error(e);
    }
  }

  async function loadSpctDictionaries() {
    try {
      const [ms, kt, cld, lm] = await Promise.all([
        fetchJSON(MAU_SAC_API),
        fetchJSON(KICH_THUOC_API),
        fetchJSON(CHAT_LIEU_DAY_API),
        fetchJSON(LOAI_MAY_API)
      ]);
      mauSacs = Array.isArray(ms) ? ms : [];
      kichThuocs = Array.isArray(kt) ? kt : [];
      chatLieuDays = Array.isArray(cld) ? cld : [];
      loaiMays = Array.isArray(lm) ? lm : [];
      renderSpctOptions();
    } catch (e) {
      console.error(e);
    }
  }

  async function loadProducts() {
    if (!els.tableBody) return;
    els.tableBody.innerHTML =
      '<tr><td colspan="7" style="text-align:center; padding: 16px;">Đang tải dữ liệu...</td></tr>';
    try {
      products = await fetchJSON(API_BASE);
      applyFilter();
    } catch (e) {
      console.error(e);
      els.tableBody.innerHTML =
        '<tr><td colspan="7" style="text-align:center; padding: 16px;">Lỗi tải dữ liệu</td></tr>';
    }
  }

  function applyFilter() {
    if (!els.search) {
      renderTable(products);
      return;
    }
    const keyword = els.search.value.trim().toLowerCase();
    const categoryId = els.categoryFilter ? els.categoryFilter.value : "all";

    const filtered = products.filter((sp) => {
      const str = `${sp.maSanPham || ""} ${sp.tenSanPham || ""}`.toLowerCase();
      const matchKeyword = !keyword || str.includes(keyword);

      let matchCategory = true;
      if (categoryId !== "all") {
        matchCategory = String(sp.idDanhMuc) === String(categoryId);
      }

      return matchKeyword && matchCategory;
    });

    renderTable(filtered);
  }

  async function loadSpctForProduct(sanPhamId) {
    try {
      const spctList = await fetchJSON(`${SPCT_API}/san-pham/${sanPhamId}`);
      if (spctList && spctList.length > 0) {
        // Lấy sản phẩm chi tiết đầu tiên
        const spct = spctList[0];
        currentSpctId = spct.id;
        if (els.giaBan && spct.giaBan != null) els.giaBan.value = spct.giaBan;
        if (els.soLuongTon && spct.soLuongTon != null) els.soLuongTon.value = spct.soLuongTon;
        if (els.mauSac) els.mauSac.value = spct.idMauSac || "";
        if (els.kichThuoc) els.kichThuoc.value = spct.idKichThuoc || "";
        if (els.chatLieuDay) els.chatLieuDay.value = spct.idChatLieuDay || "";
        if (els.loaiMay) els.loaiMay.value = spct.idLoaiMay || "";
        if (els.duongKinh && spct.duongKinh != null) els.duongKinh.value = spct.duongKinh;
        if (els.doChiuNuoc && spct.doChiuNuoc != null) els.doChiuNuoc.value = spct.doChiuNuoc;
        if (els.beRongDay && spct.beRongDay != null) els.beRongDay.value = spct.beRongDay;
        if (els.trongLuong && spct.trongLuong != null) els.trongLuong.value = spct.trongLuong;
      } else {
        currentSpctId = null;
      }
    } catch (e) {
      console.error("Không thể load sản phẩm chi tiết:", e);
      currentSpctId = null;
    }
  }

  async function fillForm(mode, sp = null) {
    if (!els.form) return;
    if (els.error) {
      els.error.hidden = true;
      els.error.textContent = "";
    }

    if (mode === "create") {
      currentSpctId = null;
      if (els.id) els.id.value = "";
      if (els.ma) els.ma.value = "";
      if (els.ten) els.ten.value = "";
      if (els.giaBan) els.giaBan.value = "";
      if (els.soLuongTon) els.soLuongTon.value = "0";
      if (els.danhMuc) els.danhMuc.value = "";
      if (els.mauSac) els.mauSac.value = "";
      if (els.kichThuoc) els.kichThuoc.value = "";
      if (els.chatLieuDay) els.chatLieuDay.value = "";
      if (els.loaiMay) els.loaiMay.value = "";
      if (els.phongCach) els.phongCach.value = "";
      if (els.moTa) els.moTa.value = "";
      if (els.duongKinh) els.duongKinh.value = "";
      if (els.doChiuNuoc) els.doChiuNuoc.value = "";
      if (els.beRongDay) els.beRongDay.value = "";
      if (els.trongLuong) els.trongLuong.value = "";
      if (els.trangThai) els.trangThai.value = "true";
    } else if (mode === "edit" && sp) {
      if (els.id) els.id.value = sp.id || "";
      if (els.ma) els.ma.value = sp.maSanPham || "";
      if (els.ten) els.ten.value = sp.tenSanPham || "";
      if (els.danhMuc) els.danhMuc.value = sp.idDanhMuc || "";
      if (els.phongCach) els.phongCach.value = sp.phongCach || "";
      if (els.moTa) els.moTa.value = sp.moTa || "";
      if (els.trangThai) els.trangThai.value = String(sp.trangThai || true);
      // Load giá bán từ sản phẩm chi tiết
      if (sp.id) {
        await loadSpctForProduct(sp.id);
      }
    }

    els.form.dataset.mode = mode;

    // luôn mở form khi thao tác
    if (els.form.classList.contains("sp-form--hidden")) {
      els.form.classList.remove("sp-form--hidden");
      els.form.dataset.visible = "true";
      if (els.addBtn) els.addBtn.textContent = "×";
    }
  }

  async function saveSanPhamChiTiet(sanPhamId, giaBan) {
    if (giaBan == null || Number.isNaN(giaBan) || giaBan <= 0) return; // Bỏ qua nếu không có giá bán

    const spctPayload = {
      idSanPham: sanPhamId,
      idMauSac: els.mauSac && els.mauSac.value ? parseInt(els.mauSac.value, 10) : null,
      idKichThuoc: els.kichThuoc && els.kichThuoc.value ? parseInt(els.kichThuoc.value, 10) : null,
      idChatLieuDay: els.chatLieuDay && els.chatLieuDay.value ? parseInt(els.chatLieuDay.value, 10) : null,
      idLoaiMay: els.loaiMay && els.loaiMay.value ? parseInt(els.loaiMay.value, 10) : null,
      soLuongTon: els.soLuongTon && els.soLuongTon.value ? parseInt(els.soLuongTon.value, 10) : 0,
      giaBan: giaBan,
      duongKinh: els.duongKinh && els.duongKinh.value ? parseFloat(els.duongKinh.value) : null,
      doChiuNuoc: els.doChiuNuoc && els.doChiuNuoc.value ? parseInt(els.doChiuNuoc.value, 10) : null,
      beRongDay: els.beRongDay && els.beRongDay.value ? parseFloat(els.beRongDay.value) : null,
      trongLuong: els.trongLuong && els.trongLuong.value ? parseFloat(els.trongLuong.value) : null,
      trangThai: els.trangThai ? els.trangThai.value === "true" : true
    };

    try {
      if (currentSpctId) {
        // Cập nhật sản phẩm chi tiết đã có
        await fetchJSON(`${SPCT_API}/${currentSpctId}`, {
          method: "PUT",
          body: JSON.stringify(spctPayload)
        });
      } else {
        // Tạo mới sản phẩm chi tiết
        await fetchJSON(SPCT_API, {
          method: "POST",
          body: JSON.stringify(spctPayload)
        });
      }
    } catch (e) {
      console.error("Lỗi khi lưu sản phẩm chi tiết:", e);
      throw new Error("Đã lưu sản phẩm nhưng không thể lưu giá bán: " + (e.message || "Lỗi hệ thống"));
    }
  }

  async function handleDelete(id) {
    if (!id) return;
    const confirmDelete = window.confirm("Bạn có chắc muốn xóa sản phẩm này?");
    if (!confirmDelete) return;

    try {
      await fetchJSON(`${API_BASE}/${id}`, { method: "DELETE" });
      await loadProducts();
    } catch (e) {
      console.error(e);
      window.alert("Không thể xóa sản phẩm: " + (e.message || "Lỗi hệ thống"));
    }
  }

  async function handleSave() {
    if (!els.form) return;
    if (!els.form.checkValidity()) {
      els.form.reportValidity();
      return;
    }

    const idDanhMuc = els.danhMuc ? parseInt(els.danhMuc.value, 10) : null;
    const giaBan = els.giaBan ? parseFloat(els.giaBan.value) : null;

    const payload = {
      maSanPham: els.ma ? els.ma.value.trim() : "",
      tenSanPham: els.ten ? els.ten.value.trim() : "",
      moTa: els.moTa ? els.moTa.value.trim() || null : null,
      hinhAnh: null,
      idThuongHieu: 1, // Giá trị mặc định vì backend yêu cầu
      idDanhMuc: idDanhMuc,
      phongCach: els.phongCach ? els.phongCach.value.trim() || null : null,
      trangThai: els.trangThai ? els.trangThai.value === "true" : true
    };

    const mode = els.form.dataset.mode || "create";
    let url = API_BASE;
    let method = "POST";

    if (mode === "edit" && els.id && els.id.value) {
      url = `${API_BASE}/${els.id.value}`;
      method = "PUT";
    }

    try {
      // Lưu sản phẩm trước
      const savedSp = await fetchJSON(url, {
        method,
        body: JSON.stringify(payload)
      });

      // Sau đó lưu sản phẩm chi tiết với giá bán (nếu có)
      if (giaBan && giaBan > 0 && savedSp && savedSp.id) {
        await saveSanPhamChiTiet(savedSp.id, giaBan);
      }

      await fillForm("create");
      await loadProducts();
    } catch (e) {
      console.error(e);
      if (els.error) {
        els.error.hidden = false;
        els.error.textContent = e.message || "Không thể lưu sản phẩm";
      }
    }
  }

  function bindEvents() {
    if (els.search) {
      els.search.addEventListener("input", () => applyFilter());
    }

    if (els.categoryFilter) {
      els.categoryFilter.addEventListener("change", () => applyFilter());
    }

    if (els.tableBody) {
      els.tableBody.addEventListener("click", (e) => {
        const btn = e.target.closest("button[data-action]");
        if (!btn) return;
        const action = btn.dataset.action;
        const tr = btn.closest("tr[data-id]");
        const id = tr ? tr.dataset.id : null;

        if (action === "edit" && id) {
          // Tải lại sản phẩm từ API để đảm bảo có dữ liệu mới nhất
          fetchJSON(`${API_BASE}/${id}`)
            .then((sp) => {
              if (!sp) {
                window.alert("Không tìm thấy sản phẩm với ID: " + id);
                return;
              }
              return fillForm("edit", sp);
            })
            .catch((err) => {
              console.error("Lỗi khi tải sản phẩm:", err);
              // Fallback: tìm trong danh sách hiện tại
              const sp = products.find((x) => String(x.id) === String(id));
              if (sp) {
                fillForm("edit", sp).catch((e) => console.error("Lỗi khi fill form:", e));
              } else {
                window.alert("Không thể tải dữ liệu sản phẩm: " + (err.message || "Lỗi hệ thống"));
              }
            });
        } else if (action === "delete" && id) {
          handleDelete(id);
        } else if (action === "view" && id) {
          const sp = products.find((x) => String(x.id) === String(id));
          if (!sp) return;
          fillForm("edit", sp).catch((err) => {
            console.error("Lỗi khi fill form:", err);
          });
        }
      });
    }

    if (els.form && els.saveBtn) {
      els.form.addEventListener("submit", (e) => {
        e.preventDefault();
        handleSave();
      });
    }

    if (els.resetBtn) {
      els.resetBtn.addEventListener("click", (e) => {
        e.preventDefault();
        fillForm("create").catch((err) => console.error(err));
      });
    }

    if (els.addBtn && els.form) {
      els.addBtn.addEventListener("click", () => {
        const isVisible = els.form.dataset.visible === "true";
        if (isVisible) {
          els.form.classList.add("sp-form--hidden");
          els.form.dataset.visible = "false";
          els.addBtn.textContent = "+";
        } else {
          fillForm("create");
          els.form.classList.remove("sp-form--hidden");
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
    loadSpctDictionaries();
    loadProducts();
  }

  return { init };
})();

// Hàm global để admin.js gọi sau khi render trang Products
window.initSanPhamPage = () => {
  SanPhamModule.init();
};

