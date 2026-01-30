package com.example.watchaura.controller;

import com.example.watchaura.entity.Voucher;
import com.example.watchaura.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/voucher")
public class VoucherController {

    @Autowired
    private VoucherService voucherService;

    // GET ALL
    @GetMapping
    public List<Voucher> getAll() {
        return voucherService.getAll();
    }

    // GET BY ID
    @GetMapping("/{id}")
    public Voucher getById(@PathVariable Integer id) {
        return voucherService.getById(id);
    }

    // CREATE
    @PostMapping
    public Voucher create(@RequestBody Voucher voucher) {
        return voucherService.create(voucher);
    }

    // UPDATE
    @PutMapping("/{id}")
    public Voucher update(
            @PathVariable Integer id,
            @RequestBody Voucher voucher) {
        return voucherService.update(id, voucher);
    }

    // DELETE
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        voucherService.delete(id);
    }
}
