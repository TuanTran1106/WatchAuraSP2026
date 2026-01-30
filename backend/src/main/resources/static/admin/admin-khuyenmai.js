
window.initKhuyenMaiPage = function () {
    const content = document.getElementById("content");
    if (!content) return;
    // Tạo giao diện bảng và form khuyến mãi (đầy đủ trường)
    content.innerHTML = `
        <section class="card card--full">
            <div class="card__header-row">
                <h2 class="card__title"></h2>
                <input id="promotion-search" class="input input--sm input--search-wide" placeholder="Tìm kiếm khuyến mãi..." style="margin-right: 12px;" />
             <button id="promotion-add-btn" class="btn btn--primary btn--sm btn-toggle-form" title="Thêm Khuyến Mãi">
                    <span>+</span>
                   </button>
            
            </div>
            <form id="promotion-form" class="kh-form kh-form--hidden" data-visible="false" style="margin-bottom: 24px;">
                <input type="hidden" id="promotion-id" />
                <div class="form-grid">
                    <div class="form-control"><label>Mã khuyến mãi</label><input id="promotion-maKhuyenMai" type="text" required /></div>
                    <div class="form-control"><label>Tên chương trình</label><input id="promotion-tenChuongTrinh" type="text" required /></div>
                    <div class="form-control"><label>Mô tả</label><input id="promotion-moTa" type="text" /></div>
                    <div class="form-control"><label>Loại giảm</label>
                        <select id="promotion-loaiGiam" required>
                            <option value="">--Chọn loại giảm--</option>
                            <option value="PHAN_TRAM">Phần trăm (%)</option>
                            <option value="TIEN_MAT">Tiền mặt (VNĐ)</option>
                        </select>
                    </div>
                    <div class="form-control"><label>Giá trị giảm</label><input id="promotion-giaTriGiam" type="number" step="0.01" required /></div>
                    <div class="form-control"><label>Giảm tối đa</label><input id="promotion-giamToiDa" type="number" step="0.01" /></div>
                    <div class="form-control"><label>Ngày bắt đầu</label><input id="promotion-ngayBatDau" type="date" required /></div>
                    <div class="form-control"><label>Ngày kết thúc</label><input id="promotion-ngayKetThuc" type="date" required /></div>
                    <div class="form-control"><label>Trạng thái</label>
                        <select id="promotion-trangThai">
                            <option value="true">Đang hoạt động</option>
                            <option value="false">Ngưng hoạt động</option>
                        </select>
                    </div>
                </div>
                <div class="form-actions">
                    <button type="submit" class="btn btn--primary btn--sm">Lưu</button>
                    <button type="button" id="promotion-cancel-btn" class="btn btn--secondary btn--sm">Hủy</button>
                </div>
            </form>
            <div id="promotion-table-container"></div>
        </section>
    `;

    let promotions = [];
    let filteredPromotions = [];

    function filterPromotions() {
        const keyword = document.getElementById("promotion-search").value.trim().toLowerCase();
        if (!keyword) {
            filteredPromotions = promotions.slice();
        } else {
            filteredPromotions = promotions.filter(p => {
                const str = `${p.maKhuyenMai || ''} ${p.tenChuongTrinh || ''} ${p.moTa || ''}`.toLowerCase();
                return str.includes(keyword);
            });
        }
    }

    async function fetchPromotions() {
        const res = await fetch("/api/khuyen-mai");
        promotions = await res.json();
        filterPromotions();
        renderTable();
    }

    function renderTable() {
        const tableHtml = `
            <table class="table">
                <thead>
                    <tr>
                        <th>ID</th><th>Mã</th><th>Tên chương trình</th><th>Mô tả</th><th>Loại giảm</th><th>Giá trị giảm</th><th>Giảm tối đa</th><th>Ngày bắt đầu</th><th>Ngày kết thúc</th><th>Trạng thái</th><th>Ngày tạo</th><th>Ngày cập nhật</th><th>Hành động</th>
                    </tr>
                </thead>
                <tbody>
                    ${filteredPromotions.length === 0 ? `<tr><td colspan='13' style='text-align:center;'>Không có dữ liệu</td></tr>` : filteredPromotions.map(p => `
                        <tr>
                            <td>${p.id ?? ''}</td>
                            <td>${p.maKhuyenMai ?? ''}</td>
                            <td>${p.tenChuongTrinh ?? ''}</td>
                            <td>${p.moTa ?? ''}</td>
                            <td>${p.loaiGiam ?? ''}</td>
                            <td>${p.giaTriGiam ?? ''}</td>
                            <td>${p.giamToiDa ?? ''}</td>
                            <td>${p.ngayBatDau ? p.ngayBatDau.replace('T', ' ').slice(0, 16) : ''}</td>
                            <td>${p.ngayKetThuc ? p.ngayKetThuc.replace('T', ' ').slice(0, 16) : ''}</td>
                            <td>${p.trangThai === 'true' || p.trangThai === true ? 'Đang hoạt động' : (p.trangThai === 'false' || p.trangThai === false ? 'Ngưng hoạt động' : (p.trangThai ?? ''))}</td>
                            <td>${p.ngayTao ? p.ngayTao.replace('T', ' ').slice(0, 16) : ''}</td>
                            <td>${p.ngayCapNhat ? p.ngayCapNhat.replace('T', ' ').slice(0, 16) : ''}</td>
                            <td>
                                <button class="btn-link" data-id="${p.id}" data-action="edit">Sửa</button>
                                <button class="btn-link btn-link--danger" data-id="${p.id}" data-action="delete">Xóa</button>
                            </td>
                        </tr>
                    `).join("")}
                </tbody>
            </table>
        `;
        document.getElementById("promotion-table-container").innerHTML = tableHtml;
    }

    // Sự kiện tìm kiếm
    document.getElementById("promotion-search").addEventListener("input", function () {
        filterPromotions();
        renderTable();
    });

    fetchPromotions();

    document.getElementById("promotion-add-btn").onclick = function () {
        const form = document.getElementById("promotion-form");
        const btn = document.getElementById("promotion-add-btn");
        const icon = btn.querySelector("span");

        const isVisible = form.dataset.visible === "true";

        if (isVisible) {
            // 👉 ĐANG MỞ → ĐÓNG
            form.classList.add("kh-form--hidden");
            form.dataset.visible = "false";
            icon.textContent = "+";
        } else {
            // 👉 ĐANG ĐÓNG → MỞ (THÊM MỚI)
            form.reset();
            document.getElementById("promotion-id").value = "";
            form.classList.remove("kh-form--hidden");
            form.dataset.visible = "true";
            icon.textContent = "×";
        }
    };



    document.getElementById("promotion-cancel-btn").onclick = function () {
        document.getElementById("promotion-form").classList.add("kh-form--hidden");
        document.getElementById("promotion-form").dataset.visible = "false";
    };

    document.getElementById("promotion-table-container").onclick = async function (e) {
        if (e.target.dataset.action === "edit") {
            const id = e.target.dataset.id;
            const p = promotions.find(x => x.id == id);
            if (p) {
                document.getElementById("promotion-id").value = p.id;
                document.getElementById("promotion-maKhuyenMai").value = p.maKhuyenMai ?? '';
                document.getElementById("promotion-tenChuongTrinh").value = p.tenChuongTrinh ?? '';
                document.getElementById("promotion-moTa").value = p.moTa ?? '';
                document.getElementById("promotion-loaiGiam").value = p.loaiGiam ?? '';
                document.getElementById("promotion-giaTriGiam").value = p.giaTriGiam ?? '';
                document.getElementById("promotion-giamToiDa").value = p.giamToiDa ?? '';
                document.getElementById("promotion-ngayBatDau").value = p.ngayBatDau ? p.ngayBatDau.slice(0, 10) : '';
                document.getElementById("promotion-ngayKetThuc").value = p.ngayKetThuc ? p.ngayKetThuc.slice(0, 10) : '';
                document.getElementById("promotion-trangThai").value = p.trangThai ?? '';
                document.getElementById("promotion-form").classList.remove("kh-form--hidden");
                document.getElementById("promotion-form").dataset.visible = "true";
            }
        }
        if (e.target.dataset.action === "delete") {
            const id = e.target.dataset.id;
            if (confirm("Bạn có chắc muốn xóa khuyến mãi này?")) {
                await fetch(`/api/khuyen-mai/${id}`, { method: "DELETE" });
                fetchPromotions();
            }
        }
    };

    document.getElementById("promotion-form").onsubmit = async function (e) {
        e.preventDefault();
        const id = document.getElementById("promotion-id").value;
        const ngayBatDau = document.getElementById("promotion-ngayBatDau").value;
        const ngayKetThuc = document.getElementById("promotion-ngayKetThuc").value;
        const data = {
            maKhuyenMai: document.getElementById("promotion-maKhuyenMai").value,
            tenChuongTrinh: document.getElementById("promotion-tenChuongTrinh").value,
            moTa: document.getElementById("promotion-moTa").value,
            loaiGiam: document.getElementById("promotion-loaiGiam").value,
            giaTriGiam: document.getElementById("promotion-giaTriGiam").value,
            giamToiDa: document.getElementById("promotion-giamToiDa").value,
            ngayBatDau: ngayBatDau ? (ngayBatDau.length === 10 ? ngayBatDau + 'T00:00:00' : ngayBatDau) : '',
            ngayKetThuc: ngayKetThuc ? (ngayKetThuc.length === 10 ? ngayKetThuc + 'T00:00:00' : ngayKetThuc) : '',
            trangThai: document.getElementById("promotion-trangThai").value
        };
        if (id) {
            await fetch(`/api/khuyen-mai/${id}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(data)
            });
        } else {
            await fetch(`/api/khuyen-mai`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(data)
            });
        }
        fetchPromotions();
        document.getElementById("promotion-form").classList.add("kh-form--hidden");
        document.getElementById("promotion-form").dataset.visible = "false";
    };
};
