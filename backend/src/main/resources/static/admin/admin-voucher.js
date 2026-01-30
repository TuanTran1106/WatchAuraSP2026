
window.initVoucherPage = function () {
    const content = document.getElementById("content");
    if (!content) return;
    // Tạo giao diện bảng và form voucher (đầy đủ trường)
    content.innerHTML = `
        <section class="card card--full">
            <div class="card__header-row">
                <h2 class="card__title"></h2>
                <input id="voucher-search" class="input input--sm input--search-wide" placeholder="Tìm kiếm voucher..." style="margin-right: 12px;" />
                <button id="voucher-add-btn" class="btn btn--primary btn--sm" title="Thêm voucher"><span style="font-size: 20px; font-weight: bold;">+</span></button>
            </div>
            <form id="voucher-form" class="kh-form kh-form--hidden" data-visible="false" style="margin-bottom: 24px;">
                <input type="hidden" id="voucher-id" />
                <div class="form-grid">
                    <div class="form-control"><label>Mã voucher</label><input id="voucher-maVoucher" type="text" required /></div>
                    <div class="form-control"><label>Tên voucher</label><input id="voucher-tenVoucher" type="text" required /></div>
                    <div class="form-control"><label>Mô tả</label><input id="voucher-moTa" type="text" /></div>
                    <div class="form-control"><label>Loại voucher</label>
                        <select id="voucher-loaiVoucher" required>
                            <option value="">--Chọn loại--</option>
                            <option value="PHAN_TRAM">Phần trăm (%)</option>
                            <option value="TIEN_MAT">Tiền mặt (VNĐ)</option>
                        </select>
                    </div>
                    <div class="form-control"><label>Giá trị</label><input id="voucher-giaTri" type="number" step="0.01" required /></div>
                    <div class="form-control"><label>Giá trị tối đa</label><input id="voucher-giaTriToiDa" type="number" step="0.01" /></div>
                    <div class="form-control"><label>Đơn hàng tối thiểu</label><input id="voucher-donHangToiThieu" type="number" step="0.01" /></div>
                    <div class="form-control"><label>Số lượng tổng</label><input id="voucher-soLuongTong" type="number" /></div>
                    <div class="form-control"><label>Ngày bắt đầu</label><input id="voucher-ngayBatDau" type="date" required /></div>
                    <div class="form-control"><label>Ngày kết thúc</label><input id="voucher-ngayKetThuc" type="date" required /></div>
                    <div class="form-control"><label>Trạng thái</label>
                        <select id="voucher-trangThai">
                            <option value="true">Đang hoạt động</option>
                            <option value="false">Ngưng hoạt động</option>
                        </select>
                    </div>
                </div>
                <div class="form-actions">
                    <button type="submit" class="btn btn--primary btn--sm">Lưu</button>
                    <button type="button" id="voucher-cancel-btn" class="btn btn--secondary btn--sm">Hủy</button>
                </div>
            </form>
            <div id="voucher-table-container"></div>
        </section>
    `;

    let vouchers = [];
    let filteredVouchers = [];

    function filterVouchers() {
        const keyword = document.getElementById("voucher-search").value.trim().toLowerCase();
        if (!keyword) {
            filteredVouchers = vouchers.slice();
        } else {
            filteredVouchers = vouchers.filter(v => {
                const str = `${v.maVoucher || ''} ${v.tenVoucher || ''} ${v.moTa || ''}`.toLowerCase();
                return str.includes(keyword);
            });
        }
    }

    async function fetchVouchers() {
        const res = await fetch("/api/voucher");
        vouchers = await res.json();
        filterVouchers();
        renderTable();
    }

    function renderTable() {
        const tableHtml = `
        <div class="table-wrapper">
            <table class="table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Mã</th>
                        <th>Tên</th>
                        <th>Mô tả</th>
                        <th>Loại</th>
                        <th>Giá trị</th>
                        <th>Giá trị tối đa</th>
                        <th>Đơn hàng tối thiểu</th>
                        <th>Số lượng tổng</th>
                        <th>Số lượng đã dùng</th>
                        <th>Ngày bắt đầu</th>
                        <th>Ngày kết thúc</th>
                        <th>Trạng thái</th>
                        <th>Hành động</th>
                    </tr>
                </thead>
                <tbody>
                    ${filteredVouchers.length === 0 ? `<tr><td colspan='14' style='text-align:center;'>Không có dữ liệu</td></tr>` : filteredVouchers.map(v => `
                        <tr>
                            <td>${v.id ?? ''}</td>
                            <td>${v.maVoucher ?? ''}</td>
                            <td>${v.tenVoucher ?? ''}</td>
                            <td>${v.moTa ?? ''}</td>
                            <td>${v.loaiVoucher ?? ''}</td>
                            <td>${v.giaTri ?? ''}</td>
                            <td>${v.giaTriToiDa ?? ''}</td>
                            <td>${v.donHangToiThieu ?? ''}</td>
                            <td>${v.soLuongTong ?? ''}</td>
                            <td>${v.soLuongDaDung ?? ''}</td>
                            <td>${v.ngayBatDau ? v.ngayBatDau.replace('T', ' ').slice(0, 16) : ''}</td>
                            <td>${v.ngayKetThuc ? v.ngayKetThuc.replace('T', ' ').slice(0, 16) : ''}</td>
                            <td>${v.trangThai === 'true' || v.trangThai === true ? 'Đang hoạt động' : (v.trangThai === 'false' || v.trangThai === false ? 'Ngưng hoạt động' : (v.trangThai ?? ''))}</td>
                            <td>
                                <button class="btn-link" data-id="${v.id}" data-action="edit">Sửa</button>
                                <button class="btn-link btn-link--danger" data-id="${v.id}" data-action="delete">Xóa</button>
                            </td>
                        </tr>
                    `).join("")}
                </tbody>
            </table>
        </div>
        `;
        document.getElementById("voucher-table-container").innerHTML = tableHtml;
    }

    // Sự kiện tìm kiếm
    document.getElementById("voucher-search").addEventListener("input", function () {
        filterVouchers();
        renderTable();
    });

    fetchVouchers();

    document.getElementById("voucher-add-btn").onclick = function () {
        const form = document.getElementById("voucher-form");
        if (form.dataset.visible === "true") {
            form.classList.add("kh-form--hidden");
            form.dataset.visible = "false";
        } else {
            form.reset();
            document.getElementById("voucher-id").value = "";
            form.classList.remove("kh-form--hidden");
            form.dataset.visible = "true";
        }
    };

    document.getElementById("voucher-cancel-btn").onclick = function () {
        document.getElementById("voucher-form").classList.add("kh-form--hidden");
        document.getElementById("voucher-form").dataset.visible = "false";
    };

    document.getElementById("voucher-table-container").onclick = async function (e) {
        if (e.target.dataset.action === "edit") {
            const id = e.target.dataset.id;
            const v = vouchers.find(x => x.id == id);
            if (v) {
                document.getElementById("voucher-id").value = v.id;
                document.getElementById("voucher-maVoucher").value = v.maVoucher ?? '';
                document.getElementById("voucher-tenVoucher").value = v.tenVoucher ?? '';
                document.getElementById("voucher-moTa").value = v.moTa ?? '';
                document.getElementById("voucher-loaiVoucher").value = v.loaiVoucher ?? '';
                document.getElementById("voucher-giaTri").value = v.giaTri ?? '';
                document.getElementById("voucher-giaTriToiDa").value = v.giaTriToiDa ?? '';
                document.getElementById("voucher-donHangToiThieu").value = v.donHangToiThieu ?? '';
                document.getElementById("voucher-soLuongTong").value = v.soLuongTong ?? '';
                // Bỏ trường số lượng đã dùng khỏi form
                document.getElementById("voucher-ngayBatDau").value = v.ngayBatDau ? v.ngayBatDau.slice(0, 10) : '';
                document.getElementById("voucher-ngayKetThuc").value = v.ngayKetThuc ? v.ngayKetThuc.slice(0, 10) : '';
                document.getElementById("voucher-trangThai").value = v.trangThai ?? '';
                document.getElementById("voucher-form").classList.remove("kh-form--hidden");
                document.getElementById("voucher-form").dataset.visible = "true";
            }
        }
        if (e.target.dataset.action === "delete") {
            const id = e.target.dataset.id;
            if (confirm("Bạn có chắc muốn xóa voucher này?")) {
                await fetch(`/api/voucher/${id}`, { method: "DELETE" });
                fetchVouchers();
            }
        }
    };

    document.getElementById("voucher-form").onsubmit = async function (e) {
        e.preventDefault();
        const id = document.getElementById("voucher-id").value;
        const isEdit = !!document.getElementById("voucher-id").value;
        const data = {
            maVoucher: document.getElementById("voucher-maVoucher").value,
            tenVoucher: document.getElementById("voucher-tenVoucher").value,
            moTa: document.getElementById("voucher-moTa").value,
            loaiVoucher: document.getElementById("voucher-loaiVoucher").value,
            giaTri: document.getElementById("voucher-giaTri").value,
            giaTriToiDa: document.getElementById("voucher-giaTriToiDa").value,
            donHangToiThieu: document.getElementById("voucher-donHangToiThieu").value,
            soLuongTong: document.getElementById("voucher-soLuongTong").value,
            ngayBatDau: (() => {
                const val = document.getElementById("voucher-ngayBatDau").value;
                return val ? (val.length === 10 ? val + 'T00:00:00' : val) : '';
            })(),
            ngayKetThuc: (() => {
                const val = document.getElementById("voucher-ngayKetThuc").value;
                return val ? (val.length === 10 ? val + 'T00:00:00' : val) : '';
            })(),
            trangThai: document.getElementById("voucher-trangThai").value
        };
        if (!isEdit) {
            data.soLuongDaDung = 0;
        }
        if (id) {
            await fetch(`/api/voucher/${id}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(data)
            });
        } else {
            await fetch(`/api/voucher`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(data)
            });
        }
        fetchVouchers();
        document.getElementById("voucher-form").classList.add("kh-form--hidden");
        document.getElementById("voucher-form").dataset.visible = "false";
    };
};
