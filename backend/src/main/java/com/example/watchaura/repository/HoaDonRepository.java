package com.example.watchaura.repository;

import com.example.watchaura.entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {
    List<HoaDon> findByKhachHangId(Integer khachHangId);
    List<HoaDon> findByTrangThaiDonHang(String trangThaiDonHang);
    Optional<HoaDon> findByMaDonHang(String maDonHang);
    List<HoaDon> findByKhachHangIdOrderByNgayDatDesc(Integer khachHangId);
}

