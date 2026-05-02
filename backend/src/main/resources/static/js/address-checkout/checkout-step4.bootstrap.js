import { createCheckoutAddressStore } from "./stores.js";
import { createLocationDropdownsComponent } from "./components/LocationDropdowns.js";
import { createCheckoutAddressSelectorComponent } from "./components/CheckoutAddressSelector.js";

/**
 * Optional bootstrap for checkout page.
 * Safe to call many times; returns null if required DOM nodes missing.
 */
export async function bootstrapCheckoutStep4() {
    const savedSelect = document.getElementById("shippingAddressSelect");
    const provinceSelect = document.getElementById("shippingProvinceSelect");
    const districtSelect = document.getElementById("shippingDistrictSelect");
    const wardSelect = document.getElementById("shippingWardSelect");
    const hiddenSelectedAddressId = document.getElementById("selectedAddressIdInput");
    const hiddenProvinceId = document.getElementById("shippingProvinceIdInput");
    const hiddenDistrictId = document.getElementById("shippingDistrictIdInput");
    const hiddenWardCode = document.getElementById("shippingWardCodeInput");
    const diaChiTextarea = document.getElementById("diaChi");
    const shippingStatusNote = document.getElementById("shipping-status-note");
    const shippingFeeEl = document.getElementById("checkout-shipping-fee");
    const grandTotalEl = document.getElementById("checkout-tong-tien");

    if (!savedSelect || !provinceSelect || !districtSelect || !wardSelect) return null;

    const store = createCheckoutAddressStore();
    window.__waCheckoutStep4Active = true;
    const baseTotal = Number(window.__checkoutBaseTotal || 0);
    store.setPricing({ merchandiseSubtotal: baseTotal, discount: 0 });
    window.__waCheckoutSetPricing = function (payload) {
        store.setPricing({
            merchandiseSubtotal: Number(payload?.merchandiseSubtotal || 0),
            discount: Number(payload?.discount || 0)
        });
        return store.recalculateShippingFee();
    };

    const selector = createCheckoutAddressSelectorComponent({
        savedSelect,
        hiddenSelectedAddressId,
        hiddenProvinceId,
        hiddenDistrictId,
        hiddenWardCode,
        diaChiTextarea,
        onSavedSelected: async (addressId) => {
            store.selectSavedAddress(addressId);
            await tryRecalculate();
        },
        onSwitchToManual: () => {
            selector.syncManualMode();
        }
    });
    selector.init();

    const locations = createLocationDropdownsComponent({
        provinceSelect,
        districtSelect,
        wardSelect,
        onProvinceChange: async (provinceId) => {
            const districts = await store.selectProvince(provinceId);
            selector.syncManualMode(provinceId, "", "");
            await tryRecalculate();
            return districts;
        },
        onDistrictChange: async (districtId) => {
            const wards = await store.selectDistrict(districtId);
            selector.syncManualMode(provinceSelect.value, districtId, "");
            await tryRecalculate();
            return wards;
        },
        onWardChange: async (wardCode) => {
            store.selectWard(wardCode);
            selector.syncManualMode(provinceSelect.value, districtSelect.value, wardCode);
            await tryRecalculate();
        },
        onError: (err) => showStatus(err?.message || "Khong tai duoc du lieu dia chi.")
    });

    const provinces = await store.loadProvinces();
    locations.init(provinces);


    const initialAddressId = savedSelect.value ? Number(savedSelect.value) : 0;
    if (initialAddressId > 0) {
        store.selectSavedAddress(initialAddressId);
        if (hiddenSelectedAddressId) hiddenSelectedAddressId.value = String(initialAddressId);
        const selectedOption = savedSelect.options[savedSelect.selectedIndex];
        const fullAddress = selectedOption?.getAttribute("data-full-address") || "";
        if (diaChiTextarea && fullAddress) {
            diaChiTextarea.value = fullAddress;
        }
    }

    store.subscribe((s) => {
        if (shippingFeeEl) shippingFeeEl.textContent = formatVnd(s.shippingFee || 0);
        if (grandTotalEl) grandTotalEl.textContent = formatVnd(s.grandTotal || 0);
        if (s.shippingError) {
            showStatus(s.shippingError, "error");
        } else if (s.shippingFallbackApplied) {
            showStatus("Dang dung phi ship tam do provider loi.", "warning");
        } else if (s.shippingLoading) {
            showStatus("Dang tinh phi van chuyen...", "loading");
        } else {
            showStatus("");
        }
    });

    // Render state hiện tại ngay lần đầu để tránh giữ số server-render cũ.
    const initialState = store.getState();
    if (shippingFeeEl) shippingFeeEl.textContent = formatVnd(initialState.shippingFee || 0);
    if (grandTotalEl) grandTotalEl.textContent = formatVnd(initialState.grandTotal || 0);

    await tryRecalculate();

    async function tryRecalculate() {
        try {
            await store.recalculateShippingFee();
        } catch (_err) {
            // handled by store + status note
        }
    }

    function showStatus(message, mode) {
        if (!shippingStatusNote) return;
        if (!message) {
            shippingStatusNote.textContent = "";
            shippingStatusNote.style.display = "none";
            shippingStatusNote.className = "cart-summary__note";
            return;
        }
        shippingStatusNote.textContent = message;
        shippingStatusNote.style.display = "";
        shippingStatusNote.className = "cart-summary__note " + (
            mode === "error" ? "cart-summary__note--error" :
            mode === "warning" ? "cart-summary__note--warning" :
            "cart-summary__note--loading"
        );
    }

    return { store, selector, locations };
}

function formatVnd(value) {
    return Number(value || 0).toLocaleString("vi-VN") + " ₫";
}
