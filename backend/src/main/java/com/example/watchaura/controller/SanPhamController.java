package com.example.watchaura.controller;


import com.example.watchaura.dto.SanPhamDTO;
import com.example.watchaura.dto.SanPhamRequest;
import com.example.watchaura.service.SanPhamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/san-pham")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SanPhamController {

    private final SanPhamService sanPhamService;

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
}