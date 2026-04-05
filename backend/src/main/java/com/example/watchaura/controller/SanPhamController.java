package com.example.watchaura.controller;

import com.example.watchaura.dto.SanPhamDTO;
import com.example.watchaura.dto.SanPhamRequest;
import com.example.watchaura.service.FileUploadService;
import com.example.watchaura.service.SanPhamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/api/san-pham")
@RequiredArgsConstructor
public class SanPhamController {

    private final SanPhamService sanPhamService;
    private final FileUploadService fileUploadService;
    // ============ API ENDPOINTS ============
    private static final int DEFAULT_PAGE_SIZE = 5;

    /**
     * GET: Lấy tất cả sản phẩm
     * URL: GET /api/san-pham
     */
    @GetMapping
    @ResponseBody
    public ResponseEntity<List<SanPhamDTO>> getAllSanPham() {
        try {
            List<SanPhamDTO> sanPhams = sanPhamService.getAllSanPham();
            return ResponseEntity.ok(sanPhams);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET: Tìm kiếm & phân trang sản phẩm (cho admin)
     * URL: GET /api/san-pham/search?keyword=...&trangThai=...&page=0&size=5
     */
    @GetMapping("/search")
    @ResponseBody
    public ResponseEntity<Page<SanPhamDTO>> searchPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean trangThai,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
            Page<SanPhamDTO> result = sanPhamService.searchPage(keyword, trangThai, pageable);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET: Kiểm tra mã sản phẩm đã tồn tại chưa (dùng khi thêm mới).
     * URL: GET /api/san-pham/check-ma?ma=SP001
     */
    @GetMapping("/check-ma")
    @ResponseBody
    public ResponseEntity<?> checkMaSanPham(@RequestParam String ma) {
        if (ma == null || (ma = ma.trim()).isEmpty()) {
            return ResponseEntity.ok(false);
        }
        boolean exists = sanPhamService.existsByMaSanPham(ma);
        return ResponseEntity.ok(exists);
    }

    /**
     * GET: Sinh mã sản phẩm tự động (dùng khi thêm mới).
     * URL: GET /api/san-pham/generate-ma
     */
    @GetMapping("/generate-ma")
    @ResponseBody
    public ResponseEntity<GenerateMaResponse> generateMaSanPham() {
        try {
            String ma = sanPhamService.generateMaSanPham();
            return ResponseEntity.ok(new GenerateMaResponse(ma, "Sinh mã thành công"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new GenerateMaResponse(null, "Lỗi sinh mã: " + e.getMessage()));
        }
    }

    /**
     * GET: Lấy sản phẩm theo ID
     * URL: GET /api/san-pham/{id}
     */
    @GetMapping("/{id}")
    @ResponseBody
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

    public static class GenerateMaResponse {
        public String maSanPham;
        public String message;

        public GenerateMaResponse(String maSanPham, String message) {
            this.maSanPham = maSanPham;
            this.message = message;
        }
    }

}