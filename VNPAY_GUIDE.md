# Hướng dẫn tích hợp thanh toán VNPay

## Mục lục
1. [Đăng ký tài khoản VNPay](#1-đăng-ký-tài-khoản-vnpay)
2. [Cấu hình credentials](#2-cấu-hình-credentials)
3. [Cấu trúc code](#3-cấu-trúc-code)
4. [Flow thanh toán](#4-flow-thanh-toán)
5. [Test với Sandbox](#5-test-với-sandbox)
6. [Deploy Production](#6-deploy-production)
7. [Mã lỗi VNPay](#7-mã-lỗi-vnpay)

---

## 1. Đăng ký tài khoản VNPay

### Sandbox (Môi trường test)
1. Truy cập: https://sandbox.vnpayment.vn/merchant/register
2. Điền thông tin đăng ký
3. Sau khi đăng ký thành công, bạn sẽ nhận được:
   - **TMN Code**: Mã merchant của bạn (ví dụ: `7B4F2S5P`)
   - **Hash Secret**: Khóa bí mật để ký HMAC SHA512

### Production (Môi trường thật)
1. Truy cập: https://merchant.vnpay.vn/
2. Đăng ký và chờ được duyệt
3. Nhận credentials tương tự sandbox

---

## 2. Cấu hình credentials

### Cách 1: Sử dụng biến môi trường (Khuyến nghị)

Tạo file `.env` trong thư mục `backend/`:
```bash
# VNPay Configuration
VNPAY_TMN_CODE=YOUR_TMN_CODE_HERE
VNPAY_HASH_SECRET=YOUR_HASH_SECRET_HERE
VNPAY_URL=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html
VNPAY_RETURN_URL=http://localhost:8080/thanh-toan/vnpay/return
```

### Cách 2: Cấu hình trong application.properties

Mở file `backend/src/main/resources/application.properties`:

```properties
# Sandbox (test)
vnpay.tmn-code=YOUR_TMN_CODE
vnpay.hash-secret=YOUR_HASH_SECRET
vnpay.url=https://sandbox.vnpayment.vn/paymentv2/vpcpay.html

# Production (thật)
# vnpay.url=https://pay.vnpay.vn/vpcpay.html
```

### Cách 3: Chạy với arguments

```bash
java -jar app.jar \
  --vnpay.tmn-code=YOUR_TMN_CODE \
  --vnpay.hash-secret=YOUR_HASH_SECRET
```

---

## 3. Cấu trúc code

```
backend/src/main/java/com/example/watchaura/
├── config/
│   └── VNPayProperties.java          # Class cấu hình VNPay
├── service/
│   ├── VNPayService.java             # Interface
│   └── impl/
│       └── VNPayServiceImpl.java     # Implement xử lý thanh toán
└── controller/
    └── UserCheckoutController.java   # Controller xử lý checkout & return

backend/src/main/resources/
├── application.properties            # Cấu hình VNPay
└── templates/user/
    ├── thanh-toan.html              # Trang thanh toán (chọn phương thức)
    ├── vnpay-result.html            # Trang kết quả VNPay
    └── thanh-toan-thanh-cong.html   # Trang thành công
```

---

## 4. Flow thanh toán

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           FLOW THANH TOÁN VNPAY                             │
└─────────────────────────────────────────────────────────────────────────────┘

  Khách hàng                    Backend                    VNPay
     │                           │                          │
     │  1. Chọn "Thanh toán     │                          │
     │     VN Pay"              │                          │
     │ ───────────────────────►  │                          │
     │                           │                          │
     │                           │  2. Tạo đơn hàng         │
     │                           │     (trạng thái:         │
     │                           │     CHO_THANH_TOAN)      │
     │                           │                          │
     │                           │  3. Tạo payment URL      │
     │                           │     với HMAC SHA512      │
     │                           │                          │
     │  4. Redirect đến          │                          │
     │     VNPay Gate            │                          │
     │ ◄──────────────────────── │                          │
     │                           │                          │
     │  5. Nhập thông tin        │                          │
     │     thẻ/ATM               │                          │
     │ ───────────────────────►  │                          │
     │                           │                          │
     │                           │  6. Xử lý thanh toán     │
     │                           │ ◄─────────────────────── │
     │                           │                          │
     │  7. Redirect về return URL│                          │
     │     (kèm vnp_ResponseCode)│                          │
     │ ───────────────────────►  │                          │
     │                           │                          │
     │                           │  8. Verify SecureHash    │
     │                           │                          │
     │                           │  9a. Thành công (00):   │
     │                           │     - Cập nhật đơn       │
     │                           │       CHO_XAC_NHAN       │
     │                           │     - Xóa giỏ hàng      │
     │                           │     - Redirect thành công│
     │                           │                          │
     │                           │  9b. Thất bại:          │
     │                           │     - Hủy đơn hàng     │
     │                           │     - Redirect kết quả  │
     │                           │                          │
     │  10. Hiển thị kết quả     │                          │
     │ ◄──────────────────────── │                          │
```

---

## 5. Test với Sandbox

### Chuẩn bị
1. Đảm bảo đã cấu hình đúng TMN Code và Hash Secret từ sandbox
2. Chạy ứng dụng: `mvn spring-boot:run` hoặc `mvnw spring-boot:run`
3. Truy cập http://localhost:8080

### Các bước test
1. Đăng nhập với tài khoản test
2. Thêm sản phẩm vào giỏ hàng
3. Chuyển đến trang thanh toán
4. Chọn phương thức **VN Pay**
5. Điền thông tin giao hàng và bấm "Đặt hàng & thanh toán VN Pay"
6. Bạn sẽ được redirect đến trang sandbox của VNPay
7. Sử dụng thông tin thẻ test được cung cấp bởi VNPay sandbox

### Thông tin thẻ test (Sandbox VNPay)
Thông tin thẻ test thường được cung cấp trong tài liệu VNPay sandbox:
- Số thẻ: `9704198526191431118` (Ngân hàng NCB - test)
- Tên: `NGUYEN VAN A`
- Ngày phát hành: `07/15`
- Mật khẩu OTP: `123456`

---

## 6. Deploy Production

### Bước 1: Cập nhật cấu hình
```properties
# Trong application.properties hoặc biến môi trường
vnpay.url=https://pay.vnpay.vn/vpcpay.html
vnpay.tmn-code=YOUR_PRODUCTION_TMN_CODE
vnpay.hash-secret=YOUR_PRODUCTION_HASH_SECRET
vnpay.return-url=https://your-domain.com/thanh-toan/vnpay/return
```

### Bước 2: Cấu hình Return URL
- Đăng nhập vào VNPay Merchant Portal
- Cấu hình **Return URL** phải là HTTPS

### Bước 3: Kiểm tra các requirements
- [ ] Domain phải là HTTPS
- [ ] Return URL phải public accessible
- [ ] Firewall cho phép traffic từ VNPay

---

## 7. Mã lỗi VNPay

| Mã | Mô tả |
|----|-------|
| `00` | Giao dịch thành công |
| `07` | Giao dịch bị nghi ngờ (suspicious) |
| `09` | Giao dịch thẻ chưa đăng ký internet banking |
| `10` | Giao dịch xác thực thẻ fail |
| `11` | Giao dịch đang xử lý |
| `12` | Thẻ hết hạn |
| `13` | Sai mật khẩu OTP |
| `24` | Khách hàng hủy giao dịch |
| `51` | Tài khoản không đủ tiền |
| `65` | Tài khoản đã vượt quá giới hạn |
| `75` | Ngân hàng đang bảo trì |
| `99` | Lỗi không xác định |

---

## Troubleshooting

### Lỗi "Invalid SecureHash"
- Kiểm tra Hash Secret có đúng không
- Đảm bảo không có tham số nào bị thiếu
- Kiểm tra URL encoding đúng cách

### Lỗi "Return URL mismatch"
- Kiểm tra return URL trong code khớp với return URL đăng ký ở VNPay
- Với localhost, dùng ngrok để test: `ngrok http 8080`

### Không redirect về return URL
- Kiểm tra return URL có thể truy cập từ internet
- Kiểm tra cấu hình firewall

---

## Liên hệ hỗ trợ

- Email: support@vnpay.vn
- Hotline: 1900 55 55 77
- Tài liệu: https://sandbox.vnpayment.vn/apis/docs/huong-dan-tich-hop/
