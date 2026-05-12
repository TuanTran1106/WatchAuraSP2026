-- Tạo bảng serial_loi để lưu các serial không thể lưu vào kho khi hoàn tiền
CREATE TABLE serial_loi (
    id INT IDENTITY(1,1) PRIMARY KEY,
    ma_serial VARCHAR(100) UNIQUE,
    hoan_tra_id INT,
    hoa_don_chi_tiet_id INT,
    san_pham_ten NVARCHAR(255),
    ly_do NVARCHAR(500),
    trang_thai VARCHAR(50) DEFAULT 'CHUA_XU_LY',
    nguoi_tao_id INT,
    ngay_tao DATETIME DEFAULT GETDATE(),
    ngay_xu_ly DATETIME,
    nguoi_xu_ly_id INT,
    CONSTRAINT fk_serial_loi_hoan_tra FOREIGN KEY (hoan_tra_id) REFERENCES hoan_tra(id),
    CONSTRAINT fk_serial_loi_hoa_don_chi_tiet FOREIGN KEY (hoa_don_chi_tiet_id) REFERENCES hoa_don_chi_tiet(id),
    CONSTRAINT fk_serial_loi_nguoi_tao FOREIGN KEY (nguoi_tao_id) REFERENCES khach_hang(id),
    CONSTRAINT fk_serial_loi_nguoi_xu_ly FOREIGN KEY (nguoi_xu_ly_id) REFERENCES khach_hang(id)
);

-- Tạo index để tìm kiếm nhanh hơn
CREATE INDEX idx_serial_loi_ma_serial ON serial_loi(ma_serial);
CREATE INDEX idx_serial_loi_trang_thai ON serial_loi(trang_thai);
CREATE INDEX idx_serial_loi_ngay_tao ON serial_loi(ngay_tao DESC);
