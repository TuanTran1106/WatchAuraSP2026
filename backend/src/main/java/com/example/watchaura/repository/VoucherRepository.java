package com.example.watchaura.repository;

import com.example.watchaura.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Integer> {

    Optional<Voucher> findByMaVoucher(String maVoucher);

    boolean existsByMaVoucher(String maVoucher);
}
