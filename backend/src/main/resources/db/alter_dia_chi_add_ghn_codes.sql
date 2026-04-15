-- Add GHN master-data codes to DiaChi for shipping fee calculation
-- SQL Server

IF COL_LENGTH('DiaChi', 'ghn_province_id') IS NULL
BEGIN
    ALTER TABLE DiaChi ADD ghn_province_id INT NULL;
END

IF COL_LENGTH('DiaChi', 'ghn_district_id') IS NULL
BEGIN
    ALTER TABLE DiaChi ADD ghn_district_id INT NULL;
END

IF COL_LENGTH('DiaChi', 'ghn_ward_code') IS NULL
BEGIN
    ALTER TABLE DiaChi ADD ghn_ward_code NVARCHAR(20) NULL;
END

