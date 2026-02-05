package com.example.watchaura.repository;

import com.example.watchaura.entity.HoaDon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {
    List<HoaDon> findByKhachHangId(Integer khachHangId);
    List<HoaDon> findByTrangThaiDonHang(String trangThaiDonHang);
    Optional<HoaDon> findByMaDonHang(String maDonHang);
    List<HoaDon> findByKhachHangIdOrderByNgayDatDesc(Integer khachHangId);

    List<HoaDon> findByMaDonHangContainingIgnoreCaseOrTenKhachHangContainingIgnoreCase(String maDonHang, String tenKhachHang);

    Page<HoaDon> findByTrangThaiDonHang(String trangThaiDonHang, Pageable pageable);
    Page<HoaDon> findByMaDonHangContainingIgnoreCaseOrTenKhachHangContainingIgnoreCase(String maDonHang, String tenKhachHang, Pageable pageable);

    @Query("SELECT h FROM HoaDon h WHERE h.trangThaiDonHang = :trangThai AND (LOWER(h.maDonHang) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(h.tenKhachHang) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<HoaDon> findByTrangThaiAndKeyword(@Param("trangThai") String trangThai, @Param("q") String q, Pageable pageable);
}

