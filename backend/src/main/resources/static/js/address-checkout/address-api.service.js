import { apiDelete, apiGetJson, apiPostJson, apiPutJson } from "./api-client.js";
import { normalizeAddressPayload, validateAddressPayload } from "./types-and-schemas.js";

/**
 * Address CRUD API services.
 * NOTE: Uses existing endpoints to avoid backend contract break.
 */
export const AddressApiService = {
    async list() {
        return await apiGetJson("/nguoidung/dia-chi");
    },

    async create(payload) {
        const check = validateAddressPayload(payload);
        if (!check.valid) throw new Error(check.errors.join(" "));
        return await apiPostJson("/nguoidung/dia-chi", normalizeAddressPayload(check.value));
    },

    async update(id, payload) {
        if (!id) throw new Error("Thieu id dia chi.");
        const check = validateAddressPayload(payload);
        if (!check.valid) throw new Error(check.errors.join(" "));
        return await apiPutJson(`/nguoidung/dia-chi/${id}`, normalizeAddressPayload(check.value));
    },

    async remove(id) {
        if (!id) throw new Error("Thieu id dia chi.");
        return await apiDelete(`/nguoidung/dia-chi/${id}`);
    },

    async setDefault(id) {
        if (!id) throw new Error("Thieu id dia chi.");
        return await apiPutJson(`/nguoidung/dia-chi/${id}/mac-dinh`, {});
    }
};
