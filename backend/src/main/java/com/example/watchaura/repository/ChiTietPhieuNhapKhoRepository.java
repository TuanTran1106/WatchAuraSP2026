package com.example.watchaura.repository;

import com.example.watchaura.entity.ChiTietPhieuNhapKho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChiTietPhieuNhapKhoRepository extends JpaRepository<ChiTietPhieuNhapKho, Integer> {

    List<ChiTietPhieuNhapKho> findByPhieuNhapKhoId(Integer phieuNhapKhoId);

    @Query("SELECT ct FROM ChiTietPhieuNhapKho ct " +
           "LEFT JOIN FETCH ct.sanPhamChiTiet spct " +
           "LEFT JOIN FETCH spct.sanPham " +
           "WHERE ct.phieuNhapKho.id = :phieuNhapKhoId")
    List<ChiTietPhieuNhapKho> findByPhieuNhapKhoIdWithDetails(@Param("phieuNhapKhoId") Integer phieuNhapKhoId);

    void deleteByPhieuNhapKhoId(Integer phieuNhapKhoId);
}
