/*
package com.example.watchaura.controller;

import com.example.watchaura.entity.ChucVu;
import com.example.watchaura.service.ChucVuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chuc-vu")
public class ChucVuController {

    @Autowired
    private ChucVuService chucVuService;

    @GetMapping
    public ResponseEntity<List<ChucVu>> getAll() {
        return ResponseEntity.ok(chucVuService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ChucVu> getById(
            @PathVariable Integer id) {

        return ResponseEntity.ok(chucVuService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ChucVu> create(
            @RequestBody ChucVu chucVu) {

        ChucVu saved = chucVuService.create(chucVu);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ChucVu> update(
            @PathVariable Integer id,
            @RequestBody ChucVu chucVu) {

        return ResponseEntity.ok(
                chucVuService.update(id, chucVu)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer id) {

        chucVuService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

*/
