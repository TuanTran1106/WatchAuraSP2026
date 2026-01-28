/*
package com.example.watchaura.controller;

import com.example.watchaura.entity.DiaChi;
import com.example.watchaura.service.DiaChiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dia-chi")
public class DiaChiController {

    @Autowired
    private DiaChiService diaChiService;


    @GetMapping("/khach-hang/{khachHangId}")
    public ResponseEntity<List<DiaChi>> getByKhachHang(
            @PathVariable Integer khachHangId) {

        return ResponseEntity.ok(
                diaChiService.getByKhachHang(khachHangId)
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiaChi> getById(
            @PathVariable Integer id) {

        return ResponseEntity.ok(diaChiService.getById(id));
    }

    @PostMapping("/khach-hang/{khachHangId}")
    public ResponseEntity<DiaChi> create(
            @PathVariable Integer khachHangId,
            @RequestBody DiaChi diaChi) {

        DiaChi saved = diaChiService.create(khachHangId, diaChi);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DiaChi> update(
            @PathVariable Integer id,
            @RequestBody DiaChi diaChi) {

        return ResponseEntity.ok(
                diaChiService.update(id, diaChi)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer id) {

        diaChiService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

*/
