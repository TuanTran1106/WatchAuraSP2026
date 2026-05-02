package com.example.watchaura.controller;

import com.example.watchaura.dto.HoanTraDTO;
import com.example.watchaura.dto.HoanTraExcelRow;
import com.example.watchaura.dto.HoanTraRequest;
import com.example.watchaura.dto.ImportHoanTraResponse;
import com.example.watchaura.entity.KhachHang;
import com.example.watchaura.repository.KhachHangRepository;
import com.example.watchaura.service.FileUploadService;
import com.example.watchaura.service.HoanTraService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.watchaura.controller.AuthController.SESSION_CURRENT_USER_ID;

@RestController
@RequestMapping("/api/hoan-tra")
@RequiredArgsConstructor
public class HoanTraApiController {

    private final HoanTraService hoanTraService;
    private final KhachHangRepository khachHangRepository;
    private final FileUploadService fileUploadService;

    @GetMapping
    public ResponseEntity<List<HoanTraDTO>> getAllHoanTra() {
        List<HoanTraDTO> list = hoanTraService.getAllHoanTra();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/paged")
    public ResponseEntity<Map<String, Object>> getHoanTraPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) String keyword) {
        try {
            Map<String, Object> result = hoanTraService.getHoanTraPaged(page, size, trangThai, keyword);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<HoanTraDTO> getHoanTraById(@PathVariable Integer id) {
        HoanTraDTO dto = hoanTraService.getHoanTraById(id);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/ma/{maHoanTra}")
    public ResponseEntity<HoanTraDTO> getHoanTraByMa(@PathVariable String maHoanTra) {
        HoanTraDTO dto = hoanTraService.getHoanTraByMaHoanTra(maHoanTra);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/trang-thai/{trangThai}")
    public ResponseEntity<List<HoanTraDTO>> getHoanTraByTrangThai(@PathVariable String trangThai) {
        List<HoanTraDTO> list = hoanTraService.getHoanTraByTrangThai(trangThai);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/khach-hang/{khachHangId}")
    public ResponseEntity<List<HoanTraDTO>> getHoanTraByKhachHang(@PathVariable Integer khachHangId) {
        List<HoanTraDTO> list = hoanTraService.getHoanTraByKhachHangId(khachHangId);
        return ResponseEntity.ok(list);
    }

    @PostMapping
    public ResponseEntity<?> createHoanTra(
            @Valid @RequestBody HoanTraRequest request,
            HttpSession session) {
        try {
            HoanTraDTO created = hoanTraService.createHoanTra(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ImportHoanTraResponse.builder()
                            .success(false)
                            .message("Lỗi khi tạo hoàn trả: " + e.getMessage())
                            .build()
            );
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateHoanTra(
            @PathVariable Integer id,
            @Valid @RequestBody HoanTraRequest request) {
        try {
            HoanTraDTO updated = hoanTraService.updateHoanTra(id, request);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ImportHoanTraResponse.builder()
                            .success(false)
                            .message("Lỗi khi cập nhật hoàn trả: " + e.getMessage())
                            .build()
            );
        }
    }

    @PutMapping("/{id}/xu-ly")
    public ResponseEntity<?> xuLyHoanTra(
            @PathVariable Integer id,
            @RequestParam(required = false) String ghiChuXuLy,
            HttpSession session) {
        try {
            Integer nhanVienId = (Integer) session.getAttribute(SESSION_CURRENT_USER_ID);
            if (nhanVienId == null) {
                return ResponseEntity.badRequest().body(
                        ImportHoanTraResponse.builder()
                                .success(false)
                                .message("Bạn chưa đăng nhập!")
                                .build()
                );
            }
            HoanTraDTO updated = hoanTraService.xuLyHoanTra(id, ghiChuXuLy, nhanVienId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ImportHoanTraResponse.builder()
                            .success(false)
                            .message("Lỗi khi xử lý hoàn trả: " + e.getMessage())
                            .build()
            );
        }
    }

    @PutMapping("/{id}/tu-choi")
    public ResponseEntity<?> tuChoiHoanTra(
            @PathVariable Integer id,
            @RequestParam(required = false) String ghiChuXuLy,
            HttpSession session) {
        try {
            Integer nhanVienId = (Integer) session.getAttribute(SESSION_CURRENT_USER_ID);
            if (nhanVienId == null) {
                return ResponseEntity.badRequest().body(
                        ImportHoanTraResponse.builder()
                                .success(false)
                                .message("Bạn chưa đăng nhập!")
                                .build()
                );
            }
            HoanTraDTO updated = hoanTraService.tuChoiHoanTra(id, ghiChuXuLy, nhanVienId);
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ImportHoanTraResponse.builder()
                            .success(false)
                            .message("Lỗi khi từ chối hoàn trả: " + e.getMessage())
                            .build()
            );
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHoanTra(@PathVariable Integer id) {
        try {
            hoanTraService.deleteHoanTra(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                    ImportHoanTraResponse.builder()
                            .success(false)
                            .message("Lỗi khi xóa hoàn trả: " + e.getMessage())
                            .build()
            );
        }
    }

    @PostMapping("/import/preview")
    public ResponseEntity<?> previewImportExcel(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        ImportHoanTraResponse.builder()
                                .success(false)
                                .message("File không được để trống")
                                .build()
                );
            }

            String filename = file.getOriginalFilename();
            if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
                return ResponseEntity.badRequest().body(
                        ImportHoanTraResponse.builder()
                                .success(false)
                                .message("File không hợp lệ. Chỉ chấp nhận file .xlsx hoặc .xls")
                                .build()
                );
            }

            ImportHoanTraResponse response = hoanTraService.previewImportExcel(file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ImportHoanTraResponse.builder()
                            .success(false)
                            .message("Lỗi khi đọc file Excel: " + e.getMessage())
                            .build()
            );
        }
    }

    @PostMapping("/import")
    public ResponseEntity<?> importFromExcel(
            @RequestParam("file") MultipartFile file,
            HttpSession session) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(
                        ImportHoanTraResponse.builder()
                                .success(false)
                                .message("File không được để trống")
                                .build()
                );
            }

            String filename = file.getOriginalFilename();
            if (filename == null || (!filename.endsWith(".xlsx") && !filename.endsWith(".xls"))) {
                return ResponseEntity.badRequest().body(
                        ImportHoanTraResponse.builder()
                                .success(false)
                                .message("File không hợp lệ. Chỉ chấp nhận file .xlsx hoặc .xls")
                                .build()
                );
            }

            Integer nhanVienId = (Integer) session.getAttribute(SESSION_CURRENT_USER_ID);
            if (nhanVienId == null) {
                return ResponseEntity.badRequest().body(
                        ImportHoanTraResponse.builder()
                                .success(false)
                                .message("Bạn chưa đăng nhập!")
                                .build()
                );
            }

            ImportHoanTraResponse response = hoanTraService.importFromExcel(file, nhanVienId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ImportHoanTraResponse.builder()
                            .success(false)
                            .message("Lỗi khi import: " + e.getMessage())
                            .build()
            );
        }
    }

    @GetMapping("/import/template")
    public ResponseEntity<byte[]> downloadTemplate() {
        try {
            byte[] template = hoanTraService.generateHoanTraTemplateExcel();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.parseMediaType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            headers.setContentDispositionFormData("attachment", "mau_import_hoan_tra.xlsx");
            return new ResponseEntity<>(template, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/validate-excel")
    public ResponseEntity<?> validateExcelData(@RequestParam("file") MultipartFile file) {
        try {
            List<HoanTraExcelRow> rows = hoanTraService.validateExcelData(file);
            return ResponseEntity.ok(rows);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ImportHoanTraResponse.builder()
                            .success(false)
                            .message("Lỗi khi validate file Excel: " + e.getMessage())
                            .build()
            );
        }
    }

    @GetMapping("/hoa-don/{id}/serial")
    public ResponseEntity<?> getSerialForReturn(@PathVariable Integer id, HttpSession session) {
        try {
            Integer khachHangId = (Integer) session.getAttribute("currentUserId");
            Map<String, Object> result = hoanTraService.getSerialCoTheTra(id, khachHangId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/hoan-tra/{id}/serial-da-ban")
    public ResponseEntity<?> getSerialDaBan(@PathVariable Integer id) {
        try {
            HoanTraDTO hoanTra = hoanTraService.getHoanTraById(id);
            if (hoanTra == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Không tìm thấy hoàn trả");
                return ResponseEntity.ok(error);
            }
            return ResponseEntity.ok(hoanTra);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/upload-image")
    public ResponseEntity<?> uploadHoanTraImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "File không được để trống");
                return ResponseEntity.badRequest().body(error);
            }

            String filename = file.getOriginalFilename();
            if (filename != null && !filename.matches(".*\\.(jpg|jpeg|png|gif|webp)$")) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Chỉ chấp nhận file ảnh (jpg, jpeg, png, gif, webp)");
                return ResponseEntity.badRequest().body(error);
            }

            String filePath = fileUploadService.uploadFile(file);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("filePath", filePath);
            result.put("message", "Upload ảnh thành công");
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Lỗi khi upload ảnh: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{id}/buoc-xu-ly")
    public ResponseEntity<?> getBuocXuLyNext(@PathVariable Integer id) {
        try {
            Map<String, Object> result = hoanTraService.getBuocXuLyNext(id);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PostMapping("/{id}/duyet")
    public ResponseEntity<?> duyetDonTraHang(
            @PathVariable Integer id,
            @RequestParam(required = false, defaultValue = "false") Boolean themVaoKho,
            @RequestParam(required = false) String ghiChuXuLy,
            HttpSession session) {
        try {
            Integer nhanVienId = (Integer) session.getAttribute(SESSION_CURRENT_USER_ID);
            if (nhanVienId == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Bạn chưa đăng nhập!");
                return ResponseEntity.badRequest().body(error);
            }
            HoanTraDTO result = hoanTraService.duyetDonTraHang(id, themVaoKho, nhanVienId, ghiChuXuLy);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Duyệt đơn thành công!");
            response.put("data", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * API xử lý đổi hàng - Cập nhật serial và hoàn tất đổi hàng
     * Áp dụng cho trạng thái CHỌN SERIAL MỚI
     */
    @PostMapping("/{id}/doi-hang/hoan-tat")
    public ResponseEntity<?> xuLyDoiHangHoanTat(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> requestBody,
            HttpSession session) {
        try {
            Integer nhanVienId = (Integer) session.getAttribute(SESSION_CURRENT_USER_ID);
            if (nhanVienId == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Bạn chưa đăng nhập!");
                return ResponseEntity.badRequest().body(error);
            }

            // Parse serialsMoi Map<String, String> -> Map<Integer, String>
            @SuppressWarnings("unchecked")
            Map<String, String> serialsMoiRaw = (Map<String, String>) requestBody.get("serialsMoi");
            Map<Integer, String> serialsMoi = new HashMap<>();
            if (serialsMoiRaw != null) {
                for (Map.Entry<String, String> entry : serialsMoiRaw.entrySet()) {
                    serialsMoi.put(Integer.parseInt(entry.getKey()), entry.getValue());
                }
            }

            // Parse serialCuLoi Map<String, Boolean> -> Map<Integer, Boolean>
            @SuppressWarnings("unchecked")
            Map<String, Boolean> serialCuLoiRaw = (Map<String, Boolean>) requestBody.get("serialCuLoi");
            Map<Integer, Boolean> serialCuLoi = new HashMap<>();
            if (serialCuLoiRaw != null) {
                for (Map.Entry<String, Boolean> entry : serialCuLoiRaw.entrySet()) {
                    serialCuLoi.put(Integer.parseInt(entry.getKey()), entry.getValue());
                }
            }

            String ghiChuXuLy = (String) requestBody.get("ghiChuXuLy");

            HoanTraDTO result = hoanTraService.xuLyDoiHangHoanTat(
                    id, serialsMoi, nhanVienId, ghiChuXuLy);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Đã cấp serial mới cho khách!");
            response.put("data", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * API hoàn tất đổi hàng - Bước 2: Xử lý serial cũ
     * Hỏi admin: Serial cũ có lưu vào kho không hay lỗi?
     * Áp dụng cho trạng thái ĐÃ ĐỔI → KẾT THÚC
     */
    @PostMapping("/{id}/doi-hang/ket-thuc")
    public ResponseEntity<?> hoanTatDoiHang(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> requestBody,
            HttpSession session) {
        try {
            Integer nhanVienId = (Integer) session.getAttribute(SESSION_CURRENT_USER_ID);
            if (nhanVienId == null) {
                Map<String, Object> error = new HashMap<>();
                error.put("success", false);
                error.put("message", "Bạn chưa đăng nhập!");
                return ResponseEntity.badRequest().body(error);
            }

            // Parse serialCuLoi Map<String, Boolean> -> Map<Integer, Boolean>
            @SuppressWarnings("unchecked")
            Map<String, Boolean> serialCuLoiRaw = (Map<String, Boolean>) requestBody.get("serialCuLoi");
            Map<Integer, Boolean> serialCuLoi = new HashMap<>();
            if (serialCuLoiRaw != null) {
                for (Map.Entry<String, Boolean> entry : serialCuLoiRaw.entrySet()) {
                    serialCuLoi.put(Integer.parseInt(entry.getKey()), entry.getValue());
                }
            }

            String ghiChuXuLy = (String) requestBody.get("ghiChuXuLy");

            HoanTraDTO result = hoanTraService.hoanTatDoiHang(
                    id, serialCuLoi, nhanVienId, ghiChuXuLy);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Hoàn tất đổi hàng thành công!");
            response.put("data", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    /**
     * API lấy serial có thể đổi cho sản phẩm
     */
    @GetMapping("/serial/doi/{hoaDonId}/{sanPhamChiTietId}")
    public ResponseEntity<?> getSerialCoTheDoi(
            @PathVariable Integer hoaDonId,
            @PathVariable Integer sanPhamChiTietId) {
        try {
            Map<String, Object> result = hoanTraService.getSerialCoTheDoi(hoaDonId, sanPhamChiTietId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
