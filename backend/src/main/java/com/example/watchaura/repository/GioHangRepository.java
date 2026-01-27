package com.example.watchaura.repository;

import com.example.watchaura.entity.GioHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GioHangRepository extends JpaRepository<GioHang, Integer> {
    Optional<GioHang> findByKhachHangIdAndTrangThai(Integer khachHangId, Boolean trangThai);
    List<GioHang> findByKhachHangId(Integer khachHangId);
}

