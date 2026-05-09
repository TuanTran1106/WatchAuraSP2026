-- Thêm cột don_gia_goc vào bảng HoaDonChiTiet để lưu giá gốc (chưa khuyến mãi) tại thời điểm đặt hàng
ALTER TABLE HoaDonChiTiet ADD COLUMN don_gia_goc DECIMAL(18, 2) NULL;

-- Cập nhật các bản ghi cũ: điền don_gia_goc = don_gia (giữ nguyên giá cũ)
UPDATE HoaDonChiTiet SET don_gia_goc = don_gia WHERE don_gia_goc IS NULL;
