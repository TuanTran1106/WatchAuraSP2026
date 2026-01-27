package com.example.watchaura.controller;

import com.example.watchaura.entity.LoaiMay;
import com.example.watchaura.repository.LoaiMayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loai-may")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LoaiMayController {

    private final LoaiMayRepository loaiMayRepository;

    /**
     * GET: Lấy tất cả loại máy
     */
    @GetMapping
    public ResponseEntity<List<LoaiMay>> getAllLoaiMay() {
        try {
            List<LoaiMay> loaiMays = loaiMayRepository.findAll();
            return ResponseEntity.ok(loaiMays);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET: Lấy loại máy theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getLoaiMayById(@PathVariable Integer id) {
        try {
            return loaiMayRepository.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST: Tạo mới loại máy
     */
    @PostMapping
    public ResponseEntity<?> createLoaiMay(@RequestBody LoaiMay loaiMay) {
        try {
            LoaiMay saved = loaiMayRepository.save(loaiMay);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * PUT: Cập nhật loại máy
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateLoaiMay(@PathVariable Integer id, @RequestBody LoaiMay loaiMay) {
        try {
            return loaiMayRepository.findById(id)
                    .map(existing -> {
                        loaiMay.setId(id);
                        return ResponseEntity.ok(loaiMayRepository.save(loaiMay));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * DELETE: Xóa loại máy
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteLoaiMay(@PathVariable Integer id) {
        try {
            if (loaiMayRepository.existsById(id)) {
                loaiMayRepository.deleteById(id);
                return ResponseEntity.ok("Xóa thành công");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
