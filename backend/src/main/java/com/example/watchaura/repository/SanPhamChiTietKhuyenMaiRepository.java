package com.example.watchaura.repository;

import com.example.watchaura.entity.SanPhamChiTietKhuyenMai;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SanPhamChiTietKhuyenMaiRepository extends JpaRepository<SanPhamChiTietKhuyenMai, Integer> {
}

