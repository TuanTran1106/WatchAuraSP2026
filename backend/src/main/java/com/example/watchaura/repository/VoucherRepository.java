package com.example.watchaura.repository;

import com.example.watchaura.entity.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, Integer> {

    boolean existsByMaVoucher(String maVoucher);

    boolean existsByMaVoucherAndIdNot(String maVoucher, Integer id);

    @Query("SELECT v FROM Voucher v WHERE " +
            "(:q IS NULL OR :q = '' OR LOWER(v.maVoucher) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(v.tenVoucher) LIKE LOWER(CONCAT('%', :q, '%'))) AND " +
            "(:trangThai IS NULL OR v.trangThai = :trangThai)")
    Page<Voucher> searchByKeywordAndTrangThai(@Param("q") String q, @Param("trangThai") Boolean trangThai, Pageable pageable);
}
