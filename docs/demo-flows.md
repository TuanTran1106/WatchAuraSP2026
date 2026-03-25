# Demo Flows - WatchAura (Admin + User)

## Flow 1: Dashboard doanh thu nang cao

Muc tieu: Chung minh he thong da co dashboard thong ke nang cao, khong chi dem so luong.

1. Dang nhap bang tai khoan admin.
2. Mo trang ` /admin `.
3. Trinh bay 4 KPI:
   - Tong khach hang
   - Tong hoa don
   - Tong san pham
   - Doanh thu da giao
4. Giai thich bieu do doanh thu 7 ngay gan nhat:
   - Cot cao = doanh thu ngay cao
   - Cot thap = doanh thu ngay thap
5. Trinh bay KPI chat luong:
   - Ti le hoan tat don
   - Ti le huy don
6. Trinh bay danh sach Top 5 san pham ban chay.
7. Ket luan gia tri:
   - Admin ra quyet dinh nhap hang/khuyen mai nhanh hon
   - Theo doi suc khoe kinh doanh theo ngay

## Flow 2: Xuat bao cao doanh thu PDF

Muc tieu: Chung minh nghiep vu bao cao thuc te cho cua hang.

1. Vao trang ` /admin/hoa-don `.
2. O khu "Xuat bao cao doanh thu":
   - Chon ` Tu ngay `
   - Chon ` Den ngay `
3. Bam nut ` Xuat bao cao doanh thu `.
4. Mo file PDF vua tai ve va trinh bay:
   - Khoang thoi gian bao cao
   - Trang thai loc (mac dinh DA_GIAO)
   - Danh sach don theo tieu chi
   - Tong doanh thu toan bo danh sach
5. Ket luan gia tri:
   - Bao cao phuc vu doi soat ke toan
   - In/luu file de nop cho giang vien khi demo

## Script thuyet trinh 90 giay (goi y)

- "He thong khong chi CRUD ma da co lop phan tich kinh doanh o dashboard."
- "Bieu do 7 ngay + top san pham giup quan ly thay xu huong ban hang ngay lap tuc."
- "Ngoai hoa don tung don, he thong da xuat duoc bao cao doanh thu PDF theo khoang ngay."
- "Day la tinh nang tiep can sat bai toan cua hang thuc te."
