package com.example.watchaura.controller;

import com.example.watchaura.dto.SanPhamDTO;
import com.example.watchaura.dto.SanPhamRequest;
import com.example.watchaura.service.SanPhamService;
import com.example.watchaura.service.FileUploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/san-pham")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SanPhamController {

    private final SanPhamService sanPhamService;
    private final FileUploadService fileUploadService;

    /**
     * GET: Lấy tất cả sản phẩm
     * URL: GET /api/san-pham
     */
    @GetMapping
    public ResponseEntity<List<SanPhamDTO>> getAllSanPham() {
        try {
            List<SanPhamDTO> sanPhams = sanPhamService.getAllSanPham();
            return ResponseEntity.ok(sanPhams);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET: Lấy sản phẩm theo ID
     * URL: GET /api/san-pham/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getSanPhamById(@PathVariable Integer id) {
        try {
            SanPhamDTO sanPham = sanPhamService.getSanPhamById(id);
            return ResponseEntity.ok(sanPham);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi hệ thống");
        }
    }

    /**
     * POST: Tạo mới sản phẩm
     * URL: POST /api/san-pham
     * Body: SanPhamRequest (JSON)
     */
    @PostMapping
    public ResponseEntity<?> createSanPham(@Valid @RequestBody SanPhamRequest request) {
        try {
            SanPhamDTO sanPham = sanPhamService.createSanPham(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(sanPham);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi hệ thống");
        }
    }

    /**
     * PUT: Cập nhật sản phẩm
     * URL: PUT /api/san-pham/{id}
     * Body: SanPhamRequest (JSON)
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateSanPham(
            @PathVariable Integer id,
            @Valid @RequestBody SanPhamRequest request) {
        try {
            SanPhamDTO sanPham = sanPhamService.updateSanPham(id, request);
            return ResponseEntity.ok(sanPham);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi hệ thống");
        }
    }

    /**
     * DELETE: Xóa sản phẩm
     * URL: DELETE /api/san-pham/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSanPham(@PathVariable Integer id) {
        try {
            sanPhamService.deleteSanPham(id);
            return ResponseEntity.ok("Xóa sản phẩm thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi hệ thống");
        }
    }

    /**
     * POST: Upload ảnh sản phẩm
     * URL: POST /api/san-pham/upload
     * Body: form-data (file)
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String filePath = fileUploadService.uploadFile(file);
            return ResponseEntity.ok(new UploadResponse(filePath, "Upload thành công"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi khi upload file"));
        }
    }

    /**
     * PUT: Cập nhật ảnh sản phẩm
     * URL: PUT /api/san-pham/{id}/upload
     * Body: form-data (file)
     */
    @PutMapping("/{id}/upload")
    public ResponseEntity<?> updateProductImage(
            @PathVariable Integer id,
            @RequestParam("file") MultipartFile file) {
        try {
            String newFilePath = fileUploadService.uploadFile(file);
            sanPhamService.updateSanPhamImage(id, newFilePath);
            return ResponseEntity.ok(new UploadResponse(newFilePath, "Cập nhật ảnh thành công"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi khi cập nhật ảnh"));
        }
    }

    // Helper classes for response
    public static class UploadResponse {
        public String filePath;
        public String message;

        public UploadResponse(String filePath, String message) {
            this.filePath = filePath;
            this.message = message;
        }
    }

    public static class ErrorResponse {
        public String error;

        public ErrorResponse(String error) {
            this.error = error;
        }
    }
}