package com.example.watchaura.controller;

import com.example.watchaura.dto.SanPhamChiTietDTO;
import com.example.watchaura.dto.SanPhamChiTietRequest;
import com.example.watchaura.service.SanPhamChiTietService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/san-pham-chi-tiet")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SanPhamChiTietController {

    private final SanPhamChiTietService sanPhamChiTietService;

    /**
     * GET: Lấy tất cả sản phẩm chi tiết
     * URL: GET /api/san-pham-chi-tiet
     */
    @GetMapping
    public ResponseEntity<List<SanPhamChiTietDTO>> getAllSanPhamChiTiet() {
        try {
            List<SanPhamChiTietDTO> list = sanPhamChiTietService.getAllSanPhamChiTiet();
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET: Lấy sản phẩm chi tiết theo ID
     * URL: GET /api/san-pham-chi-tiet/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getSanPhamChiTietById(@PathVariable Integer id) {
        try {
            SanPhamChiTietDTO spct = sanPhamChiTietService.getSanPhamChiTietById(id);
            return ResponseEntity.ok(spct);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi hệ thống");
        }
    }

    /**
     * POST: Tạo mới sản phẩm chi tiết
     * URL: POST /api/san-pham-chi-tiet
     * Body: SanPhamChiTietRequest (JSON)
     */
    @PostMapping
    public ResponseEntity<?> createSanPhamChiTiet(@Valid @RequestBody SanPhamChiTietRequest request) {
        try {
            SanPhamChiTietDTO spct = sanPhamChiTietService.createSanPhamChiTiet(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(spct);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi hệ thống");
        }
    }

    /**
     * PUT: Cập nhật sản phẩm chi tiết
     * URL: PUT /api/san-pham-chi-tiet/{id}
     * Body: SanPhamChiTietRequest (JSON)
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateSanPhamChiTiet(
            @PathVariable Integer id,
            @Valid @RequestBody SanPhamChiTietRequest request) {
        try {
            SanPhamChiTietDTO spct = sanPhamChiTietService.updateSanPhamChiTiet(id, request);
            return ResponseEntity.ok(spct);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi hệ thống");
        }
    }

    /**
     * DELETE: Xóa sản phẩm chi tiết
     * URL: DELETE /api/san-pham-chi-tiet/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSanPhamChiTiet(@PathVariable Integer id) {
        try {
            sanPhamChiTietService.deleteSanPhamChiTiet(id);
            return ResponseEntity.ok("Xóa sản phẩm chi tiết thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi hệ thống");
        }
    }
}