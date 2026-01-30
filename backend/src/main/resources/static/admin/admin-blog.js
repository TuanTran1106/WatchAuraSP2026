
window.initBlogPage = function () {
    const content = document.getElementById("content");
    if (!content) return;
    // Tạo giao diện bảng và form blog (đầy đủ trường)
    content.innerHTML = `
        <section class="card card--full">
            <div class="card__header-row">
                <h2 class="card__title"></h2>
                <input id="blog-search" class="input input--sm input--search-wide" placeholder="Tìm kiếm blog..." style="margin-right: 12px;" />
                <button id="blog-add-btn" class="btn btn--primary btn--sm" title="Thêm blog"><span style="font-size: 20px; font-weight: bold;">+</span></button>
            </div>
            <form id="blog-form" class="kh-form kh-form--hidden" data-visible="false" style="margin-bottom: 24px;">
                <input type="hidden" id="blog-id" />
                <div class="form-grid">
                    <div class="form-control"><label>Tiêu đề</label><input id="blog-tieuDe" type="text" required /></div>
                    <div class="form-control"><label>Nội dung</label><textarea id="blog-noiDung" required></textarea></div>
                    <div class="form-control"><label>Hình ảnh</label><input id="blog-hinhAnh" type="text" /></div>
                    <div class="form-control"><label>Ngày đăng</label><input id="blog-ngayDang" type="date" required /></div>
                </div>
                <div class="form-actions">
                    <button type="submit" class="btn btn--primary btn--sm">Lưu</button>
                    <button type="button" id="blog-cancel-btn" class="btn btn--secondary btn--sm">Hủy</button>
                </div>
            </form>
            <div id="blog-table-container"></div>
        </section>
    `;

    let blogs = [];
    let filteredBlogs = [];

    function filterBlogs() {
        const keyword = document.getElementById("blog-search").value.trim().toLowerCase();
        if (!keyword) {
            filteredBlogs = blogs.slice();
        } else {
            filteredBlogs = blogs.filter(b => {
                const str = `${b.tieuDe || ''} ${b.noiDung || ''} ${b.hinhAnh || ''}`.toLowerCase();
                return str.includes(keyword);
            });
        }
    }

    async function fetchBlogs() {
        const res = await fetch("/api/blog");
        blogs = await res.json();
        filterBlogs();
        renderTable();
    }

    function renderTable() {
        const tableHtml = `
            <table class="table">
                <thead>
                    <tr>
                        <th>ID</th><th>Tiêu đề</th><th>Nội dung</th><th>Hình ảnh</th><th>Ngày đăng</th><th>Hành động</th>
                    </tr>
                </thead>
                <tbody>
                    ${filteredBlogs.length === 0 ? `<tr><td colspan='6' style='text-align:center;'>Không có dữ liệu</td></tr>` : filteredBlogs.map(b => `
                        <tr>
                            <td>${b.id ?? ''}</td>
                            <td>${b.tieuDe ?? ''}</td>
                            <td>${b.noiDung ?? ''}</td>
                            <td>${b.hinhAnh ?? ''}</td>
                            <td>${b.ngayDang ? b.ngayDang.replace('T', ' ').slice(0, 10) : ''}</td>
                            <td>
                                <button class="btn-link" data-id="${b.id}" data-action="edit">Sửa</button>
                                <button class="btn-link btn-link--danger" data-id="${b.id}" data-action="delete">Xóa</button>
                            </td>
                        </tr>
                    `).join("")}
                </tbody>
            </table>
        `;
        document.getElementById("blog-table-container").innerHTML = tableHtml;
    }

    // Sự kiện tìm kiếm
    document.getElementById("blog-search").addEventListener("input", function () {
        filterBlogs();
        renderTable();
    });

    fetchBlogs();

    document.getElementById("blog-add-btn").onclick = function () {
        const form = document.getElementById("blog-form");
        if (form.dataset.visible === "true") {
            form.classList.add("kh-form--hidden");
            form.dataset.visible = "false";
        } else {
            form.reset();
            document.getElementById("blog-id").value = "";
            form.classList.remove("kh-form--hidden");
            form.dataset.visible = "true";
        }
    };

    document.getElementById("blog-cancel-btn").onclick = function () {
        document.getElementById("blog-form").classList.add("kh-form--hidden");
        document.getElementById("blog-form").dataset.visible = "false";
    };

    document.getElementById("blog-table-container").onclick = async function (e) {
        if (e.target.dataset.action === "edit") {
            const id = e.target.dataset.id;
            const b = blogs.find(x => x.id == id);
            if (b) {
                document.getElementById("blog-id").value = b.id;
                document.getElementById("blog-tieuDe").value = b.tieuDe ?? '';
                document.getElementById("blog-noiDung").value = b.noiDung ?? '';
                document.getElementById("blog-hinhAnh").value = b.hinhAnh ?? '';
                document.getElementById("blog-ngayDang").value = b.ngayDang ? b.ngayDang.slice(0, 10) : '';
                document.getElementById("blog-form").classList.remove("kh-form--hidden");
                document.getElementById("blog-form").dataset.visible = "true";
            }
        }
        if (e.target.dataset.action === "delete") {
            const id = e.target.dataset.id;
            if (confirm("Bạn có chắc muốn xóa blog này?")) {
                await fetch(`/api/blog/${id}`, { method: "DELETE" });
                fetchBlogs();
            }
        }
    };

    document.getElementById("blog-form").onsubmit = async function (e) {
        e.preventDefault();
        const id = document.getElementById("blog-id").value;
        const data = {
            tieuDe: document.getElementById("blog-tieuDe").value,
            noiDung: document.getElementById("blog-noiDung").value,
            hinhAnh: document.getElementById("blog-hinhAnh").value,
            ngayDang: (() => {
                const val = document.getElementById("blog-ngayDang").value;
                return val ? (val.length === 10 ? val + 'T00:00:00' : val) : '';
            })()
        };
        if (id) {
            await fetch(`/api/blog/${id}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(data)
            });
        } else {
            await fetch(`/api/blog`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(data)
            });
        }
        fetchBlogs();
        document.getElementById("blog-form").classList.add("kh-form--hidden");
        document.getElementById("blog-form").dataset.visible = "false";
    };
};
