-- =============================================
-- Migration: Add PhieuNhapKho and ChiTietPhieuNhapKho tables
-- Date: 2026-04-28
-- Description: Bảng phiếu nhập kho từ hoàn trả và chi tiết
-- =============================================

-- Tạo bảng Phiếu Nhập Kho
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='PhieuNhapKho' AND xtype='U')
BEGIN
    CREATE TABLE PhieuNhapKho (
        id INT IDENTITY(1,1) PRIMARY KEY,
        ma_phieu NVARCHAR(50) NOT NULL UNIQUE,
        loai_phieu NVARCHAR(50) NOT NULL,
        id_hoan_tra INT NULL,
        id_nhan_vien INT NOT NULL,
        so_tien_hoan DECIMAL(18,2) NULL,
        phuong_thuc_hoan_tien NVARCHAR(50) NULL,
        ghi_chu NVARCHAR(500) NULL,
        ngay_tao DATETIME DEFAULT GETDATE(),
        CONSTRAINT FK_PhieuNhapKho_HoanTra FOREIGN KEY (id_hoan_tra) REFERENCES HoanTra(id),
        CONSTRAINT FK_PhieuNhapKho_NhanVien FOREIGN KEY (id_nhan_vien) REFERENCES KhachHang(id)
    );
    
    PRINT 'Created table: PhieuNhapKho';
END
ELSE
BEGIN
    PRINT 'Table PhieuNhapKho already exists';
END
GO

-- Tạo bảng Chi Tiết Phiếu Nhập Kho
IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='ChiTietPhieuNhapKho' AND xtype='U')
BEGIN
    CREATE TABLE ChiTietPhieuNhapKho (
        id INT IDENTITY(1,1) PRIMARY KEY,
        id_phieu_nhap_kho INT NOT NULL,
        id_san_pham_chi_tiet INT NOT NULL,
        id_serial INT NULL,
        so_luong INT NOT NULL DEFAULT 1,
        don_gia DECIMAL(18,2) NULL,
        ghi_chu NVARCHAR(255) NULL,
        CONSTRAINT FK_ChiTietPhieuNhapKho_PhieuNhapKho FOREIGN KEY (id_phieu_nhap_kho) REFERENCES PhieuNhapKho(id),
        CONSTRAINT FK_ChiTietPhieuNhapKho_SanPhamChiTiet FOREIGN KEY (id_san_pham_chi_tiet) REFERENCES SanPhamChiTiet(id),
        CONSTRAINT FK_ChiTietPhieuNhapKho_Serial FOREIGN KEY (id_serial) REFERENCES SerialSanPham(id)
    );
    
    PRINT 'Created table: ChiTietPhieuNhapKho';
END
ELSE
BEGIN
    PRINT 'Table ChiTietPhieuNhapKho already exists';
END
GO

-- Thêm các cột mới vào bảng HoanTra nếu chưa có
IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('dbo.HoanTra') AND name = 'phuong_thuc_hoan_tien')
BEGIN
    ALTER TABLE HoanTra ADD phuong_thuc_hoan_tien NVARCHAR(50) NULL;
    PRINT 'Added column: phuong_thuc_hoan_tien to HoanTra';
END
ELSE
BEGIN
    PRINT 'Column phuong_thuc_hoan_tien already exists in HoanTra';
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('dbo.HoanTra') AND name = 'so_tien_hoan_thuc_te')
BEGIN
    ALTER TABLE HoanTra ADD so_tien_hoan_thuc_te DECIMAL(18,2) NULL;
    PRINT 'Added column: so_tien_hoan_thuc_te to HoanTra';
END
ELSE
BEGIN
    PRINT 'Column so_tien_hoan_thuc_te already exists in HoanTra';
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('dbo.HoanTra') AND name = 'ghi_chu_hoan_tien')
BEGIN
    ALTER TABLE HoanTra ADD ghi_chu_hoan_tien NVARCHAR(500) NULL;
    PRINT 'Added column: ghi_chu_hoan_tien to HoanTra';
END
ELSE
BEGIN
    PRINT 'Column ghi_chu_hoan_tien already exists in HoanTra';
END
GO

IF NOT EXISTS (SELECT * FROM sys.columns WHERE object_id = OBJECT_ID('dbo.HoanTra') AND name = 'ngay_hoan_tien')
BEGIN
    ALTER TABLE HoanTra ADD ngay_hoan_tien DATETIME NULL;
    PRINT 'Added column: ngay_hoan_tien to HoanTra';
END
ELSE
BEGIN
    PRINT 'Column ngay_hoan_tien already exists in HoanTra';
END
GO

-- Tạo index cho bảng PhieuNhapKho
IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_PhieuNhapKho_ma_phieu' AND object_id = OBJECT_ID('dbo.PhieuNhapKho'))
BEGIN
    CREATE UNIQUE INDEX IX_PhieuNhapKho_ma_phieu ON PhieuNhapKho(ma_phieu);
    PRINT 'Created index: IX_PhieuNhapKho_ma_phieu';
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_PhieuNhapKho_id_hoan_tra' AND object_id = OBJECT_ID('dbo.PhieuNhapKho'))
BEGIN
    CREATE INDEX IX_PhieuNhapKho_id_hoan_tra ON PhieuNhapKho(id_hoan_tra);
    PRINT 'Created index: IX_PhieuNhapKho_id_hoan_tra';
END

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name = 'IX_ChiTietPhieuNhapKho_id_phieu' AND object_id = OBJECT_ID('dbo.ChiTietPhieuNhapKho'))
BEGIN
    CREATE INDEX IX_ChiTietPhieuNhapKho_id_phieu ON ChiTietPhieuNhapKho(id_phieu_nhap_kho);
    PRINT 'Created index: IX_ChiTietPhieuNhapKho_id_phieu';
END
GO

PRINT 'Migration completed successfully!';
GO
