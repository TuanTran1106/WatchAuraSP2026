package com.example.watchaura.controller;

import com.example.watchaura.dto.HoaDonChiTietDTO;
import com.example.watchaura.service.HoaDonChiTietService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hoa-don-chi-tiet")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class HoaDonChiTietController {

    private final HoaDonChiTietService hoaDonChiTietService;

    @GetMapping
    public ResponseEntity<List<HoaDonChiTietDTO>> getAll() {
        try {
            return ResponseEntity.ok(hoaDonChiTietService.getAll());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(hoaDonChiTietService.getById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống");
        }
    }

    @GetMapping("/hoa-don/{hoaDonId}")
    public ResponseEntity<?> getByHoaDonId(@PathVariable Integer hoaDonId) {
        try {
            return ResponseEntity.ok(hoaDonChiTietService.getByHoaDonId(hoaDonId));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống");
        }
    }
}
