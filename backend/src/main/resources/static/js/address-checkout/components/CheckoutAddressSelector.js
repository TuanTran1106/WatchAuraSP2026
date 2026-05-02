/**
 * Checkout address selector orchestrates:
 * - saved address select
 * - manual location dropdown mode
 * - hidden fields sync for backend hardening
 */
export function createCheckoutAddressSelectorComponent(options) {
    const savedSelect = options?.savedSelect;
    const hiddenSelectedAddressId = options?.hiddenSelectedAddressId;
    const hiddenProvinceId = options?.hiddenProvinceId;
    const hiddenDistrictId = options?.hiddenDistrictId;
    const hiddenWardCode = options?.hiddenWardCode;
    const diaChiTextarea = options?.diaChiTextarea;
    const onSavedSelected = options?.onSavedSelected || function () {};
    const onSwitchToManual = options?.onSwitchToManual || function () {};

    if (!savedSelect) {
        throw new Error("CheckoutAddressSelector requires savedSelect.");
    }

    function init() {
        savedSelect.addEventListener("change", function () {
            const selected = savedSelect.options[savedSelect.selectedIndex];
            const addressId = savedSelect.value;
            if (!addressId) {
                syncManualMode();
                onSwitchToManual();
                return;
            }

            const fullAddress = selected?.getAttribute("data-full-address") || "";
            if (diaChiTextarea && fullAddress) {
                diaChiTextarea.value = fullAddress;
            }
            if (hiddenSelectedAddressId) hiddenSelectedAddressId.value = addressId;
            if (hiddenProvinceId) hiddenProvinceId.value = "";
            if (hiddenDistrictId) hiddenDistrictId.value = "";
            if (hiddenWardCode) hiddenWardCode.value = "";
            onSavedSelected(Number(addressId));
        });
    }

    function syncManualMode(provinceId = "", districtId = "", wardCode = "") {
        if (savedSelect && savedSelect.value) savedSelect.value = "";
        if (hiddenSelectedAddressId) hiddenSelectedAddressId.value = "";
        if (hiddenProvinceId) hiddenProvinceId.value = String(provinceId || "");
        if (hiddenDistrictId) hiddenDistrictId.value = String(districtId || "");
        if (hiddenWardCode) hiddenWardCode.value = String(wardCode || "");
    }

    return { init, syncManualMode };
}
