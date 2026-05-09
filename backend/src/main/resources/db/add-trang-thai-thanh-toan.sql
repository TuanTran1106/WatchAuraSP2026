-- Add trang_thai_thanh_toan column to HoaDon table
-- Values: CHUA_THANH_TOAN (default), DA_THANH_TOAN, THANH_TOAN_LOI
-- NOTE: This field is for reference only. The actual display logic is:
-- - If order status is HOAN_THANH/DA THANH TOAN/DA_THANH_TOAN -> "Đã thanh toán"
-- - If order status is DA_HUY -> "Thanh toán lỗi"
-- - Otherwise -> "Chưa thanh toán"

ALTER TABLE HoaDon
ADD COLUMN trang_thai_thanh_toan VARCHAR(50) DEFAULT 'CHUA_THANH_TOAN';
