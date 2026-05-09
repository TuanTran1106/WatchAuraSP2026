# Hướng Dẫn Import Sản Phẩm Từ Excel

## 📋 Cấu Trúc File Excel

### Tên Cột (Hàng 1 - Header):
Các cột PHẢI theo đúng tên này (không phân biệt chữ hoa/thường):

| Tên Cột | Bắt Buộc | Mô Tả |
|---------|----------|--------|
| Tên Sản Phẩm | ✅ | Tên sản phẩm |
| Thương Hiệu | ✅ | Tên thương hiệu phải khớp với danh sách trong hệ thống |
| Danh Mục | ✅ | Tên danh mục phải khớp với danh sách trong hệ thống |
| Loại Máy | ❌ | Tên loại máy (nếu có) |
| Phong Cách | ❌ | Phong cách thiết kế |
| Hình Ảnh | ❌ | Đường dẫn hình ảnh (ví dụ: /images/product.jpg) |
| Mô Tả | ❌ | Mô tả sản phẩm |
| Trạng Thái | ❌ | "true" hoặc "false", mặc định "true" (hoạt động) |
| Màu Sắc | ✅ | Tên màu sắc phải khớp với danh sách (Đen, Trắng, Vàng, v.v.) |
| Kích Thước | ✅ | Tên kích thước (ví dụ: 42mm, 40mm) |
| Chất Liệu Dây | ✅ | Tên chất liệu (Dây Vải, Dây Da, Dây Inox, Dây Cao Su) |
| Giá Bán | ✅ | Giá bán (số, không có đơn vị) |
| Số Lượng Tồn | ✅ | Số lượng tồn (số nguyên) |
| Đường Kính | ❌ | Đường kính (số, đơn vị mm) |
| Độ Chịu Nước | ❌ | Độ chịu nước (số, đơn vị mét) |
| Bề Rộng Dây | ❌ | Bề rộng dây (số, đơn vị mm) |
| Trọng Lượng | ❌ | Trọng lượng (số, đơn vị gram) |
| Trạng Thái Chi Tiết | ❌ | "true" hoặc "false", mặc định "true" |
| Serial | ❌ | Mã serial sản phẩm |

## 📝 Ví Dụ Dữ Liệu

```
Tên Sản Phẩm | Thương Hiệu | Danh Mục | Loại Máy | Phong Cách | Hình Ảnh | Mô Tả | Trạng Thái | Màu Sắc | Kích Thước | Chất Liệu Dây | Giá Bán | Số Lượng Tồn | Đường Kính | Độ Chịu Nước | Bề Rộng Dây | Trọng Lượng | Trạng Thái Chi Tiết | Serial
Đồng Hồ Seiko Presage | Seiko | Đồng Hồ Đeo Tay | Automatic | Hiện Đại | /images/seiko-presage.jpg | Đồng hồ Seiko chính hãng | true | Đen | 42mm | Dây Inox | 5000000 | 10 | 42 | 10 | 20 | 120 | true | SKX-001
Đồng Hồ Orient | Orient | Đồng Hồ Đeo Tay | Automatic | Kinh Doanh | /images/orient-watch.jpg | Đồng hồ tự động | true | Trắng | 40mm | Dây Da | 3500000 | 5 | 40 | 5 | 18 | 100 | true | OAU-002
```

## 🎯 Quy Tắc Dữ Liệu

### 1. **Trạng Thái & Trạng Thái Chi Tiết**
- Nhập `true` hoặc `false` (không có dấu ngoặc)
- Hoặc từ khóa: `Hoạt động` / `Không hoạt động`
- Mặc định: `true` (Hoạt động)

### 2. **Giá Bán & Số Lượng**
- Chỉ nhập số (không có dấu tiền tệ hay ký tự khác)
- Ví dụ: `5000000` chứ không phải `5.000.000₫`

### 3. **Thương Hiệu, Danh Mục, Loại Máy**
- Tên PHẢI khớp chính xác với danh sách trong hệ thống
- VD: Nếu trong hệ thống có "Seiko" thì phải nhập "Seiko", không phải "seiko" hay "SEIKO"

### 4. **Màu Sắc, Kích Thước, Chất Liệu**
- Tên PHẢI khớp chính xác với danh sách trong hệ thống
- Một sản phẩm chỉ có 1 biến thể (1 hàng Excel = 1 sản phẩm + 1 biến thể)
- Nếu 1 sản phẩm có nhiều biến thể, thêm thêm hàng với cùng tên sản phẩm + biến thể khác

### 5. **Các Trường Số (Đường Kính, Độ Chịu Nước, v.v.)**
- Để trống nếu không có dữ liệu
- Hoặc nhập số (số thập phân được hỗ trợ)

## 🚀 Quy Trình Import

### Bước 1: Chuẩn Bị File Excel
- Tạo file `.xlsx` (Excel 2007 trở lên)
- Dòng 1: Tiêu đề cột (Header)
- Dòng 2 trở đi: Dữ liệu sản phẩm
- Lưu file

### Bước 2: Mở Trang Quản Lý Sản Phẩm
- Vào Admin > Quản Lý Sản Phẩm
- Nhấp nút **"Import Excel"** (bên cạnh nút "Thêm sản phẩm")

### Bước 3: Chọn File
- Bật modal "Chọn File Import Excel"
- Chọn file Excel của bạn
- Nhấp **"Tải File"**

### Bước 4: Preview & Chỉnh Sửa (Nếu Cần)
- Hệ thống hiển thị danh sách tất cả sản phẩm được import
- Mỗi sản phẩm là 1 thẻ (card) có thông tin tóm tắt
- Nếu muốn chỉnh sửa: Nhấp **"Chỉnh sửa"**
- Nếu muốn xóa khỏi danh sách: Nhấp **"Xóa"**

### Bước 5: Xác Nhận & Thêm Vào DB
- Sau khi chỉnh sửa xong
- Nhấp **"Xác Nhận Thêm Tất Cả"**
- Hệ thống sẽ thêm tất cả sản phẩm vào database

## ⚠️ Lưu Ý Quan Trọng

1. **Định Dạng Tên Cột**: Hệ thống hỗ trợ cả tiếng Việt có dấu
2. **Tương Thích Excel**: File phải là `.xlsx` hoặc `.xls`
3. **Không Có Hàng Trống**: Không để hàng trống giữa dữ liệu
4. **Khớp Dữ Liệu**: Tên thương hiệu, danh mục, v.v. phải khớp chính xác
5. **Preview Trước Khi Xác Nhận**: Kiểm tra kỹ dữ liệu trong bước preview
6. **Một Sản Phẩm = Một Biến Thể**: Mỗi hàng = 1 sản phẩm + 1 biến thể

## 📧 Hỗ Trợ

Nếu gặp lỗi:
- Kiểm tra lại định dạng tên cột
- Kiểm tra dữ liệu tham chiếu (Thương hiệu, Danh mục) có tồn tại không
- Xem console (F12) để xem chi tiết lỗi
- Thử lại với số lượng ít sản phẩm trước
