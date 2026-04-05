-- Khuyến mãi trên bảng SanPham — chạy trên đúng database trong application.properties (vd. WATCHAURA_STORE2).
-- Kiểm tra: SELECT DB_NAME();
--
-- 1) Đổi tên nếu trước đó đã tạo cột km_* (bỏ qua nếu báo không tìm thấy cột).
-- 2) Thêm cột mới nếu chưa có (dùng sys.columns, tránh lỗi COL_LENGTH + schema).

BEGIN TRY
    IF COL_LENGTH('SanPham', 'km_loai_giam') IS NOT NULL
       AND COL_LENGTH('SanPham', 'loai_khuyen_mai') IS NULL
        EXEC sp_rename 'SanPham.km_loai_giam', 'loai_khuyen_mai', 'COLUMN';
END TRY BEGIN CATCH END CATCH;

BEGIN TRY
    IF COL_LENGTH('SanPham', 'km_gia_tri_giam') IS NOT NULL
       AND COL_LENGTH('SanPham', 'gia_tri_khuyen_mai') IS NULL
        EXEC sp_rename 'SanPham.km_gia_tri_giam', 'gia_tri_khuyen_mai', 'COLUMN';
END TRY BEGIN CATCH END CATCH;

BEGIN TRY
    IF COL_LENGTH('SanPham', 'km_ngay_bat_dau') IS NOT NULL
       AND COL_LENGTH('SanPham', 'ngay_bat_dau_khuyen_mai') IS NULL
        EXEC sp_rename 'SanPham.km_ngay_bat_dau', 'ngay_bat_dau_khuyen_mai', 'COLUMN';
END TRY BEGIN CATCH END CATCH;

BEGIN TRY
    IF COL_LENGTH('SanPham', 'km_ngay_ket_thuc') IS NOT NULL
       AND COL_LENGTH('SanPham', 'ngay_ket_thuc_khuyen_mai') IS NULL
        EXEC sp_rename 'SanPham.km_ngay_ket_thuc', 'ngay_ket_thuc_khuyen_mai', 'COLUMN';
END TRY BEGIN CATCH END CATCH;

BEGIN TRY
    IF COL_LENGTH('SanPham', 'km_trang_thai') IS NOT NULL
       AND COL_LENGTH('SanPham', 'trang_thai_khuyen_mai') IS NULL
        EXEC sp_rename 'SanPham.km_trang_thai', 'trang_thai_khuyen_mai', 'COLUMN';
END TRY BEGIN CATCH END CATCH;

-- Thêm cột (chạy từng ALTER nếu cột chưa tồn tại)
IF NOT EXISTS (
    SELECT 1 FROM sys.columns c
    INNER JOIN sys.tables t ON t.object_id = c.object_id
    INNER JOIN sys.schemas s ON s.schema_id = t.schema_id
    WHERE s.name = N'dbo' AND t.name = N'SanPham' AND c.name = N'loai_khuyen_mai'
)
    ALTER TABLE dbo.SanPham ADD loai_khuyen_mai NVARCHAR(20) NULL;

IF NOT EXISTS (
    SELECT 1 FROM sys.columns c
    INNER JOIN sys.tables t ON t.object_id = c.object_id
    INNER JOIN sys.schemas s ON s.schema_id = t.schema_id
    WHERE s.name = N'dbo' AND t.name = N'SanPham' AND c.name = N'gia_tri_khuyen_mai'
)
    ALTER TABLE dbo.SanPham ADD gia_tri_khuyen_mai DECIMAL(18, 2) NULL;

IF NOT EXISTS (
    SELECT 1 FROM sys.columns c
    INNER JOIN sys.tables t ON t.object_id = c.object_id
    INNER JOIN sys.schemas s ON s.schema_id = t.schema_id
    WHERE s.name = N'dbo' AND t.name = N'SanPham' AND c.name = N'ngay_bat_dau_khuyen_mai'
)
    ALTER TABLE dbo.SanPham ADD ngay_bat_dau_khuyen_mai DATETIME2 NULL;

IF NOT EXISTS (
    SELECT 1 FROM sys.columns c
    INNER JOIN sys.tables t ON t.object_id = c.object_id
    INNER JOIN sys.schemas s ON s.schema_id = t.schema_id
    WHERE s.name = N'dbo' AND t.name = N'SanPham' AND c.name = N'ngay_ket_thuc_khuyen_mai'
)
    ALTER TABLE dbo.SanPham ADD ngay_ket_thuc_khuyen_mai DATETIME2 NULL;

IF NOT EXISTS (
    SELECT 1 FROM sys.columns c
    INNER JOIN sys.tables t ON t.object_id = c.object_id
    INNER JOIN sys.schemas s ON s.schema_id = t.schema_id
    WHERE s.name = N'dbo' AND t.name = N'SanPham' AND c.name = N'trang_thai_khuyen_mai'
)
    ALTER TABLE dbo.SanPham ADD trang_thai_khuyen_mai BIT NULL;
