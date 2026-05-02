package com.example.watchaura.repository;

import com.example.watchaura.entity.HoanTraChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HoanTraChiTietRepository extends JpaRepository<HoanTraChiTiet, Integer> {

    List<HoanTraChiTiet> findByHoanTraId(Integer hoanTraId);

    HoanTraChiTiet findByHoaDonChiTietId(Integer hoaDonChiTietId);

    @Query("SELECT h FROM HoanTraChiTiet h WHERE h.hoanTra.id = :hoanTraId")
    List<HoanTraChiTiet> findByHoanTraIdWithDetails(@Param("hoanTraId") Integer hoanTraId);

    @Query("SELECT SUM(h.soTienHoan) FROM HoanTraChiTiet h WHERE h.hoanTra.id = :hoanTraId")
    java.math.BigDecimal sumSoTienHoanByHoanTraId(@Param("hoanTraId") Integer hoanTraId);

    @Query("SELECT COUNT(h) FROM HoanTraChiTiet h WHERE h.hoanTra.id = :hoanTraId")
    long countByHoanTraId(@Param("hoanTraId") Integer hoanTraId);

    @Query("SELECT SUM(h.soLuongHoanTra) FROM HoanTraChiTiet h WHERE h.hoaDonChiTiet.id = :hdctId")
    Integer sumSoLuongHoanTraByHoaDonChiTietId(@Param("hdctId") Integer hdctId);
}
