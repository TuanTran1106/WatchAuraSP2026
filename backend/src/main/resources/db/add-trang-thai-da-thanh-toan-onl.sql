-- Migration: Thêm trạng thái DA_THANH_TOAN_ONL (Thanh toán Onl thành công)
-- Dùng cho đơn hàng VNPay: tiền đã về tay nhưng đơn vẫn cần admin xác nhận
-- Flow: CHO_THANH_TOAN → DA_THANH_TOAN_ONL → DA_XAC_NHAN → DANG_GIAO → DA_GIAO → HOAN_THANH
--
-- Cột trang_thai_don_hang là VARCHAR(50) nên không cần ALTER TABLE
-- Chỉ cần cập nhật các đơn VNPAY đã thanh toán thành công nhưng đang ở trạng thái sai

-- Cập nhật các đơn VNPAY đã thanh toán (trạng thái CHO_XAC_NHAN) sang DA_THANH_TOAN_ONL
-- Chỉ cập nhật đơn có phương thức thanh toán VNPAY và đang ở trạng thái CHO_XAC_NHAN
UPDATE HoaDon
SET trang_thai_don_hang = 'DA_THANH_TOAN_ONL'
WHERE phuong_thuc_thanh_toan LIKE '%VNPAY%'
  AND trang_thai_don_hang = 'CHO_XAC_NHAN';
