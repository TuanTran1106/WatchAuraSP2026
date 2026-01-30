package com.example.watchaura.controller;

import com.example.watchaura.entity.KhachHang;
import com.example.watchaura.service.KhachHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/khach-hang")
public class KhachHangController {
    @Autowired
    private KhachHangService khachHangService;

    @GetMapping
    public List<KhachHang> getAll() {
        return khachHangService.getAll();
    }

    @GetMapping("/{id}")
    public KhachHang getById(@PathVariable Integer id) {
        return khachHangService.getById(id);
    }

    @PostMapping
    public KhachHang create(@RequestBody KhachHang khachHang) {
        return khachHangService.create(khachHang);
    }

    @PutMapping("/{id}")
    public KhachHang update(
            @PathVariable Integer id,
            @RequestBody KhachHang khachHang) {
        return khachHangService.update(id, khachHang);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        khachHangService.delete(id);
    }

    @GetMapping("/by-chuc-vu")
    public List<KhachHang> getByChucVu(@RequestParam String ten) {
        return khachHangService.getByTenChucVu(ten);
    }

}

