/**
 * Province -> District -> Ward dropdown component.
 */
export function createLocationDropdownsComponent(options) {
    const provinceSelect = options?.provinceSelect;
    const districtSelect = options?.districtSelect;
    const wardSelect = options?.wardSelect;
    const onProvinceChange = options?.onProvinceChange || (async () => []);
    const onDistrictChange = options?.onDistrictChange || (async () => []);
    const onWardChange = options?.onWardChange || function () {};
    const onError = options?.onError || function () {};

    if (!provinceSelect || !districtSelect || !wardSelect) {
        throw new Error("LocationDropdowns requires province/district/ward selects.");
    }

    function init(provinces) {
        fill(provinceSelect, provinces || [], "-- Chon tinh/thanh --");
        fill(districtSelect, [], "-- Chon quan/huyen --");
        fill(wardSelect, [], "-- Chon phuong/xa --");
        districtSelect.disabled = true;
        wardSelect.disabled = true;
        bindEvents();
    }

    function bindEvents() {
        provinceSelect.addEventListener("change", async function () {
            try {
                districtSelect.disabled = true;
                wardSelect.disabled = true;
                fill(wardSelect, [], "-- Chon phuong/xa --");
                const districts = await onProvinceChange(this.value);
                fill(districtSelect, districts || [], "-- Chon quan/huyen --");
                districtSelect.disabled = false;
            } catch (err) {
                onError(err);
            }
        });

        districtSelect.addEventListener("change", async function () {
            try {
                wardSelect.disabled = true;
                const wards = await onDistrictChange(this.value);
                fill(wardSelect, wards || [], "-- Chon phuong/xa --");
                wardSelect.disabled = false;
            } catch (err) {
                onError(err);
            }
        });

        wardSelect.addEventListener("change", function () {
            onWardChange(this.value);
        });
    }

    function fill(selectEl, items, placeholder) {
        const html = ['<option value="">' + placeholder + '</option>']
            .concat((items || []).map((x) => '<option value="' + escapeHtml(x.id) + '">' + escapeHtml(x.name) + "</option>"))
            .join("");
        selectEl.innerHTML = html;
    }

    return { init, fill };
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
