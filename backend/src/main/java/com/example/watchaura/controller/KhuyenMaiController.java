package com.example.watchaura.controller;

import com.example.watchaura.entity.KhuyenMai;
import com.example.watchaura.service.KhuyenMaiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/khuyen-mai")
public class KhuyenMaiController {

    @Autowired
    private KhuyenMaiService khuyenMaiService;

    // GET ALL
    @GetMapping
    public List<KhuyenMai> getAll() {
        return khuyenMaiService.getAll();
    }

    // GET BY ID
    @GetMapping("/{id}")
    public KhuyenMai getById(@PathVariable Integer id) {
        return khuyenMaiService.getById(id);
    }

    // CREATE
    @PostMapping
    public KhuyenMai create(@RequestBody KhuyenMai khuyenMai) {
        return khuyenMaiService.create(khuyenMai);
    }

    // UPDATE
    @PutMapping("/{id}")
    public KhuyenMai update(
            @PathVariable Integer id,
            @RequestBody KhuyenMai khuyenMai) {
        return khuyenMaiService.update(id, khuyenMai);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        khuyenMaiService.delete(id);
    }
}
