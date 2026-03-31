-- Chuyển "Loại máy" từ SanPhamChiTiet sang SanPham (SQL Server).
-- Chạy thủ công trên database WATCHAURA_STORE2 (hoặc DB của bạn) khi deploy.

-- 1) Thêm cột trên bảng sản phẩm
IF NOT EXISTS (
    SELECT 1 FROM sys.columns
    WHERE object_id = OBJECT_ID(N'dbo.SanPham') AND name = N'id_loai_may'
)
BEGIN
    ALTER TABLE dbo.SanPham ADD id_loai_may INT NULL;
END
GO

-- 2) Gán giá trị từ biến thể (một giá trị đại diện mỗi sản phẩm khi có nhiều dòng)
UPDATE sp
SET sp.id_loai_may = x.id_loai_may
FROM dbo.SanPham sp
INNER JOIN (
    SELECT id_san_pham, MIN(id_loai_may) AS id_loai_may
    FROM dbo.SanPhamChiTiet
    WHERE id_loai_may IS NOT NULL
    GROUP BY id_san_pham
) x ON sp.id = x.id_san_pham;
GO

-- 3) Khóa ngoại SanPham -> LoaiMay
IF NOT EXISTS (SELECT 1 FROM sys.foreign_keys WHERE name = N'FK_SanPham_LoaiMay')
BEGIN
    ALTER TABLE dbo.SanPham
    ADD CONSTRAINT FK_SanPham_LoaiMay FOREIGN KEY (id_loai_may) REFERENCES dbo.LoaiMay(id);
END
GO

-- 4) Xóa cột cũ trên biến thể (tùy chọn, sau khi đã backup)
-- Tìm tên FK thực tế: SELECT name FROM sys.foreign_keys WHERE parent_object_id = OBJECT_ID('dbo.SanPhamChiTiet');
-- Ví dụ:
-- ALTER TABLE dbo.SanPhamChiTiet DROP CONSTRAINT <tên_FK_id_loai_may>;
-- ALTER TABLE dbo.SanPhamChiTiet DROP COLUMN id_loai_may;
