package com.example.watchaura.controller;

import com.example.watchaura.entity.MauSac;
import com.example.watchaura.repository.MauSacRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mau-sac")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MauSacController {

    private final MauSacRepository mauSacRepository;

    /**
     * GET: Lấy tất cả màu sắc
     */
    @GetMapping
    public ResponseEntity<List<MauSac>> getAllMauSac() {
        try {
            List<MauSac> mauSacs = mauSacRepository.findAll();
            return ResponseEntity.ok(mauSacs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET: Lấy màu sắc theo ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getMauSacById(@PathVariable Integer id) {
        try {
            return mauSacRepository.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST: Tạo mới màu sắc
     */
    @PostMapping
    public ResponseEntity<?> createMauSac(@RequestBody MauSac mauSac) {
        try {
            MauSac saved = mauSacRepository.save(mauSac);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * PUT: Cập nhật màu sắc
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMauSac(@PathVariable Integer id, @RequestBody MauSac mauSac) {
        try {
            return mauSacRepository.findById(id)
                    .map(existing -> {
                        mauSac.setId(id);
                        return ResponseEntity.ok(mauSacRepository.save(mauSac));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * DELETE: Xóa màu sắc
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMauSac(@PathVariable Integer id) {
        try {
            if (mauSacRepository.existsById(id)) {
                mauSacRepository.deleteById(id);
                return ResponseEntity.ok("Xóa thành công");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
