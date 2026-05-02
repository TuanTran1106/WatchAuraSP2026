/**
 * Premium address list renderer (saved addresses).
 */
export function createAddressListComponent(options) {
    const root = options?.root;
    const onSelect = options?.onSelect || function () {};
    const onSetDefault = options?.onSetDefault || function () {};
    const onEdit = options?.onEdit || function () {};
    const onDelete = options?.onDelete || function () {};

    if (!root) throw new Error("AddressList requires root element.");

    function render(addresses, selectedId) {
        const items = Array.isArray(addresses) ? addresses : [];
        if (!items.length) {
            root.innerHTML = '<div class="wa-address-empty">Chưa có địa chỉ nào.</div>';
            return;
        }

        root.innerHTML = items.map((dc) => {
            const id = Number(dc.id || 0);
            const selected = selectedId && Number(selectedId) === id;
            const isDefault = !!dc.macDinh;
            const fullAddress = [dc.diaChiCuThe, dc.phuongXa, dc.quanHuyen, dc.tinhThanh]
                .filter(Boolean)
                .join(", ");

            return `
                <article class="wa-address-card ${selected ? "is-selected" : ""}" data-address-id="${id}">
                    <header class="wa-address-card__header">
                        <label class="wa-address-card__pick">
                            <input type="radio" name="wa-address-select" value="${id}" ${selected ? "checked" : ""}>
                            <span>${escapeHtml(dc.tenNguoiNhan || "Người nhận")}</span>
                        </label>
                        ${isDefault ? '<span class="wa-address-badge">Mặc định</span>' : ""}
                    </header>
                    <p class="wa-address-card__line">${escapeHtml(fullAddress || "Địa chỉ không đầy đủ")}</p>
                    <p class="wa-address-card__meta">${escapeHtml(dc.sdtNguoiNhan || "")}</p>
                    <footer class="wa-address-card__actions">
                        <button type="button" class="wa-icon-btn" data-action="default" title="Đặt mặc định">
                            <i class="fas fa-check"></i>
                        </button>
                        <button type="button" class="wa-icon-btn" data-action="edit" title="Sửa địa chỉ">
                            <i class="fas fa-pen"></i>
                        </button>
                        <button type="button" class="wa-icon-btn wa-icon-btn--danger" data-action="delete" title="Xóa địa chỉ">
                            <i class="fas fa-trash"></i>
                        </button>
                    </footer>
                </article>
            `;
        }).join("");
        bindEvents();
    }

    function bindEvents() {
        root.querySelectorAll('input[name="wa-address-select"]').forEach((input) => {
            input.addEventListener("change", function () {
                onSelect(Number(this.value));
            });
        });

        root.querySelectorAll(".wa-address-card").forEach((card) => {
            const id = Number(card.getAttribute("data-address-id"));
            const btnDefault = card.querySelector('[data-action="default"]');
            const btnEdit = card.querySelector('[data-action="edit"]');
            const btnDelete = card.querySelector('[data-action="delete"]');

            if (btnDefault) btnDefault.addEventListener("click", () => onSetDefault(id));
            if (btnEdit) btnEdit.addEventListener("click", () => onEdit(id));
            if (btnDelete) btnDelete.addEventListener("click", () => onDelete(id));
        });
    }

    return { render };
}

function escapeHtml(value) {
    const s = String(value ?? "");
    return s
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
}
