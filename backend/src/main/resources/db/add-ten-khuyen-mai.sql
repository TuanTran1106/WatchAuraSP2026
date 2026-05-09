-- Thêm cột ten_khuyen_mai vào bảng HoaDonChiTiet
-- để lưu tên chương trình khuyến mãi đang áp dụng cho sản phẩm

ALTER TABLE HoaDonChiTiet
ADD COLUMN ten_khuyen_mai VARCHAR(255) NULL;
