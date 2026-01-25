package com.example.watchaura.repository;

import com.example.watchaura.entity.DiaChiGiaoHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DiaChiGiaoHangRepository extends JpaRepository<DiaChiGiaoHang, Integer> {
    Optional<DiaChiGiaoHang> findByHoaDonId(Integer hoaDonId);
}

