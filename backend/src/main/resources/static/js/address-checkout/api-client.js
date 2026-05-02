/**
 * Shared HTTP client for address-checkout modules.
 */

export async function apiGetJson(url) {
    const res = await fetch(url, { credentials: "same-origin" });
    return parseJsonResponse(res);
}

export async function apiPostJson(url, body) {
    const res = await fetch(url, {
        method: "POST",
        credentials: "same-origin",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body || {})
    });
    return parseJsonResponse(res);
}

export async function apiPutJson(url, body) {
    const res = await fetch(url, {
        method: "PUT",
        credentials: "same-origin",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body || {})
    });
    return parseJsonResponse(res);
}

export async function apiDelete(url) {
    const res = await fetch(url, {
        method: "DELETE",
        credentials: "same-origin"
    });
    return parseJsonResponse(res);
}

async function parseJsonResponse(res) {
    let data = null;
    try {
        data = await res.json();
    } catch (_e) {
        data = null;
    }

    if (!res.ok) {
        const message = data?.message || data?.thongBao || `HTTP ${res.status}`;
        const err = new Error(message);
        err.status = res.status;
        err.payload = data;
        throw err;
    }
    return data;
}
