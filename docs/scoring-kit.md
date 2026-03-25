# Scoring Kit - Checklist, Test Data, Slide Outline

## 1) Test checklist de lay diem

### Dashboard nang cao

- [ ] Dang nhap admin vao duoc dashboard.
- [ ] KPI "Doanh thu da giao" hien thi dung va khong bi null.
- [ ] Bieu do 7 ngay hien thi du 7 cot.
- [ ] Khi khong co doanh thu, bieu do van render an toan.
- [ ] Top 5 san pham ban chay hien thi dung thu tu.
- [ ] Ti le hoan tat/huy don hien thi duoc khi co va khong co du lieu.

### Bao cao doanh thu PDF

- [ ] Nhan nut xuat PDF tai trang hoa don tai duoc file.
- [ ] File mo duoc tren trinh doc PDF thong dung.
- [ ] Bao cao hien thi dung khoang ngay da loc.
- [ ] Danh sach don trong PDF khop voi du lieu tren he thong.
- [ ] Tong doanh thu trong PDF khop tong tien cac dong.
- [ ] Truong hop khong co du lieu van xuat PDF hop le.

### Regression nhanh

- [ ] Trang danh sach hoa don van tim kiem/loc/phan trang binh thuong.
- [ ] Xuat PDF theo tung hoa don (`/admin/hoa-don/{id}/pdf`) van hoat dong.
- [ ] Dashboard admin van tai duoc trong che do AJAX layout.

## 2) Bo du lieu mau de demo (goi y)

Dung 5 don hang cho de demo:

1. Don A: `DA_GIAO`, ngay hom nay, 2 san pham.
2. Don B: `DA_GIAO`, ngay hom qua, 1 san pham.
3. Don C: `DA_GIAO`, 3 ngay truoc, 3 san pham.
4. Don D: `DA_HUY`, 2 ngay truoc.
5. Don E: `CHO_XAC_NHAN`, hom nay.

Yeu cau du lieu:

- Co it nhat 3 san pham lap lai giua cac don de top san pham co y nghia.
- Co ca don da giao va da huy de KPI ty le ro rang.
- Co don nam trong va ngoai khoang ngay loc de test PDF.

## 3) Slide outline ngan (5 slide)

1. **Bai toan**
   - He thong ban dong ho can dashboard quan tri va bao cao doanh thu.
2. **Giai phap**
   - Dashboard nang cao: KPI + bieu do 7 ngay + top san pham.
   - Xuat bao cao doanh thu PDF theo khoang ngay.
3. **Demo**
   - Chay Flow 1 (dashboard) va Flow 2 (xuat PDF).
4. **Ket qua**
   - Admin theo doi hieu suat ban hang nhanh.
   - Bao cao doi soat de dang.
5. **Huong mo rong**
   - Export Excel
   - Email bao cao tu dong theo lich
   - Canh bao doanh thu bat thuong
