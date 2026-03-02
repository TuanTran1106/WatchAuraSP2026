package com.example.watchaura.repository;

import com.example.watchaura.entity.SanPhamChiTietKhuyenMai;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SanPhamChiTietKhuyenMaiRepository extends JpaRepository<SanPhamChiTietKhuyenMai, Integer> {
    @Query("SELECT spkm FROM SanPhamChiTietKhuyenMai spkm WHERE spkm.sanPhamChiTiet.id = :spctId AND spkm.khuyenMai.trangThai = true AND spkm.khuyenMai.ngayBatDau <= :now AND spkm.khuyenMai.ngayKetThuc >= :now")
    List<SanPhamChiTietKhuyenMai> findActiveKhuyenMaiBySpctId(@Param("spctId") Integer spctId, @Param("now") LocalDateTime now);
}

