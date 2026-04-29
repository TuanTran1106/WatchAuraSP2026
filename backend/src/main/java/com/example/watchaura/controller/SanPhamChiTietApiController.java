package com.example.watchaura.controller;

import com.example.watchaura.dto.ImportSerialResponse;
import com.example.watchaura.dto.SanPhamChiTietDTO;
import com.example.watchaura.dto.SanPhamChiTietRequest;
import com.example.watchaura.dto.SerialSanPhamRequest;
import com.example.watchaura.service.ExcelService;
import com.example.watchaura.service.SanPhamChiTietService;
import com.example.watchaura.service.SerialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/san-pham-chi-tiet")
@RequiredArgsConstructor
public class SanPhamChiTietApiController {

    private final SanPhamChiTietService sanPhamChiTietService;
    private final ExcelService excelService;
    private final SerialService serialService;

    @GetMapping("/san-pham/{sanPhamId}")
    public ResponseEntity<List<SanPhamChiTietDTO>> getBySanPhamId(@PathVariable Integer sanPhamId) {
        List<SanPhamChiTietDTO> list = sanPhamChiTietService.getSanPhamChiTietBySanPhamId(sanPhamId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SanPhamChiTietDTO> getById(@PathVariable Integer id) {
        SanPhamChiTietDTO dto = sanPhamChiTietService.getSanPhamChiTietById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/check-duplicate")
    public ResponseEntity<?> checkDuplicate(
            @RequestParam("idSanPham") Integer idSanPham,
            @RequestParam(value = "idMauSac", required = false) Integer idMauSac,
            @RequestParam(value = "idKichThuoc", required = false) Integer idKichThuoc,
            @RequestParam(value = "idChatLieuDay", required = false) Integer idChatLieuDay,
            @RequestParam(value = "excludeId", required = false) Integer excludeId) {
        try {
            boolean exists = sanPhamChiTietService.existsByVariant(idSanPham, idMauSac, idKichThuoc, idChatLieuDay, excludeId);
            return ResponseEntity.ok(java.util.Map.of("exists", exists));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<SanPhamChiTietDTO> create(@Valid @RequestBody SanPhamChiTietRequest request) {
        SanPhamChiTietDTO saved = sanPhamChiTietService.createSanPhamChiTiet(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/create-with-excel")
    public ResponseEntity<?> createWithExcel(
            @RequestParam("idSanPham") Integer idSanPham,
            @RequestParam(value = "idMauSac", required = false) Integer idMauSac,
            @RequestParam(value = "idKichThuoc", required = false) Integer idKichThuoc,
            @RequestParam(value = "idChatLieuDay", required = false) Integer idChatLieuDay,
            @RequestParam(value = "giaBan") String giaBan,
            @RequestParam(value = "soLuongTon", defaultValue = "0") Integer soLuongTon,
            @RequestParam(value = "duongKinh", required = false) Double duongKinh,
            @RequestParam(value = "doChiuNuoc", required = false) Integer doChiuNuoc,
            @RequestParam(value = "beRongDay", required = false) Double beRongDay,
            @RequestParam(value = "trongLuong", required = false) Double trongLuong,
            @RequestParam(value = "trangThai", defaultValue = "true") Boolean trangThai,
            @RequestParam(value = "file", required = false) MultipartFile file) {
        try {
            SanPhamChiTietRequest request = new SanPhamChiTietRequest();
            request.setIdSanPham(idSanPham);
            request.setIdMauSac(idMauSac);
            request.setIdKichThuoc(idKichThuoc);
            request.setIdChatLieuDay(idChatLieuDay);
            request.setGiaBan(new java.math.BigDecimal(giaBan));
            request.setSoLuongTon(soLuongTon);
            request.setDuongKinh(duongKinh);
            request.setDoChiuNuoc(doChiuNuoc);
            request.setBeRongDay(beRongDay);
            request.setTrongLuong(trongLuong);
            request.setTrangThai(trangThai);

            List<String> serials = null;
            if (file != null && !file.isEmpty()) {
                if (!excelService.isValidExcelFile(file)) {
                    return ResponseEntity.badRequest().body(
                            ImportSerialResponse.builder()
                                    .success(false)
                                    .message("File không hợp lệ. Chỉ chấp nhận file .xlsx hoặc .xls")
                                    .build()
                    );
                }
                ExcelService.ExcelReadResult result = excelService.readSerialFromExcel(file);
                if (result.hasErrors()) {
                    List<String> allErrors = new java.util.ArrayList<>();
                    allErrors.addAll(result.getEmptySerials());
                    allErrors.addAll(result.getDuplicateSerials());
                    return ResponseEntity.badRequest().body(
                            ImportSerialResponse.builder()
                                    .success(false)
                                    .message("File có lỗi: " + allErrors.size() + " dòng")
                                    .totalSerials(result.getSerials().size())
                                    .errorCount(allErrors.size())
                                    .errorSerials(allErrors)
                                    .previewSerials(result.getPreviewSerials())
                                    .build()
                    );
                }
                serials = result.getSerials();
            }

            request.setSerials(serials);
            SanPhamChiTietDTO saved = sanPhamChiTietService.createSanPhamChiTiet(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ImportSerialResponse.builder()
                            .success(false)
                            .message("Lỗi khi tạo sản phẩm chi tiết: " + e.getMessage())
                            .build()
            );
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<SanPhamChiTietDTO> update(
            @PathVariable Integer id,
            @Valid @RequestBody SanPhamChiTietRequest request) {
        SanPhamChiTietDTO updated = sanPhamChiTietService.updateSanPhamChiTiet(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        sanPhamChiTietService.deleteSanPhamChiTiet(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/serial/preview")
    public ResponseEntity<ImportSerialResponse> previewSerialFromExcel(
            @RequestParam("file") MultipartFile file) {
        try {
            if (!excelService.isValidExcelFile(file)) {
                return ResponseEntity.badRequest().body(
                        ImportSerialResponse.builder()
                                .success(false)
                                .message("File không hợp lệ. Chỉ chấp nhận file .xlsx hoặc .xls")
                                .build()
                );
            }

            ExcelService.ExcelReadResult result = excelService.readSerialFromExcel(file);

            List<String> allErrors = new java.util.ArrayList<>();
            allErrors.addAll(result.getEmptySerials());
            allErrors.addAll(result.getDuplicateSerials());

            ImportSerialResponse response = ImportSerialResponse.builder()
                    .success(result.hasErrors())
                    .message(result.hasErrors()
                            ? "Có lỗi trong file: " + allErrors.size() + " dòng"
                            : "Đọc file thành công")
                    .totalSerials(result.getSerials().size())
                    .successCount(result.getSerials().size())
                    .errorCount(allErrors.size())
                    .errorSerials(allErrors)
                    .previewSerials(result.getPreviewSerials())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ImportSerialResponse.builder()
                            .success(false)
                            .message("Lỗi khi đọc file Excel: " + e.getMessage())
                            .build()
            );
        }
    }

    @PostMapping("/serial/validate")
    public ResponseEntity<ImportSerialResponse> validateSerials(@RequestBody SerialSanPhamRequest request) {
        try {
            ImportSerialResponse response = serialService.validateSerials(request.getSerials());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ImportSerialResponse.builder()
                            .success(false)
                            .message("Lỗi khi validate serial: " + e.getMessage())
                            .build()
            );
        }
    }

    @PostMapping("/serial/import")
    public ResponseEntity<ImportSerialResponse> importSerialFromExcel(
            @RequestParam("idSanPhamChiTiet") Integer idSanPhamChiTiet,
            @RequestParam("file") MultipartFile file) {
        try {
            if (!excelService.isValidExcelFile(file)) {
                return ResponseEntity.badRequest().body(
                        ImportSerialResponse.builder()
                                .success(false)
                                .message("File không hợp lệ. Chỉ chấp nhận file .xlsx hoặc .xls")
                                .build()
                );
            }

            ExcelService.ExcelReadResult result = excelService.readSerialFromExcel(file);

            if (result.hasErrors()) {
                List<String> allErrors = new java.util.ArrayList<>();
                allErrors.addAll(result.getEmptySerials());
                allErrors.addAll(result.getDuplicateSerials());

                return ResponseEntity.badRequest().body(
                        ImportSerialResponse.builder()
                                .success(false)
                                .message("File có lỗi: " + allErrors.size() + " dòng")
                                .totalSerials(result.getSerials().size())
                                .successCount(0)
                                .errorCount(allErrors.size())
                                .errorSerials(allErrors)
                                .previewSerials(result.getPreviewSerials())
                                .build()
                );
            }

            ImportSerialResponse response = serialService.importSerialsToVariant(
                    idSanPhamChiTiet, result.getSerials());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ImportSerialResponse.builder()
                            .success(false)
                            .message("Lỗi khi import serial: " + e.getMessage())
                            .build()
            );
        }
    }

    @PostMapping("/serial/import-direct")
    public ResponseEntity<ImportSerialResponse> importSerialDirect(@RequestBody SerialSanPhamRequest request) {
        try {
            ImportSerialResponse response = serialService.importSerialsToVariant(
                    request.getIdSanPhamChiTiet(), request.getSerials());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ImportSerialResponse.builder()
                            .success(false)
                            .message("Lỗi khi import serial: " + e.getMessage())
                            .build()
            );
        }
    }

    @GetMapping("/serial/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        try {
            byte[] template = excelService.generateTemplateExcel();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "mau_import_serial.xlsx");
            return new ResponseEntity<>(template, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
