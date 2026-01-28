package com.example.watchaura.service;

import com.example.watchaura.entity.Voucher;

import java.util.List;

public interface VoucherService {

    List<Voucher> findAll();

    Voucher findById(Integer id);

    Voucher save(Voucher voucher);

    Voucher update(Integer id, Voucher voucher);

    void delete(Integer id);

    boolean existsByMaVoucher(String maVoucher);
}
