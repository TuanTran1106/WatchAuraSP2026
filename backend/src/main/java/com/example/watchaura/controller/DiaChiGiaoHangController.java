package com.example.watchaura.controller;

import com.example.watchaura.dto.DiaChiGiaoHangDTO;
import com.example.watchaura.dto.DiaChiGiaoHangRequest;
import com.example.watchaura.service.DiaChiGiaoHangService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dia-chi-giao-hang")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DiaChiGiaoHangController {

    private final DiaChiGiaoHangService diaChiGiaoHangService;

    @GetMapping
    public ResponseEntity<List<DiaChiGiaoHangDTO>> getAll() {
        try {
            return ResponseEntity.ok(diaChiGiaoHangService.getAll());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(diaChiGiaoHangService.getById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống");
        }
    }

    @GetMapping("/hoa-don/{hoaDonId}")
    public ResponseEntity<?> getByHoaDonId(@PathVariable Integer hoaDonId) {
        try {
            return ResponseEntity.ok(diaChiGiaoHangService.getByHoaDonId(hoaDonId));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống");
        }
    }

    @PostMapping("/hoa-don/{hoaDonId}")
    public ResponseEntity<?> create(@PathVariable Integer hoaDonId, @RequestBody DiaChiGiaoHangRequest request) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(diaChiGiaoHangService.create(hoaDonId, request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody DiaChiGiaoHangRequest request) {
        try {
            return ResponseEntity.ok(diaChiGiaoHangService.update(id, request));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        try {
            diaChiGiaoHangService.delete(id);
            return ResponseEntity.ok("Xóa địa chỉ giao hàng thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống");
        }
    }
}
