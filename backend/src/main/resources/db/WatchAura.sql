CREATE DATABASE WATCHAURA_STORE;
GO
USE WATCHAURA_STORE;
GO

CREATE TABLE ChucVu (
                        id INT IDENTITY(1,1) PRIMARY KEY,
                        ten_chuc_vu NVARCHAR(50) NOT NULL
);

CREATE TABLE KhachHang (
                           id INT IDENTITY(1,1) PRIMARY KEY,
                           ma_nguoi_dung NVARCHAR(30) UNIQUE NOT NULL,
                           ten_nguoi_dung NVARCHAR(100) NOT NULL,
                           email NVARCHAR(100) UNIQUE,
                           sdt NVARCHAR(20),
                           mat_khau NVARCHAR(255) NOT NULL,
                           gioi_tinh NVARCHAR(10),
                           ngay_sinh DATE,
                           hinh_anh NVARCHAR(255),
                           trang_thai BIT DEFAULT 1,
                           ngay_tao DATETIME DEFAULT GETDATE(),
                           id_chuc_vu INT,
                           FOREIGN KEY (id_chuc_vu) REFERENCES ChucVu(id)
);

CREATE TABLE DiaChi (
                        id INT IDENTITY(1,1) PRIMARY KEY,
                        id_khach_hang INT NOT NULL,
                        dia_chi_cu_the NVARCHAR(255),
                        phuong_xa NVARCHAR(100),
                        quan_huyen NVARCHAR(100),
                        tinh_thanh NVARCHAR(100),
                        mac_dinh BIT DEFAULT 0,
                        FOREIGN KEY (id_khach_hang) REFERENCES KhachHang(id)
);
CREATE TABLE ThuongHieu (
                            id INT IDENTITY(1,1) PRIMARY KEY,
                            ten_thuong_hieu NVARCHAR(100) NOT NULL
);
CREATE TABLE DanhMuc (
                         id INT IDENTITY(1,1) PRIMARY KEY,
                         ten_danh_muc NVARCHAR(100) NOT NULL
);
CREATE TABLE LoaiMay (
                         id INT IDENTITY(1,1) PRIMARY KEY,
                         ten_loai_may NVARCHAR(100)
);

CREATE TABLE MauSac (
                        id INT IDENTITY(1,1) PRIMARY KEY,
                        ten_mau_sac NVARCHAR(50)
);

CREATE TABLE KichThuoc (
                           id INT IDENTITY(1,1) PRIMARY KEY,
                           ten_kich_thuoc NVARCHAR(50)
);

CREATE TABLE ChatLieuDay (
                             id INT IDENTITY(1,1) PRIMARY KEY,
                             ten_chat_lieu NVARCHAR(100)
);
CREATE TABLE SanPham (
                         id INT IDENTITY(1,1) PRIMARY KEY,
                         ma_san_pham NVARCHAR(50) NOT NULL UNIQUE,
                         ten_san_pham NVARCHAR(255),
                         mo_ta NVARCHAR(MAX),
                         hinh_anh NVARCHAR(255),
                         id_thuong_hieu INT,
                         id_danh_muc INT,
                         phong_cach NVARCHAR(100),
                         trang_thai BIT DEFAULT 1,
                         ngay_tao DATETIME DEFAULT GETDATE(),
                         FOREIGN KEY (id_thuong_hieu) REFERENCES ThuongHieu(id),
                         FOREIGN KEY (id_danh_muc) REFERENCES DanhMuc(id)
);

CREATE TABLE SanPhamChiTiet (
                                id INT IDENTITY(1,1) PRIMARY KEY,
                                id_san_pham INT NOT NULL,
                                id_mau_sac INT,
                                id_kich_thuoc INT,
                                id_chat_lieu_day INT,
                                id_loai_may INT,
                                so_luong_ton INT,
                                gia_ban DECIMAL(18,2),
                                duong_kinh FLOAT,
                                do_chiu_nuoc INT,
                                be_rong_day FLOAT,
                                trong_luong FLOAT,
                                trang_thai BIT DEFAULT 1,
                                FOREIGN KEY (id_san_pham) REFERENCES SanPham(id),
                                FOREIGN KEY (id_mau_sac) REFERENCES MauSac(id),
                                FOREIGN KEY (id_kich_thuoc) REFERENCES KichThuoc(id),
                                FOREIGN KEY (id_chat_lieu_day) REFERENCES ChatLieuDay(id),
                                FOREIGN KEY (id_loai_may) REFERENCES LoaiMay(id)
);
CREATE TABLE GioHang (
                         id INT IDENTITY(1,1) PRIMARY KEY,
                         id_khach_hang INT NOT NULL,
                         trang_thai BIT DEFAULT 1,
                         ngay_tao DATETIME DEFAULT GETDATE(),
                         FOREIGN KEY (id_khach_hang) REFERENCES KhachHang(id)
);

CREATE TABLE GioHangChiTiet (
                                id INT IDENTITY(1,1) PRIMARY KEY,
                                id_gio_hang INT NOT NULL,
                                id_san_pham_chi_tiet INT NOT NULL,
                                so_luong INT,
                                FOREIGN KEY (id_gio_hang) REFERENCES GioHang(id),
                                FOREIGN KEY (id_san_pham_chi_tiet) REFERENCES SanPhamChiTiet(id)
);
CREATE TABLE Voucher (
                         id INT IDENTITY(1,1) PRIMARY KEY,
                         ma_voucher VARCHAR(50) NOT NULL UNIQUE,
                         ten_voucher NVARCHAR(255) NOT NULL,
                         mo_ta NVARCHAR(500),
                         loai_voucher VARCHAR(20) NOT NULL,
                         gia_tri DECIMAL(18,2) NOT NULL,
                         gia_tri_toi_da DECIMAL(18,2),
                         don_hang_toi_thieu DECIMAL(18,2) DEFAULT 0,
                         so_luong_tong INT NOT NULL,
                         so_luong_da_dung INT DEFAULT 0,
                         gioi_han_moi_user BIT NOT NULL DEFAULT 0,
                         ngay_bat_dau DATETIME NOT NULL,
                         ngay_ket_thuc DATETIME NOT NULL,
                         trang_thai BIT NOT NULL DEFAULT 1,
                         ngay_tao DATETIME DEFAULT GETDATE(),
                         ngay_cap_nhat DATETIME
);

CREATE TABLE Voucher_User (
                             id INT IDENTITY(1,1) PRIMARY KEY,
                             id_voucher INT NOT NULL,
                             id_khach_hang INT NOT NULL,
                             so_lan_da_dung INT NOT NULL DEFAULT 0,
                             lan_cuoi_dung DATETIME NULL,
                             CONSTRAINT UQ_VoucherUser UNIQUE (id_voucher, id_khach_hang),
                             FOREIGN KEY (id_voucher) REFERENCES Voucher(id),
                             FOREIGN KEY (id_khach_hang) REFERENCES KhachHang(id)
);
----------------------------
CREATE TABLE KhuyenMai (
                           id INT IDENTITY(1,1) PRIMARY KEY,
                           ma_khuyen_mai VARCHAR(50) UNIQUE,
                           ten_chuong_trinh NVARCHAR(255) NOT NULL,
                           mo_ta NVARCHAR(500),
                           loai_giam VARCHAR(20) NOT NULL,
                           gia_tri_giam DECIMAL(18,2) NOT NULL,
                           giam_toi_da DECIMAL(18,2),
                           ngay_bat_dau DATETIME NOT NULL,
                           ngay_ket_thuc DATETIME NOT NULL,
                           trang_thai BIT DEFAULT 1,
                           ngay_tao DATETIME DEFAULT GETDATE(),
                           ngay_cap_nhat DATETIME
);


CREATE TABLE SanPhamChiTietKhuyenMai (
                                         id INT IDENTITY(1,1) PRIMARY KEY,
                                         id_san_pham_chi_tiet INT,
                                         id_khuyen_mai INT,
                                         FOREIGN KEY (id_san_pham_chi_tiet) REFERENCES SanPhamChiTiet(id),
                                         FOREIGN KEY (id_khuyen_mai) REFERENCES KhuyenMai(id)
);
CREATE TABLE HoaDon (
                        id INT IDENTITY(1,1) PRIMARY KEY,
                        ma_don_hang VARCHAR(50) NOT NULL UNIQUE,
                        id_khach_hang INT NOT NULL,
                        id_nhan_vien INT NULL,
                        id_voucher INT NULL,
                        tong_tien_tam_tinh DECIMAL(18,2) NOT NULL,
                        tien_giam DECIMAL(18,2) DEFAULT 0,
                        tong_tien_thanh_toan DECIMAL(18,2) NOT NULL,
                        phuong_thuc_thanh_toan VARCHAR(50) NOT NULL,
                        loai_hoa_don VARCHAR(50) NOT NULL,
                        trang_thai BIT DEFAULT 1,
                        trang_thai_don_hang VARCHAR(50) NOT NULL,
                        ngay_dat DATETIME DEFAULT GETDATE(),
                        dia_chi NVARCHAR(255) NOT NULL,
                        ten_khach_hang NVARCHAR(100) NOT NULL,
                        sdt_khach_hang VARCHAR(20) NOT NULL,
                        ghi_chu NVARCHAR(255) NULL
	FOREIGN KEY (id_nhan_vien) REFERENCES KhachHang(id),
                        FOREIGN KEY (id_khach_hang) REFERENCES KhachHang(id),
                        FOREIGN KEY (id_voucher) REFERENCES Voucher(id),


);

CREATE TABLE DiaChiGiaoHang (
                                id INT IDENTITY(1,1) PRIMARY KEY,
                                id_hoa_don INT NOT NULL,
                                ten_nguoi_nhan NVARCHAR(100),
                                sdt_nguoi_nhan NVARCHAR(20),
                                dia_chi_cu_the NVARCHAR(255),
                                phuong_xa NVARCHAR(100),
                                quan_huyen NVARCHAR(100),
                                tinh_thanh NVARCHAR(100),
                                ghi_chu NVARCHAR(255),
                                FOREIGN KEY (id_hoa_don) REFERENCES HoaDon(id)
);
CREATE TABLE HoaDonChiTiet (
                               id INT IDENTITY(1,1) PRIMARY KEY,
                               id_hoa_don INT NOT NULL,
                               id_san_pham_chi_tiet INT NOT NULL,
                               so_luong INT NOT NULL,
                               don_gia DECIMAL(18,2) NOT NULL,
                               FOREIGN KEY (id_hoa_don) REFERENCES HoaDon(id),
                               FOREIGN KEY (id_san_pham_chi_tiet) REFERENCES SanPhamChiTiet(id)
);
CREATE TABLE Blog (
                      id INT IDENTITY(1,1) PRIMARY KEY,
                      tieu_de NVARCHAR(255),
                      noi_dung NVARCHAR(MAX),
                      hinh_anh NVARCHAR(255),
                      ngay_dang DATETIME DEFAULT GETDATE()
);

INSERT INTO ChucVu (ten_chuc_vu) VALUES
                                     (N'Admin'),
                                     (N'Nhân viên'),
                                     (N'Khách hàng');
INSERT INTO KhachHang
(ma_nguoi_dung, ten_nguoi_dung, email, sdt, mat_khau, gioi_tinh, ngay_sinh, id_chuc_vu)
VALUES
    ('ADMIN01', N'Phạm Quang Huy', 'admin@watchaura.vn', '0909123456', 'admin123', N'Nam', '1995-04-12', 1),
    ('NV001',   N'Nguyễn Minh Tuấn', 'tuan.nv@watchaura.vn', '0912345678', 'nv123', N'Nam', '1998-09-20', 2),
    ('KH001',   N'Trần Thị Mai', 'mai.tran@gmail.com', '0987654321', 'kh123', N'Nữ', '2000-03-15', 3);
INSERT INTO DiaChi
(id_khach_hang, dia_chi_cu_the, phuong_xa, quan_huyen, tinh_thanh, mac_dinh)
VALUES
    (1, N'12 Nguyễn Huệ', N'Bến Nghé', N'Quận 1', N'TP Hồ Chí Minh', 1),
    (2, N'88 Trần Hưng Đạo', N'Phường 7', N'Quận 5', N'TP Hồ Chí Minh', 1),
    (3, N'25 Lý Thường Kiệt', N'Phường Trần Phú', N'Hà Đông', N'Hà Nội', 1);
INSERT INTO ThuongHieu (ten_thuong_hieu) VALUES
                                             (N'Rolex'),
                                             (N'Omega'),
                                             (N'Seiko');
INSERT INTO DanhMuc (ten_danh_muc) VALUES
                                       (N'Đồng hồ hiện đại'),
                                       (N'Đồng hồ nam'),
                                       (N'Đồng hồ nữ')


    INSERT INTO MauSac (ten_mau_sac) VALUES
    (N'Đen'),
    (N'Bạc'),
    (N'Nâu');

INSERT INTO LoaiMay (ten_loai_may) VALUES
                                       (N'Automatic'),
                                       (N'Cơ tay'),
                                       (N'Quartz');
INSERT INTO KichThuoc (ten_kich_thuoc) VALUES
                                           (N'40mm'),
                                           (N'42mm'),
                                           (N'44mm');
INSERT INTO ChatLieuDay (ten_chat_lieu) VALUES
                                            (N'Da bò'),
                                            (N'Thép không gỉ'),
                                            (N'Cao su');
INSERT INTO SanPham
(ma_san_pham, ten_san_pham, mo_ta, id_thuong_hieu, id_danh_muc, phong_cach)
VALUES
    ('SP001', N'Rolex Submariner', N'Đồng hồ cơ cao cấp, chống nước tốt', 1, 1, N'Sang trọng'),
    ('SP002', N'Omega Seamaster', N'Đồng hồ thể thao chuyên nghiệp', 2, 1, N'Thể thao'),
    ('SP003', N'Seiko Presage', N'Đồng hồ cơ Nhật Bản cổ điển', 3, 3, N'Cổ điển');
INSERT INTO SanPhamChiTiet
(id_san_pham, id_mau_sac, id_kich_thuoc, id_chat_lieu_day, id_loai_may,
 so_luong_ton, gia_ban, duong_kinh, do_chiu_nuoc, be_rong_day, trong_luong)
VALUES
    (1, 1, 2, 2, 1, 10, 250000000, 42, 300, 20, 150),
    (2, 2, 1, 2, 1, 15, 180000000, 40, 300, 20, 145),
    (3, 3, 1, 1, 2, 20, 18000000, 40, 50, 18, 120);

INSERT INTO GioHang (id_khach_hang)
VALUES
    (3),
    (2),
    (1);
INSERT INTO GioHangChiTiet
(id_gio_hang, id_san_pham_chi_tiet, so_luong)
VALUES
    (3, 1, 1),
    (2, 2, 1),
    (1, 3, 1);

SELECT * FROM SanPhamChiTiet;

INSERT INTO Voucher
(ma_voucher, ten_voucher, loai_voucher, gia_tri, don_hang_toi_thieu,so_luong_tong, ngay_bat_dau, ngay_ket_thuc)
VALUES
    ('SALE10', N'Giảm 10%', 'PERCENT', 10, 5000000, 100, GETDATE(), DATEADD(DAY,30,GETDATE())),
    ('SALE500K', N'Giảm 500K', 'FIXED', 500000, 10000000, 50, GETDATE(), DATEADD(DAY,20,GETDATE())),
    ('VIP20', N'Khách VIP giảm 20%', 'PERCENT', 20, 20000000, 30, GETDATE(), DATEADD(DAY,60,GETDATE()));
INSERT INTO KhuyenMai
(ma_khuyen_mai, ten_chuong_trinh, loai_giam, gia_tri_giam,
 ngay_bat_dau, ngay_ket_thuc)
VALUES
    ('KM01', N'Khuyến mãi hè', 'PERCENT', 15, GETDATE(), DATEADD(DAY,30,GETDATE())),
    ('KM02', N'Sale cuối năm', 'FIXED', 2000000, GETDATE(), DATEADD(DAY,45,GETDATE())),
    ('KM03', N'Black Friday', 'PERCENT', 25, GETDATE(), DATEADD(DAY,10,GETDATE()));
INSERT INTO SanPhamChiTietKhuyenMai
(id_san_pham_chi_tiet, id_khuyen_mai)
VALUES
    (1, 1),
    (2, 3),
    (3, 2);

INSERT INTO HoaDon
(ma_don_hang, id_khach_hang, id_nhan_vien, id_voucher,
 tong_tien_tam_tinh, tien_giam, tong_tien_thanh_toan,
 phuong_thuc_thanh_toan, loai_hoa_don, trang_thai_don_hang,
 dia_chi, ten_khach_hang, sdt_khach_hang)
VALUES
    ('HD001', 3, 2, 1, 18000000, 1800000, 16200000, 'COD', 'ONLINE', 'HOAN_THANH',
     N'25 Lý Thường Kiệt, Hà Đông, Hà Nội', N'Trần Thị Mai', '0987654321'),
    ('HD002', 2, 2, NULL, 180000000, 0, 180000000, 'VNPAY', 'ONLINE', 'DANG_GIAO',
     N'88 Trần Hưng Đạo, Q5, TP HCM', N'Nguyễn Minh Tuấn', '0912345678'),
    ('HD003', 3, 2, 2, 250000000, 500000, 249500000, 'COD', 'ONLINE', 'CHO_XAC_NHAN',
     N'25 Lý Thường Kiệt, Hà Đông, Hà Nội', N'Trần Thị Mai', '0987654321');
INSERT INTO HoaDonChiTiet
(id_hoa_don, id_san_pham_chi_tiet, so_luong, don_gia)
VALUES
    (1, 3, 1, 18000000),
    (2, 2, 1, 180000000),
    (3, 1, 1, 250000000);
INSERT INTO DiaChiGiaoHang
(id_hoa_don, ten_nguoi_nhan, sdt_nguoi_nhan,
 dia_chi_cu_the, phuong_xa, quan_huyen, tinh_thanh)
VALUES
    (1, N'Trần Thị Mai', '0987654321', N'25 Lý Thường Kiệt', N'Trần Phú', N'Hà Đông', N'Hà Nội'),
    (2, N'Nguyễn Minh Tuấn', '0912345678', N'88 Trần Hưng Đạo', N'Phường 7', N'Quận 5', N'TP HCM'),
    (3, N'Trần Thị Mai', '0987654321', N'25 Lý Thường Kiệt', N'Trần Phú', N'Hà Đông', N'Hà Nội');
INSERT INTO Blog (tieu_de, noi_dung)
VALUES
    (N'Cách chọn đồng hồ cơ chuẩn', N'Hướng dẫn chọn đồng hồ cơ phù hợp cổ tay.'),
    (N'Rolex có gì đặc biệt?', N'Phân tích vì sao Rolex luôn giữ giá.'),
    (N'Bảo quản đồng hồ đúng cách', N'Mẹo giúp đồng hồ bền đẹp theo thời gian.');

SELECT * FROM ChucVu;
SELECT * FROM KhachHang;
SELECT * FROM DiaChi;
SELECT * FROM ThuongHieu;
SELECT * FROM DanhMuc;
SELECT * FROM LoaiMay;
SELECT * FROM MauSac;
SELECT * FROM KichThuoc;
SELECT * FROM ChatLieuDay;
SELECT * FROM SanPham;
SELECT * FROM SanPhamChiTiet;
SELECT * FROM GioHang;
SELECT * FROM GioHangChiTiet;
SELECT * FROM Voucher;
SELECT * FROM KhuyenMai;
SELECT * FROM SanPhamChiTietKhuyenMai;
SELECT * FROM HoaDon;
SELECT * FROM HoaDonChiTiet;
SELECT * FROM DiaChiGiaoHang;
SELECT * FROM Blog;
