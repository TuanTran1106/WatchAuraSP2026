package com.example.watchaura.service;

import com.example.watchaura.entity.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface VoucherService {

    Page<Voucher> findAll(Pageable pageable);

    Page<Voucher> searchPage(String q, Boolean trangThai, Pageable pageable);

    Voucher findById(Integer id);

    Voucher save(Voucher voucher);

    Voucher update(Integer id, Voucher voucher);

    void delete(Integer id);

    void toggleTrangThai(Integer id);

    boolean existsByMaVoucher(String maVoucher);

    boolean existsByMaVoucherAndIdNot(String maVoucher, Integer id);
}
