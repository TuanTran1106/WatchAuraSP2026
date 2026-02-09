package com.example.watchaura.service;

import com.example.watchaura.entity.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface VoucherService {

    Page<Voucher> findAll(Pageable pageable);


    Voucher findById(Integer id);

    Voucher save(Voucher voucher);

    Voucher update(Integer id, Voucher voucher);

    void delete(Integer id);

    boolean existsByMaVoucher(String maVoucher);
}
