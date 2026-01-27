package com.example.watchaura.controller;

import com.example.watchaura.dto.GioHangChiTietDTO;
import com.example.watchaura.dto.GioHangChiTietRequest;
import com.example.watchaura.service.GioHangChiTietService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gio-hang-chi-tiet")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GioHangChiTietController {

    private final GioHangChiTietService gioHangChiTietService;

    @GetMapping
    public ResponseEntity<List<GioHangChiTietDTO>> getAll() {
        try {
            return ResponseEntity.ok(gioHangChiTietService.getAll());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(gioHangChiTietService.getById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống");
        }
    }

    @GetMapping("/gio-hang/{gioHangId}")
    public ResponseEntity<?> getByGioHangId(@PathVariable Integer gioHangId) {
        try {
            return ResponseEntity.ok(gioHangChiTietService.getByGioHangId(gioHangId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống");
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody GioHangChiTietRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(gioHangChiTietService.create(request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @Valid @RequestBody GioHangChiTietRequest request) {
        try {
            return ResponseEntity.ok(gioHangChiTietService.update(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            gioHangChiTietService.delete(id);
            return ResponseEntity.ok("Xóa chi tiết giỏ hàng thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống");
        }
    }
}
