package com.example.watchaura.service;

import com.example.watchaura.entity.Voucher;

import java.util.List;

public interface VoucherService {
    List<Voucher> getAll();

    Voucher getById(Integer id);

    Voucher create(Voucher voucher);

    Voucher update(Integer id, Voucher voucher);

    void delete(Integer id);
}
