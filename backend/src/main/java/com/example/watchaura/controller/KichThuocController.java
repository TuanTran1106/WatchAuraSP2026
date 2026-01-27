package com.example.watchaura.controller;

import com.example.watchaura.entity.KichThuoc;
import com.example.watchaura.repository.KichThuocRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kich-thuoc")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class KichThuocController {

    private final KichThuocRepository kichThuocRepository;

    /**
     * GET: Lấy tất cả kích thước
     */
    @GetMapping
    public ResponseEntity<List<KichThuoc>> getAllKichThuoc() {
        try {
            List<KichThuoc> kichThuocs = kichThuocRepository.findAll();
            return ResponseEntity.ok(kichThuocs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET: Lấy kích thước theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getKichThuocById(@PathVariable Integer id) {
        try {
            return kichThuocRepository.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST: Tạo mới kích thước
     */
    @PostMapping
    public ResponseEntity<?> createKichThuoc(@RequestBody KichThuoc kichThuoc) {
        try {
            KichThuoc saved = kichThuocRepository.save(kichThuoc);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * PUT: Cập nhật kích thước
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateKichThuoc(@PathVariable Integer id, @RequestBody KichThuoc kichThuoc) {
        try {
            return kichThuocRepository.findById(id)
                    .map(existing -> {
                        kichThuoc.setId(id);
                        return ResponseEntity.ok(kichThuocRepository.save(kichThuoc));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * DELETE: Xóa kích thước
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteKichThuoc(@PathVariable Integer id) {
        try {
            if (kichThuocRepository.existsById(id)) {
                kichThuocRepository.deleteById(id);
                return ResponseEntity.ok("Xóa thành công");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
