package com.example.watchaura.controller;

import com.example.watchaura.dto.GioHangDTO;
import com.example.watchaura.dto.GioHangRequest;
import com.example.watchaura.service.GioHangService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gio-hang")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GioHangController {

    private final GioHangService gioHangService;

    @GetMapping
    public ResponseEntity<List<GioHangDTO>> getAll() {
        try {
            return ResponseEntity.ok(gioHangService.getAll());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(gioHangService.getById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống");
        }
    }

    @GetMapping("/khach-hang/{khachHangId}")
    public ResponseEntity<?> getByKhachHangId(@PathVariable Integer khachHangId) {
        try {
            return ResponseEntity.ok(gioHangService.getByKhachHangId(khachHangId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống");
        }
    }

    @PostMapping("/get-or-create")
    public ResponseEntity<?> getOrCreateCart(@RequestParam Integer khachHangId) {
        try {
            return ResponseEntity.ok(gioHangService.getOrCreateCart(khachHangId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody GioHangRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(gioHangService.create(request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody GioHangRequest request) {
        try {
            return ResponseEntity.ok(gioHangService.update(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            gioHangService.delete(id);
            return ResponseEntity.ok("Xóa giỏ hàng thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống");
        }
    }
}
