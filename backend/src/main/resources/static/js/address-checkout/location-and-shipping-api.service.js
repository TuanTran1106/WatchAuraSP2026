import { apiGetJson, apiPostJson } from "./api-client.js";
import { validateLocationOptionList, validateShippingFeeResponse } from "./types-and-schemas.js";

/**
 * Shipping location + fee service
 * Unified to current WatchAura shipping endpoints.
 */
export const LocationAndShippingApiService = {
    async getProvinces() {
        const res = await apiGetJson("/api/shipping/provinces");
        const items = res?.items || [];
        if (!validateLocationOptionList(items)) {
            throw new Error("Du lieu tinh/thanh khong hop le.");
        }
        return items;
    },

    async getDistricts(provinceId) {
        if (!provinceId) throw new Error("provinceId khong hop le.");
        const res = await apiGetJson(`/api/shipping/districts?provinceId=${encodeURIComponent(provinceId)}`);
        const items = res?.items || [];
        if (!validateLocationOptionList(items)) {
            throw new Error("Du lieu quan/huyen khong hop le.");
        }
        return items;
    },

    async getWards(districtId) {
        if (!districtId) throw new Error("districtId khong hop le.");
        const res = await apiGetJson(`/api/shipping/wards?districtId=${encodeURIComponent(districtId)}`);
        const items = res?.items || [];
        if (!validateLocationOptionList(items)) {
            throw new Error("Du lieu phuong/xa khong hop le.");
        }
        return items;
    },

    async calculateShippingFee(payload) {
        const res = await apiPostJson("/api/shipping/fee", payload || {});
        if (!validateShippingFeeResponse(res)) {
            throw new Error("Shipping fee response khong hop le.");
        }
        return res;
    }
};
