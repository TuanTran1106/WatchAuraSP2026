# SHIPPING AI PLAYBOOK

Tai lieu nay dong vai tro "nguon su that" de AI co the doc va trien khai module Shipping cho project e-commerce moi mot cach linh hoat.

---

## 1) Muc tieu

- Tai su dung logic shipping da duoc kiem chung tu project mau.
- Chuan hoa theo kieu kien truc de co the thay doi provider trong tuong lai (GHN -> GHTK/...).
- AI co the doc file nay va tu generate code theo stack cua project moi.

---

## 2) Third-party shipping provider trong project mau

- Provider duoc su dung: **GHN (Giao Hang Nhanh)**.
- Cac nhom API su dung:
  - Master data:
    - `/shiip/public-api/master-data/province`
    - `/shiip/public-api/master-data/district`
    - `/shiip/public-api/master-data/ward`
  - Tinh phi:
    - `/shiip/public-api/v2/shipping-order/fee`
  - Mo rong (optional):
    - `/shiip/public-api/v2/shipping-order/available-services`

Khuyen nghi cho project moi:
- Khong hard-code token/secret trong source.
- Dung env var/secret manager.

---

## 3) Data model can co cho Shipping

### 3.1 Address model (khuyen nghi)

Luu dong thoi:
- Truong hien thi cho nguoi dung:
  - `provinceName`
  - `districtName`
  - `wardName`
  - `addressLine1`
- Truong mapping provider:
  - `providerProvinceId`
  - `providerDistrictId`
  - `providerWardCode`
- Truong quan tri:
  - `isDefault`
  - `recipientName`
  - `recipientPhone`
  - `createdAt`, `updatedAt`

### 3.2 Checkout payload

Can co:
- `addressId` (neu chon dia chi da luu)
- Hoac full dia chi moi (recipient + names + provider IDs)
- `shippingFee`
- `paymentMethod`
- `voucherCode` (neu co)
- `note` (optional)

### 3.3 Order snapshot

Khi tao order, luu snapshot:
- `shippingFee`
- `providerProvinceId`, `providerDistrictId`, `providerWardCode`
- `shippingAddressText`
- `recipientName`, `recipientPhone`

Luu y:
- Khong phu thuoc lai vao bang address sau khi order tao xong.

---

## 4) Workflow chi tiet (tu chon Ward -> hien thi phi ship)

1. FE load cascading address:
   - provinces
   - districts theo province
   - wards theo district
2. User chon xong district + ward.
3. FE tinh trong luong gio hang (hoac default).
4. FE goi API noi bo:
   - `POST /shipping/fee` (hoac endpoint tuong duong)
5. BE goi provider (GHN) de lay phi.
6. BE parse ket qua:
   - thanh cong => tra fee
   - that bai => fallback fee + flag fallback
7. FE render:
   - subtotal
   - discount
   - shipping fee
   - final total
8. Submit order:
   - gui `addressId` + shipping snapshot + `shippingFee`
9. BE map `addressId -> provider IDs` va luu vao order.

---

## 5) Mapping logic noi bo -> provider IDs

Pattern khuyen nghi:
- Khong can bang mapping rieng neu address da luu provider IDs.
- Dung `addressId` de truy van address cua user.
- Lay ra:
  - `providerProvinceId`
  - `providerDistrictId`
  - `providerWardCode`
- Snapshot vao order.

Neu khong co `addressId`:
- Dung provider IDs tu payload checkout (user nhap dia chi moi).

---

## 6) Backend template logic (pseudo Java, co the chuyen stack)

```java
public ShippingFeeResult calculateFee(Integer toDistrictId, String toWardCode, int weightGram) {
    try {
        int safeWeight = weightGram > 0 ? weightGram : 500;

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("service_type_id", 2);
        body.put("from_district_id", warehouseDistrictId);
        body.put("from_ward_code", warehouseWardCode);
        body.put("to_district_id", toDistrictId);
        body.put("to_ward_code", toWardCode);
        body.put("weight", safeWeight);
        body.put("length", 30);
        body.put("width", 20);
        body.put("height", 5);
        body.put("insurance_value", 0);

        // Call provider API with Token + ShopId headers
        JsonNode root = providerClient.post("/shipping-order/fee", body);

        int code = root.has("code") ? root.get("code").asInt() : -1;
        if (code != 200) {
            return ShippingFeeResult.fallback(35000, "provider_non_200");
        }

        JsonNode total = root.path("data").path("total");
        if (total.isMissingNode()) {
            return ShippingFeeResult.fallback(35000, "missing_total");
        }

        return ShippingFeeResult.success(total.asLong());

    } catch (ProviderConnectionException ex) {
        return ShippingFeeResult.fallback(35000, "provider_connection_error");
    } catch (Exception ex) {
        return ShippingFeeResult.fallback(35000, "unexpected_error");
    }
}
```

### Response contract khuyen nghi

```json
{
  "shippingFee": 25000,
  "currency": "VND",
  "provider": "GHN",
  "fallbackApplied": false,
  "providerErrorCode": null,
  "message": "success"
}
```

Neu fallback:

```json
{
  "shippingFee": 35000,
  "currency": "VND",
  "provider": "GHN",
  "fallbackApplied": true,
  "providerErrorCode": "provider_connection_error",
  "message": "using fallback fee"
}
```

---

## 7) Danh sach tinh nang cho AI implement (chia nho task)

### 7.1 Backend tasks

1. Tao `ShippingProvider` interface:
   - `getProvinces()`
   - `getDistricts(provinceId)`
   - `getWards(districtId)`
   - `calculateFee(input)`
2. Tao `GhnShippingProvider` implement interface.
3. Tao `ShippingService` de:
   - validate input
   - goi provider
   - ap fallback
   - tra response contract chuan
4. Tao API:
   - `GET /shipping/provinces`
   - `GET /shipping/districts?provinceId=`
   - `GET /shipping/wards?districtId=`
   - `POST /shipping/fee`
5. Chuan hoa error handling + HTTP status.
6. Chuan hoa env config:
   - `GHN_API_URL`
   - `GHN_TOKEN`
   - `GHN_SHOP_ID`
   - `SHIPPING_WAREHOUSE_DISTRICT_ID`
   - `SHIPPING_WAREHOUSE_WARD_CODE`
7. Cap nhat Address entity luu dual fields name + provider IDs.
8. Checkout/Order service:
   - map `addressId -> provider IDs`
   - snapshot shipping data vao order
   - verify/recalculate shipping fee truoc khi finalize (khuyen nghi)
9. Them unit tests:
   - parse success
   - provider non-200
   - missing total
   - timeout/connection error
   - address mapping logic
10. Them observability:
    - log request-id
    - metric latency
    - metric fallback rate

### 7.2 Frontend tasks

1. Tao `shippingApi` module rieng.
2. Tao state/hook cho cascading dropdown:
   - province -> district -> ward
3. Reset child dropdown khi parent thay doi.
4. Trigger tinh ship khi:
   - da co district + ward
   - gio hang thay doi trong luong
   - user doi dia chi da luu
5. State bat buoc:
   - `isShippingLoading`
   - `shippingFee`
   - `shippingError`
   - `isFallbackFee`
6. Hien thi order summary:
   - subtotal, discount, shipping, final total
7. Submit order payload gom:
   - `addressId`
   - provider IDs snapshot
   - `shippingFee`
8. UX loi:
   - thong bao loi nhe
   - van cho checkout voi fallback fee
   - co nut retry tinh phi
9. E2E tests:
   - chon dia chi moi
   - doi dia chi
   - provider fail -> fallback
   - dat hang thanh cong

---

## 8) Prompt mau de dua cho AI coder trong project moi

Dung prompt sau (copy/paste):

```text
Doc file SHIPPING_AI_PLAYBOOK.md va implement module Shipping cho project nay theo dung architecture trong file.

Yeu cau:
1) Khong hard-code secrets.
2) Tao abstraction ShippingProvider de de mo rong multi-carrier.
3) Implement GHN provider + endpoint shipping master data + shipping fee.
4) Luu dual fields cho address: display names + provider IDs.
5) Checkout phai recalculate phi ship khi doi district/ward hoac doi dia chi.
6) Order phai snapshot shippingFee + provider IDs vao order.
7) Co fallback fee khi provider loi, response co fallbackApplied.
8) Them unit test cho shipping service va mapping logic.
9) Neu stack/framework khac Java/React, giu nguyen business logic, adapt theo convention cua project hien tai.

Bat dau bang viec scan codebase, de xuat file change list, sau do implement tung buoc.
```

---

## 9) Quy tac adapt cho project moi (de AI "linh hoat")

- Neu project la NestJS/Spring/.NET:
  - map pattern service + provider adapter + controller.
- Neu project la monolith:
  - dat trong module `shipping`.
- Neu microservice:
  - tach service rieng va expose API noi bo.
- Neu da co address model:
  - extend them provider IDs thay vi tao bang moi.
- Neu da co checkout flow:
  - chen shipping step truoc submit order.
- Neu provider khac GHN:
  - giu interface, thay implement provider.

---

## 10) Checklist acceptance (Definition of Done)

- [ ] FE load duoc province/district/ward theo cascade.
- [ ] FE tinh va hien thi shipping fee truoc khi dat hang.
- [ ] FE co xu ly fallback fee va retry.
- [ ] BE goi provider thanh cong va parse dung fee.
- [ ] BE fallback an toan khi provider loi.
- [ ] Order luu snapshot shipping data day du.
- [ ] Unit test shipping pass.
- [ ] Secrets duoc lay tu env/secret manager.

---

File nay co the dung nhu "nguon context dau vao" cho AI de bat dau implement tren moi codebase.
