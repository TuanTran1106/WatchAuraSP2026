import { AddressApiService } from "./address-api.service.js";
import { LocationAndShippingApiService } from "./location-and-shipping-api.service.js";

/**
 * Tiny store helper (no framework required).
 */
function createStore(initialState) {
    let state = { ...(initialState || {}) };
    const listeners = new Set();

    function getState() {
        return state;
    }

    function setState(patch) {
        state = { ...state, ...(patch || {}) };
        listeners.forEach((fn) => fn(state));
    }

    function subscribe(listener) {
        listeners.add(listener);
        return () => listeners.delete(listener);
    }

    return { getState, setState, subscribe };
}

/**
 * Step 3.1 - Address Book Store
 * Manage CRUD lifecycle for profile/checkout shared address data.
 */
export function createAddressBookStore() {
    const store = createStore({
        items: [],
        loading: false,
        saving: false,
        error: "",
        lastLoadedAt: 0
    });

    async function load(force = false) {
        const current = store.getState();
        if (!force && current.loading) return current.items;
        store.setState({ loading: true, error: "" });
        try {
            const data = await AddressApiService.list();
            const items = Array.isArray(data) ? data : (data?.duLieu || data?.data || []);
            store.setState({
                items,
                loading: false,
                lastLoadedAt: Date.now()
            });
            return items;
        } catch (err) {
            store.setState({ loading: false, error: err?.message || "Khong tai duoc danh sach dia chi." });
            throw err;
        }
    }

    async function createAddress(payload) {
        store.setState({ saving: true, error: "" });
        try {
            await AddressApiService.create(payload);
            await load(true);
        } finally {
            store.setState({ saving: false });
        }
    }

    async function updateAddress(id, payload) {
        store.setState({ saving: true, error: "" });
        try {
            await AddressApiService.update(id, payload);
            await load(true);
        } finally {
            store.setState({ saving: false });
        }
    }

    async function removeAddress(id) {
        store.setState({ saving: true, error: "" });
        try {
            await AddressApiService.remove(id);
            await load(true);
        } finally {
            store.setState({ saving: false });
        }
    }

    async function setDefaultAddress(id) {
        store.setState({ saving: true, error: "" });
        try {
            await AddressApiService.setDefault(id);
            await load(true);
        } finally {
            store.setState({ saving: false });
        }
    }

    return {
        ...store,
        load,
        createAddress,
        updateAddress,
        removeAddress,
        setDefaultAddress
    };
}

/**
 * Step 3.2 - Checkout Address + Shipping Store
 * Source of truth for:
 * - address mode (saved/manual)
 * - location cascade
 * - shipping fee preview
 * - grand total recompute
 */
export function createCheckoutAddressStore() {
    const store = createStore({
        mode: "saved", // "saved" | "manual"
        savedAddresses: [],
        selectedAddressId: null,

        provinces: [],
        districts: [],
        wards: [],
        selectedProvinceId: "",
        selectedDistrictId: "",
        selectedWardCode: "",

        loadingLocations: false,
        locationError: "",

        merchandiseSubtotal: 0,
        discount: 0,
        shippingFee: 0,
        shippingLoading: false,
        shippingError: "",
        shippingFallbackApplied: false,
        shippingProviderErrorCode: "",

        grandTotal: 0,
        lastShippingUpdatedAt: 0
    });

    const locationCache = {
        provinces: null,
        districtsByProvince: new Map(),
        wardsByDistrict: new Map()
    };
    let shippingRequestVersion = 0;

    function recalculateGrandTotal() {
        const s = store.getState();
        const subtotal = Number(s.merchandiseSubtotal || 0);
        const discount = Number(s.discount || 0);
        const shipping = Number(s.shippingFee || 0);
        const merchandiseAfterDiscount = Math.max(0, subtotal - discount);
        store.setState({ grandTotal: merchandiseAfterDiscount + shipping });
    }

    function setPricing({ merchandiseSubtotal, discount }) {
        const patch = {};
        if (typeof merchandiseSubtotal === "number") patch.merchandiseSubtotal = merchandiseSubtotal;
        if (typeof discount === "number") patch.discount = discount;
        store.setState(patch);
        recalculateGrandTotal();
    }

    function setSavedAddresses(addresses) {
        const items = Array.isArray(addresses) ? addresses : [];
        const defaultItem = items.find((x) => !!x?.macDinh) || items[0] || null;
        store.setState({
            savedAddresses: items,
            selectedAddressId: defaultItem?.id || null
        });
    }

    async function loadProvinces() {
        const current = store.getState();
        if (locationCache.provinces) {
            store.setState({ provinces: locationCache.provinces, locationError: "" });
            return locationCache.provinces;
        }
        store.setState({ loadingLocations: true, locationError: "" });
        try {
            const provinces = await LocationAndShippingApiService.getProvinces();
            locationCache.provinces = provinces;
            store.setState({ provinces, loadingLocations: false });
            return provinces;
        } catch (err) {
            store.setState({
                loadingLocations: false,
                locationError: err?.message || "Khong tai duoc danh sach tinh/thanh."
            });
            throw err;
        }
    }

    async function selectProvince(provinceId) {
        store.setState({
            mode: "manual",
            selectedAddressId: null,
            selectedProvinceId: String(provinceId || ""),
            selectedDistrictId: "",
            selectedWardCode: "",
            districts: [],
            wards: []
        });
        if (!provinceId) return [];

        const key = String(provinceId);
        if (locationCache.districtsByProvince.has(key)) {
            const districts = locationCache.districtsByProvince.get(key) || [];
            store.setState({ districts, locationError: "" });
            return districts;
        }

        store.setState({ loadingLocations: true, locationError: "" });
        try {
            const districts = await LocationAndShippingApiService.getDistricts(provinceId);
            locationCache.districtsByProvince.set(key, districts);
            store.setState({ districts, loadingLocations: false });
            return districts;
        } catch (err) {
            store.setState({
                loadingLocations: false,
                locationError: err?.message || "Khong tai duoc danh sach quan/huyen."
            });
            throw err;
        }
    }

    async function selectDistrict(districtId) {
        store.setState({
            mode: "manual",
            selectedAddressId: null,
            selectedDistrictId: String(districtId || ""),
            selectedWardCode: "",
            wards: []
        });
        if (!districtId) return [];

        const key = String(districtId);
        if (locationCache.wardsByDistrict.has(key)) {
            const wards = locationCache.wardsByDistrict.get(key) || [];
            store.setState({ wards, locationError: "" });
            return wards;
        }

        store.setState({ loadingLocations: true, locationError: "" });
        try {
            const wards = await LocationAndShippingApiService.getWards(districtId);
            locationCache.wardsByDistrict.set(key, wards);
            store.setState({ wards, loadingLocations: false });
            return wards;
        } catch (err) {
            store.setState({
                loadingLocations: false,
                locationError: err?.message || "Khong tai duoc danh sach phuong/xa."
            });
            throw err;
        }
    }

    function selectWard(wardCode) {
        store.setState({
            mode: "manual",
            selectedAddressId: null,
            selectedWardCode: String(wardCode || "")
        });
    }

    function selectSavedAddress(addressId) {
        store.setState({
            mode: "saved",
            selectedAddressId: addressId ? Number(addressId) : null
        });
    }

    async function recalculateShippingFee(extraPayload = {}) {
        const s = store.getState();
        const payload = {
            insuranceValue: Math.max(0, Math.round(Number(s.merchandiseSubtotal || 0) - Number(s.discount || 0))),
            ...extraPayload
        };

        if (s.mode === "saved" && s.selectedAddressId) {
            payload.addressId = s.selectedAddressId;
        } else if (s.selectedDistrictId && s.selectedWardCode) {
            payload.toDistrictId = Number(s.selectedDistrictId);
            payload.toWardCode = s.selectedWardCode;
        } else {
            store.setState({
                shippingFee: 0,
                shippingError: "Vui long chon dia chi hoac quan/huyen + phuong/xa.",
                shippingFallbackApplied: false,
                shippingProviderErrorCode: "",
                shippingLoading: false
            });
            recalculateGrandTotal();
            return null;
        }

        shippingRequestVersion += 1;
        const requestVersion = shippingRequestVersion;
        store.setState({ shippingLoading: true, shippingError: "" });

        try {
            const res = await LocationAndShippingApiService.calculateShippingFee(payload);
            if (requestVersion !== shippingRequestVersion) return null;

            store.setState({
                shippingFee: Number(res.shippingFee || 0),
                shippingLoading: false,
                shippingFallbackApplied: !!res.fallbackApplied,
                shippingProviderErrorCode: res.providerErrorCode || "",
                shippingError: "",
                lastShippingUpdatedAt: Date.now()
            });
            recalculateGrandTotal();
            return res;
        } catch (err) {
            if (requestVersion !== shippingRequestVersion) return null;
            store.setState({
                shippingLoading: false,
                shippingFee: 0,
                shippingError: err?.message || "Khong tinh duoc phi van chuyen.",
                shippingFallbackApplied: false,
                shippingProviderErrorCode: ""
            });
            recalculateGrandTotal();
            throw err;
        }
    }

    return {
        ...store,
        setPricing,
        setSavedAddresses,
        loadProvinces,
        selectProvince,
        selectDistrict,
        selectWard,
        selectSavedAddress,
        recalculateShippingFee,
        recalculateGrandTotal
    };
}
