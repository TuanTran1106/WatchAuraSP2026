package com.example.watchaura.repository;

import com.example.watchaura.entity.SanPhamChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SanPhamChatBotRepository extends JpaRepository<SanPhamChiTiet, Integer> {

    // Chỉ lấy sản phẩm còn hàng + đang bán
    @Query("""
        SELECT s FROM SanPhamChiTiet s
        JOIN FETCH s.sanPham sp
        JOIN FETCH sp.thuongHieu th
        JOIN FETCH sp.danhMuc dm
        JOIN FETCH sp.loaiMay lm
        LEFT JOIN FETCH s.mauSac ms
        LEFT JOIN FETCH s.kichThuoc kt
        LEFT JOIN FETCH s.chatLieuDay cl
        WHERE s.soLuongTon > 0
        AND s.trangThai = true
        AND sp.trangThai = true
    """)
    List<SanPhamChiTiet> findAllAvailableForChatbot();
}