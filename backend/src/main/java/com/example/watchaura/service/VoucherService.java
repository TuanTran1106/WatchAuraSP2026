package com.example.watchaura.service;

import com.example.watchaura.entity.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface VoucherService {
    List<Voucher> getAll();

    Page<Voucher> getPage(Pageable pageable);

    Page<Voucher> searchPage(String keyword, Boolean trangThai, Pageable pageable);

    Voucher getById(Integer id);

    Voucher create(Voucher voucher);

    Voucher update(Integer id, Voucher voucher);

    void delete(Integer id);

    void toggleTrangThai(Integer id);
}
