/**
 * Address/Checkout type hints + runtime schemas (plain JS).
 * Keep this file framework-agnostic so it can be reused by Thymeleaf pages or future SPA.
 */

export const ApiEnvelopeSchema = Object.freeze({
    success: "boolean",
    message: "string",
    data: "any",
    errorCode: "string?"
});

export const ShippingFeeResponseSchema = Object.freeze({
    shippingFee: "number",
    currency: "string",
    provider: "string",
    fallbackApplied: "boolean",
    providerErrorCode: "string|null",
    message: "string"
});

export const AddressSchema = Object.freeze({
    id: "number?",
    diaChiCuThe: "string",
    phuongXa: "string",
    quanHuyen: "string",
    tinhThanh: "string",
    ghnProvinceId: "number?",
    ghnDistrictId: "number?",
    ghnWardCode: "string?",
    tenNguoiNhan: "string?",
    sdtNguoiNhan: "string?",
    macDinh: "boolean?",
    isDeleted: "boolean?"
});

export const LocationOptionSchema = Object.freeze({
    id: "string",
    name: "string"
});

export function normalizeAddressPayload(payload) {
    return {
        id: toNumberOrNull(payload?.id),
        diaChiCuThe: toSafeString(payload?.diaChiCuThe),
        phuongXa: toSafeString(payload?.phuongXa),
        quanHuyen: toSafeString(payload?.quanHuyen),
        tinhThanh: toSafeString(payload?.tinhThanh),
        ghnProvinceId: toNumberOrNull(payload?.ghnProvinceId),
        ghnDistrictId: toNumberOrNull(payload?.ghnDistrictId),
        ghnWardCode: toNullableString(payload?.ghnWardCode),
        tenNguoiNhan: toNullableString(payload?.tenNguoiNhan),
        sdtNguoiNhan: toNullableString(payload?.sdtNguoiNhan),
        macDinh: !!payload?.macDinh,
        isDeleted: !!payload?.isDeleted
    };
}

export function validateAddressPayload(payload) {
    const errors = [];
    const x = normalizeAddressPayload(payload);

    if (!x.tenNguoiNhan) errors.push("Tên người nhận không được để trống.");
    if (!x.sdtNguoiNhan) errors.push("Số điện thoại người nhận không được để trống.");
    if (!x.diaChiCuThe) errors.push("Địa chỉ cụ thể không được để trống.");
    if (!x.phuongXa) errors.push("Phường/xã không được để trống.");
    if (!x.quanHuyen) errors.push("Quận/huyện không được để trống.");
    if (!x.tinhThanh) errors.push("Tỉnh/thành không được để trống.");

    if (!x.ghnProvinceId || x.ghnProvinceId <= 0) errors.push("Chưa chọn mã tỉnh GHN hợp lệ.");
    if (!x.ghnDistrictId || x.ghnDistrictId <= 0) errors.push("Chưa chọn mã huyện GHN hợp lệ.");
    if (!x.ghnWardCode) errors.push("Chưa chọn mã xã GHN hợp lệ.");

    if (x.tenNguoiNhan && x.tenNguoiNhan.length > 100) errors.push("Tên người nhận tối đa 100 ký tự.");
    if (x.sdtNguoiNhan && x.sdtNguoiNhan.length > 20) errors.push("Số điện thoại tối đa 20 ký tự.");
    if (x.sdtNguoiNhan && !/^[0-9+\s.-]{8,20}$/.test(x.sdtNguoiNhan)) {
        errors.push("Số điện thoại người nhận không hợp lệ.");
    }

    return {
        valid: errors.length === 0,
        errors,
        value: x
    };
}

export function validateShippingFeeResponse(payload) {
    if (typeof payload !== "object" || payload === null) return false;
    return typeof payload.shippingFee === "number"
        && typeof payload.currency === "string"
        && typeof payload.provider === "string"
        && typeof payload.fallbackApplied === "boolean"
        && typeof payload.message === "string";
}

export function validateLocationOptionList(items) {
    if (!Array.isArray(items)) return false;
    return items.every((x) => typeof x?.id === "string" && typeof x?.name === "string");
}

function toSafeString(v) {
    return typeof v === "string" ? v.trim() : "";
}

function toNullableString(v) {
    const s = toSafeString(v);
    return s || null;
}

function toNumberOrNull(v) {
    if (v === null || typeof v === "undefined" || v === "") return null;
    const n = Number(v);
    return Number.isFinite(n) ? n : null;
}
