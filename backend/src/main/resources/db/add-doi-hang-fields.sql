-- =====================================================
-- Database Migration: Add DOI_HANG fields to HoanTraChiTiet
-- Purpose: Support for "Đổi hàng" (Exchange) workflow
-- =====================================================

-- Add columns to hoan_tra_chi_tiet table
ALTER TABLE hoan_tra_chi_tiet
ADD COLUMN IF NOT EXISTS hinh_anh VARCHAR(1000);

ALTER TABLE hoan_tra_chi_tiet
ADD COLUMN IF NOT EXISTS serial_moi VARCHAR(100);

ALTER TABLE hoan_tra_chi_tiet
ADD COLUMN IF NOT EXISTS serial_cu_loi BOOLEAN DEFAULT FALSE;

-- =====================================================
-- Optional: Add indexes for better query performance
-- =====================================================
CREATE INDEX IF NOT EXISTS idx_htct_serial_moi ON hoan_tra_chi_tiet(serial_moi);
CREATE INDEX IF NOT EXISTS idx_htct_serial_cu_loi ON hoan_tra_chi_tiet(serial_cu_loi);
