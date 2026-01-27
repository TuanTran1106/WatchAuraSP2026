package com.example.watchaura.controller;

import com.example.watchaura.entity.ThuongHieu;
import com.example.watchaura.service.ThuongHieuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/thuong-hieu")
public class ThuongHieuController {

    @Autowired
    private ThuongHieuService thuongHieuService;

    @GetMapping
    public ResponseEntity<List<ThuongHieu>> getAll() {
        return ResponseEntity.ok(thuongHieuService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ThuongHieu> getById(
            @PathVariable Integer id) {

        return ResponseEntity.ok(thuongHieuService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ThuongHieu> create(
            @RequestBody ThuongHieu thuongHieu) {

        ThuongHieu saved = thuongHieuService.create(thuongHieu);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ThuongHieu> update(
            @PathVariable Integer id,
            @RequestBody ThuongHieu thuongHieu) {

        return ResponseEntity.ok(
                thuongHieuService.update(id, thuongHieu)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer id) {

        thuongHieuService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

