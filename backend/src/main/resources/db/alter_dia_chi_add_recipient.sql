-- Optional recipient per saved address (checkout / quick edit)
-- SQL Server

IF COL_LENGTH('DiaChi', 'ten_nguoi_nhan') IS NULL
BEGIN
    ALTER TABLE DiaChi ADD ten_nguoi_nhan NVARCHAR(100) NULL;
END

IF COL_LENGTH('DiaChi', 'sdt_nguoi_nhan') IS NULL
BEGIN
    ALTER TABLE DiaChi ADD sdt_nguoi_nhan NVARCHAR(20) NULL;
END
