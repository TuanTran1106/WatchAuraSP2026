package com.example.watchaura.repository;

import com.example.watchaura.entity.SanPhamChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SanPhamChiTietRepository extends JpaRepository<SanPhamChiTiet, Integer> {

    // Tìm theo sản phẩm
    List<SanPhamChiTiet> findBySanPhamId(Integer sanPhamId);

    // Tìm theo trạng thái
    List<SanPhamChiTiet> findByTrangThai(Boolean trangThai);

    // Tìm theo sản phẩm và trạng thái
    List<SanPhamChiTiet> findBySanPhamIdAndTrangThai(Integer sanPhamId, Boolean trangThai);

    // Lấy chi tiết kèm thông tin liên quan (tránh N+1 query)
    @Query("SELECT spct FROM SanPhamChiTiet spct " +
            "LEFT JOIN FETCH spct.sanPham " +
            "LEFT JOIN FETCH spct.mauSac " +
            "LEFT JOIN FETCH spct.kichThuoc " +
            "LEFT JOIN FETCH spct.chatLieuDay " +
            "LEFT JOIN FETCH spct.loaiMay " +
            "WHERE spct.id = :id")
    Optional<SanPhamChiTiet> findByIdWithDetails(@Param("id") Integer id);

    // Lấy tất cả kèm thông tin
    @Query("SELECT DISTINCT spct FROM SanPhamChiTiet spct " +
            "LEFT JOIN FETCH spct.sanPham " +
            "LEFT JOIN FETCH spct.mauSac " +
            "LEFT JOIN FETCH spct.kichThuoc " +
            "LEFT JOIN FETCH spct.chatLieuDay " +
            "LEFT JOIN FETCH spct.loaiMay")
    List<SanPhamChiTiet> findAllWithDetails();
}