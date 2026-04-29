package com.example.watchaura.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ExcelService {

    boolean isValidExcelFile(MultipartFile file);

    ExcelReadResult readSerialFromExcel(MultipartFile file);

    byte[] generateTemplateExcel();

    class ExcelReadResult {
        private final List<String> serials;
        private final List<String> emptySerials;
        private final List<String> duplicateSerials;
        private final List<String> previewSerials;

        public ExcelReadResult(List<String> serials, List<String> emptySerials,
                               List<String> duplicateSerials, List<String> previewSerials) {
            this.serials = serials;
            this.emptySerials = emptySerials;
            this.duplicateSerials = duplicateSerials;
            this.previewSerials = previewSerials;
        }

        public List<String> getSerials() { return serials; }
        public List<String> getEmptySerials() { return emptySerials; }
        public List<String> getDuplicateSerials() { return duplicateSerials; }
        public List<String> getPreviewSerials() { return previewSerials; }
        public boolean hasErrors() { return !emptySerials.isEmpty() || !duplicateSerials.isEmpty(); }
    }
}
