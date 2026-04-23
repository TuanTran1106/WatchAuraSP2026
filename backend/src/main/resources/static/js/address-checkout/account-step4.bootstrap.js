import { createAddressBookStore } from "./stores.js";
import { createAddressListComponent } from "./components/AddressList.js";
import { createAddressFormComponent } from "./components/AddressForm.js";

export async function bootstrapAccountAddressBook() {
    const listRoot = document.getElementById("waAddressListRoot");
    const formRoot = document.getElementById("waAddressFormRoot");
    const btnAdd = document.getElementById("waBtnOpenAddressForm");
    if (!listRoot || !formRoot || !btnAdd) return null;

    const store = createAddressBookStore();
    let currentEditing = null;

    const listComponent = createAddressListComponent({
        root: listRoot,
        onSelect: async (id) => {
            const items = store.getState().items || [];
            const selected = items.find((x) => Number(x.id) === Number(id));
            if (!selected) return;
            listComponent.render(items, id);
        },
        onSetDefault: async (id) => {
            try {
                await store.setDefaultAddress(id);
                renderFromStore();
                showToast("Đã cập nhật địa chỉ mặc định.", "success");
            } catch (err) {
                showToast(err?.message || "Không thể đặt mặc định.", "error");
            }
        },
        onEdit: (id) => {
            const items = store.getState().items || [];
            currentEditing = items.find((x) => Number(x.id) === Number(id)) || null;
            openForm(currentEditing);
        },
        onDelete: async (id) => {
            if (!window.confirm("Bạn có chắc muốn xóa địa chỉ này?")) return;
            try {
                await store.removeAddress(id);
                renderFromStore();
                showToast("Đã xóa địa chỉ.", "success");
            } catch (err) {
                showToast(err?.message || "Không thể xóa địa chỉ.", "error");
            }
        }
    });

    const formComponent = createAddressFormComponent({
        root: formRoot,
        onSubmit: async (payload) => {
            if (payload.id) {
                await store.updateAddress(payload.id, payload);
                showToast("Đã cập nhật địa chỉ.", "success");
            } else {
                await store.createAddress(payload);
                showToast("Đã thêm địa chỉ.", "success");
            }
            currentEditing = null;
            renderFromStore();
        },
        onCancel: () => {
            currentEditing = null;
        }
    });

    btnAdd.addEventListener("click", function () {
        currentEditing = null;
        openForm(null);
    });

    await store.load(true);
    renderFromStore();

    function openForm(value) {
        formComponent.render(value || {
            tenNguoiNhan: "",
            sdtNguoiNhan: "",
            diaChiCuThe: "",
            phuongXa: "",
            quanHuyen: "",
            tinhThanh: "",
            macDinh: false
        });
        requestAnimationFrame(function () {
            formRoot.scrollIntoView({ behavior: "smooth", block: "start" });
        });
    }

    function renderFromStore() {
        const state = store.getState();
        const items = (state.items || []).filter((x) => !x.isDeleted);
        const selected = (items.find((x) => !!x.macDinh) || items[0] || {}).id || null;
        listComponent.render(items, selected);
    }

    return { store, listComponent, formComponent };
}

function showToast(message, type) {
    if (typeof window.showToast === "function") {
        window.showToast(message, type);
    }
}
