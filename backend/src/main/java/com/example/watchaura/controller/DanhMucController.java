package com.example.watchaura.controller;

import com.example.watchaura.entity.DanhMuc;
import com.example.watchaura.service.DanhMucService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/danh-muc")
public class DanhMucController {

    @Autowired
    private DanhMucService danhMucService;

    @GetMapping
    public ResponseEntity<List<DanhMuc>> getAll() {
        return ResponseEntity.ok(danhMucService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DanhMuc> getById(
            @PathVariable Integer id) {

        return ResponseEntity.ok(danhMucService.getById(id));
    }

    @PostMapping
    public ResponseEntity<DanhMuc> create(
            @RequestBody DanhMuc danhMuc) {

        DanhMuc saved = danhMucService.create(danhMuc);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DanhMuc> update(
            @PathVariable Integer id,
            @RequestBody DanhMuc danhMuc) {

        return ResponseEntity.ok(
                danhMucService.update(id, danhMuc)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer id) {

        danhMucService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
