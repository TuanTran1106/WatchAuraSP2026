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
public class SanPhamChiTietApiController {

    private final SanPhamChiTietService sanPhamChiTietService;

    @GetMapping("/san-pham/{sanPhamId}")
    public ResponseEntity<List<SanPhamChiTietDTO>> getBySanPhamId(@PathVariable Integer sanPhamId) {
        List<SanPhamChiTietDTO> list = sanPhamChiTietService.getSanPhamChiTietBySanPhamId(sanPhamId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SanPhamChiTietDTO> getById(@PathVariable Integer id) {
        SanPhamChiTietDTO dto = sanPhamChiTietService.getSanPhamChiTietById(id);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<SanPhamChiTietDTO> create(@Valid @RequestBody SanPhamChiTietRequest request) {
        SanPhamChiTietDTO saved = sanPhamChiTietService.createSanPhamChiTiet(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SanPhamChiTietDTO> update(
            @PathVariable Integer id,
            @Valid @RequestBody SanPhamChiTietRequest request) {
        SanPhamChiTietDTO updated = sanPhamChiTietService.updateSanPhamChiTiet(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        sanPhamChiTietService.deleteSanPhamChiTiet(id);
        return ResponseEntity.noContent().build();
    }
}
