-- Migration: Thêm cột loai_hoan_tra vào bảng HoanTra
-- Ngày: 2026-04-27

-- Thêm cột loai_hoan_tra nếu chưa tồn tại
IF NOT EXISTS (SELECT * FROM sys.columns WHERE Object_ID = Object_ID('HoanTra') AND name = 'loai_hoan_tra')
BEGIN
    ALTER TABLE HoanTra
    ADD loai_hoan_tra NVARCHAR(20) NULL;
END
GO

-- Cập nhật dữ liệu cũ: mặc định là TRA_HANG cho các bản ghi hiện có
UPDATE HoanTra
SET loai_hoan_tra = 'TRA_HANG'
WHERE loai_hoan_tra IS NULL;
GO

-- Thêm constraint NOT NULL sau khi đã cập nhật dữ liệu
IF EXISTS (SELECT * FROM sys.columns WHERE Object_ID = Object_ID('HoanTra') AND name = 'loai_hoan_tra')
BEGIN
    -- Kiểm tra nếu cột nullable
    IF EXISTS (SELECT * FROM sys.columns WHERE Object_ID = Object_ID('HoanTra') AND name = 'loai_hoan_tra' AND is_nullable = 1)
    BEGIN
        ALTER TABLE HoanTra
        ALTER COLUMN loai_hoan_tra NVARCHAR(20) NOT NULL;
    END
END
GO

PRINT 'Migration thêm cột loai_hoan_tra thành công!';
