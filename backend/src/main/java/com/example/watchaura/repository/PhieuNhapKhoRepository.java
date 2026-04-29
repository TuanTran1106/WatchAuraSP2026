package com.example.watchaura.repository;

import com.example.watchaura.entity.PhieuNhapKho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhieuNhapKhoRepository extends JpaRepository<PhieuNhapKho, Integer> {

    Optional<PhieuNhapKho> findByMaPhieu(String maPhieu);

    Optional<PhieuNhapKho> findByHoanTraId(Integer hoanTraId);

    List<PhieuNhapKho> findByLoaiPhieu(String loaiPhieu);

    @Query("SELECT p FROM PhieuNhapKho p LEFT JOIN FETCH p.chiTietList WHERE p.id = :id")
    Optional<PhieuNhapKho> findByIdWithChiTiet(@Param("id") Integer id);

    @Query("SELECT p FROM PhieuNhapKho p LEFT JOIN FETCH p.chiTietList WHERE p.hoanTra.id = :hoanTraId")
    Optional<PhieuNhapKho> findByHoanTraIdWithChiTiet(@Param("hoanTraId") Integer hoanTraId);

    boolean existsByHoanTraId(Integer hoanTraId);
}
