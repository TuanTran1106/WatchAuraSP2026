package com.example.watchaura.repository;

import com.example.watchaura.entity.GioHangChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GioHangChiTietRepository extends JpaRepository<GioHangChiTiet, Integer> {
    List<GioHangChiTiet> findByGioHangId(Integer gioHangId);
    Optional<GioHangChiTiet> findByGioHangIdAndSanPhamChiTietId(Integer gioHangId, Integer sanPhamChiTietId);
    void deleteByGioHangId(Integer gioHangId);
}

