import { validateAddressPayload } from "../types-and-schemas.js";
import { LocationAndShippingApiService } from "../location-and-shipping-api.service.js";

/**
 * Address modal/form component with smooth open/close.
 */
export function createAddressFormComponent(options) {
    const root = options?.root;
    const onSubmit = options?.onSubmit || (async () => {});
    const onCancel = options?.onCancel || function () {};
    if (!root) throw new Error("AddressForm requires root element.");

    let editingId = null;

    function render(initialValue) {
        const data = initialValue || {};
        editingId = data.id || null;
        root.innerHTML = `
            <div class="wa-address-form-shell is-open">
                <div class="wa-address-form">
                    <h3 class="wa-address-form__title">${editingId ? "Sửa địa chỉ" : "Thêm địa chỉ mới"}</h3>
                    <div class="wa-address-form__grid">
                        <label>Tên người nhận<input id="wa-tenNguoiNhan" value="${escapeHtml(data.tenNguoiNhan || "")}" maxlength="100"></label>
                        <label>Số điện thoại<input id="wa-sdtNguoiNhan" value="${escapeHtml(data.sdtNguoiNhan || "")}" maxlength="20"></label>
                        <label>Tỉnh/Thành phố
                            <select id="wa-ghnProvinceId">
                                <option value="">-- Chọn Tỉnh/Thành phố --</option>
                            </select>
                        </label>
                        <label>Quận/Huyện
                            <select id="wa-ghnDistrictId" disabled>
                                <option value="">-- Chọn Quận/Huyện --</option>
                            </select>
                        </label>
                        <label>Phường/Xã
                            <select id="wa-ghnWardCode" disabled>
                                <option value="">-- Chọn Phường/Xã --</option>
                            </select>
                        </label>
                        <label class="full">Địa chỉ cụ thể<input id="wa-diaChiCuThe" value="${escapeHtml(data.diaChiCuThe || "")}" maxlength="255"></label>
                        <input type="hidden" id="wa-phuongXa" value="${escapeHtml(data.phuongXa || "")}">
                        <input type="hidden" id="wa-quanHuyen" value="${escapeHtml(data.quanHuyen || "")}">
                        <input type="hidden" id="wa-tinhThanh" value="${escapeHtml(data.tinhThanh || "")}">
                        <label class="full wa-checkbox"><input type="checkbox" id="wa-macDinh" ${data.macDinh ? "checked" : ""}> Đặt làm địa chỉ mặc định</label>
                    </div>
                    <p class="wa-address-form__error" id="wa-address-form-error"></p>
                    <div class="wa-address-form__actions">
                        <button type="button" class="wa-btn wa-btn--ghost" id="wa-address-cancel">Hủy</button>
                        <button type="button" class="wa-btn wa-btn--primary" id="wa-address-submit">${editingId ? "Lưu thay đổi" : "Thêm địa chỉ"}</button>
                    </div>
                </div>
            </div>
        `;
        bindEvents();
        initLocationDropdowns(data).catch(function (err) {
            const errorEl = root.querySelector("#wa-address-form-error");
            if (errorEl) errorEl.textContent = err?.message || "Không thể tải dữ liệu tỉnh/huyện/xã.";
        });
    }

    function bindEvents() {
        const btnSubmit = root.querySelector("#wa-address-submit");
        const btnCancel = root.querySelector("#wa-address-cancel");
        const errorEl = root.querySelector("#wa-address-form-error");

        if (btnCancel) {
            btnCancel.addEventListener("click", function () {
                close();
                onCancel();
            });
        }
        if (btnSubmit) {
            btnSubmit.addEventListener("click", async function () {
                btnSubmit.disabled = true;
                if (errorEl) errorEl.textContent = "";
                try {
                    const payload = collectPayload();
                    const check = validateAddressPayload(payload);
                    if (!check.valid) {
                        throw new Error(check.errors.join(" "));
                    }
                    await onSubmit({ ...check.value, id: editingId });
                    close();
                } catch (err) {
                    if (errorEl) errorEl.textContent = err?.message || "Không thể lưu địa chỉ.";
                } finally {
                    btnSubmit.disabled = false;
                }
            });
        }
    }

    function collectPayload() {
        syncLocationNames();
        return {
            tenNguoiNhan: valueOf("#wa-tenNguoiNhan"),
            sdtNguoiNhan: valueOf("#wa-sdtNguoiNhan"),
            diaChiCuThe: valueOf("#wa-diaChiCuThe"),
            phuongXa: valueOf("#wa-phuongXa"),
            quanHuyen: valueOf("#wa-quanHuyen"),
            tinhThanh: valueOf("#wa-tinhThanh"),
            ghnProvinceId: numericOf("#wa-ghnProvinceId"),
            ghnDistrictId: numericOf("#wa-ghnDistrictId"),
            ghnWardCode: valueOf("#wa-ghnWardCode"),
            macDinh: !!root.querySelector("#wa-macDinh")?.checked
        };
    }

    async function initLocationDropdowns(data) {
        const provinceSelect = root.querySelector("#wa-ghnProvinceId");
        const districtSelect = root.querySelector("#wa-ghnDistrictId");
        const wardSelect = root.querySelector("#wa-ghnWardCode");
        if (!provinceSelect || !districtSelect || !wardSelect) return;

        const provinces = await LocationAndShippingApiService.getProvinces();
        fillOptions(provinceSelect, provinces, "-- Chọn Tỉnh/Thành phố --");

        if (data?.ghnProvinceId) {
            provinceSelect.value = String(data.ghnProvinceId);
            await loadDistricts(provinceSelect.value, data?.ghnDistrictId);
        }
        if (data?.ghnDistrictId) {
            await loadWards(String(data.ghnDistrictId), data?.ghnWardCode);
        }

        provinceSelect.addEventListener("change", async function () {
            root.querySelector("#wa-tinhThanh").value = selectedText(provinceSelect);
            await loadDistricts(this.value, "");
            fillOptions(wardSelect, [], "-- Chọn Phường/Xã --");
            wardSelect.disabled = true;
        });

        districtSelect.addEventListener("change", async function () {
            root.querySelector("#wa-quanHuyen").value = selectedText(districtSelect);
            await loadWards(this.value, "");
        });

        wardSelect.addEventListener("change", function () {
            root.querySelector("#wa-phuongXa").value = selectedText(wardSelect);
        });
    }

    async function loadDistricts(provinceId, selectedDistrictId) {
        const districtSelect = root.querySelector("#wa-ghnDistrictId");
        const wardSelect = root.querySelector("#wa-ghnWardCode");
        if (!districtSelect || !wardSelect) return;
        if (!provinceId) {
            fillOptions(districtSelect, [], "-- Chọn Quận/Huyện --");
            fillOptions(wardSelect, [], "-- Chọn Phường/Xã --");
            districtSelect.disabled = true;
            wardSelect.disabled = true;
            return;
        }
        districtSelect.disabled = true;
        const districts = await LocationAndShippingApiService.getDistricts(provinceId);
        fillOptions(districtSelect, districts, "-- Chọn Quận/Huyện --");
        districtSelect.disabled = false;
        if (selectedDistrictId) {
            districtSelect.value = String(selectedDistrictId);
            root.querySelector("#wa-quanHuyen").value = selectedText(districtSelect);
        }
    }

    async function loadWards(districtId, selectedWardCode) {
        const wardSelect = root.querySelector("#wa-ghnWardCode");
        if (!wardSelect) return;
        if (!districtId) {
            fillOptions(wardSelect, [], "-- Chọn Phường/Xã --");
            wardSelect.disabled = true;
            return;
        }
        wardSelect.disabled = true;
        const wards = await LocationAndShippingApiService.getWards(districtId);
        fillOptions(wardSelect, wards, "-- Chọn Phường/Xã --");
        wardSelect.disabled = false;
        if (selectedWardCode) {
            wardSelect.value = String(selectedWardCode);
            root.querySelector("#wa-phuongXa").value = selectedText(wardSelect);
        }
    }

    function fillOptions(selectEl, items, placeholder) {
        const html = ['<option value="">' + placeholder + "</option>"]
            .concat((items || []).map((x) => '<option value="' + escapeHtml(x.id) + '">' + escapeHtml(x.name) + "</option>"))
            .join("");
        selectEl.innerHTML = html;
    }

    function selectedText(selectEl) {
        return selectEl?.options?.[selectEl.selectedIndex]?.text || "";
    }

    function syncLocationNames() {
        const provinceSelect = root.querySelector("#wa-ghnProvinceId");
        const districtSelect = root.querySelector("#wa-ghnDistrictId");
        const wardSelect = root.querySelector("#wa-ghnWardCode");
        if (provinceSelect) root.querySelector("#wa-tinhThanh").value = selectedText(provinceSelect);
        if (districtSelect) root.querySelector("#wa-quanHuyen").value = selectedText(districtSelect);
        if (wardSelect) root.querySelector("#wa-phuongXa").value = selectedText(wardSelect);
    }

    function close() {
        root.innerHTML = "";
    }

    function valueOf(selector) {
        return (root.querySelector(selector)?.value || "").trim();
    }

    function numericOf(selector) {
        const v = valueOf(selector);
        if (!v) return null;
        const n = Number(v);
        return Number.isFinite(n) ? n : null;
    }

    return { render, close };
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
