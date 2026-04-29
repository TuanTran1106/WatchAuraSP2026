package com.example.watchaura.service.impl;

import com.example.watchaura.service.ExcelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExcelServiceImpl implements ExcelService {

    private static final String EXCEL_CONTENT_TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
    private static final String EXCEL_CONTENT_TYPE_XLS = "application/vnd.ms-excel";

    @Override
    public boolean isValidExcelFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        String contentType = file.getContentType();
        return EXCEL_CONTENT_TYPE_XLSX.equals(contentType) 
            || EXCEL_CONTENT_TYPE_XLS.equals(contentType)
            || file.getOriginalFilename() != null 
               && (file.getOriginalFilename().endsWith(".xlsx") || file.getOriginalFilename().endsWith(".xls"));
    }

    @Override
    public ExcelReadResult readSerialFromExcel(MultipartFile file) {
        List<String> serials = new ArrayList<>();
        List<String> emptySerials = new ArrayList<>();
        List<String> duplicateSerials = new ArrayList<>();
        List<String> previewSerials = new ArrayList<>();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            
            for (int i = 0; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                Cell cell = row.getCell(0);
                if (cell == null) continue;
                
                String serial = getCellValueAsString(cell);
                if (serial == null || serial.trim().isEmpty()) {
                    emptySerials.add("Dòng " + (i + 1) + ": serial trống");
                    continue;
                }
                
                serial = serial.trim();
                
                if (serials.contains(serial)) {
                    duplicateSerials.add("Dòng " + (i + 1) + ": serial '" + serial + "' bị trùng trong file");
                } else {
                    serials.add(serial);
                    previewSerials.add(serial); // Lưu tất cả serial để preview
                }
            }
        } catch (IOException e) {
            log.error("Lỗi khi đọc file Excel: {}", e.getMessage());
            throw new RuntimeException("Lỗi khi đọc file Excel: " + e.getMessage());
        }

        return new ExcelReadResult(serials, emptySerials, duplicateSerials, previewSerials);
    }

    @Override
    public byte[] generateTemplateExcel() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Serial");
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            
            Row headerRow = sheet.createRow(0);
            Cell headerCell = headerRow.createCell(0);
            headerCell.setCellValue("Serial");
            headerCell.setCellStyle(headerStyle);
            
            for (int i = 1; i <= 10; i++) {
                Row row = sheet.createRow(i);
                row.createCell(0).setCellValue("SERIAL_" + String.format("%04d", i));
            }
            
            sheet.setColumnWidth(0, 5000);
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            log.error("Lỗi khi tạo template Excel: {}", e.getMessage());
            throw new RuntimeException("Lỗi khi tạo template Excel: " + e.getMessage());
        }
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return null;
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return null;
        }
    }
}
