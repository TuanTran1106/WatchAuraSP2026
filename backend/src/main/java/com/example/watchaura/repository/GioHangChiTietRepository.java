package com.example.watchaura.repository;

import com.example.watchaura.entity.GioHangChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GioHangChiTietRepository extends JpaRepository<GioHangChiTiet, Integer> {

    @Query("SELECT DISTINCT ghct FROM GioHangChiTiet ghct " +
           "LEFT JOIN FETCH ghct.gioHang " +
           "LEFT JOIN FETCH ghct.sanPhamChiTiet spct " +
           "LEFT JOIN FETCH spct.sanPham sp " +
           "LEFT JOIN FETCH sp.danhMuc " +
           "LEFT JOIN FETCH sp.loaiMay " +
           "LEFT JOIN FETCH spct.mauSac " +
           "LEFT JOIN FETCH spct.kichThuoc " +
           "LEFT JOIN FETCH spct.chatLieuDay " +
           "WHERE ghct.id = :id")
    Optional<GioHangChiTiet> findByIdWithSanPhamDetails(@Param("id") Integer id);
    List<GioHangChiTiet> findByGioHangId(Integer gioHangId);

    @Query("SELECT DISTINCT ghct FROM GioHangChiTiet ghct " +
           "LEFT JOIN FETCH ghct.sanPhamChiTiet spct " +
           "LEFT JOIN FETCH spct.sanPham sp " +
           "LEFT JOIN FETCH sp.danhMuc " +
           "LEFT JOIN FETCH sp.loaiMay " +
           "LEFT JOIN FETCH spct.mauSac " +
           "LEFT JOIN FETCH spct.kichThuoc " +
           "LEFT JOIN FETCH spct.chatLieuDay " +
           "WHERE ghct.gioHang.id = :gioHangId")
    List<GioHangChiTiet> findByGioHangIdWithSanPhamDetails(@Param("gioHangId") Integer gioHangId);

    Optional<GioHangChiTiet> findByGioHangIdAndSanPhamChiTietId(Integer gioHangId, Integer sanPhamChiTietId);
    void deleteByGioHangId(Integer gioHangId);

    /**
     * Tính tổng số lượng của sản phẩm trong giỏ hàng của một khách hàng.
     */
    @Query("SELECT COALESCE(SUM(ghct.soLuong), 0) FROM GioHangChiTiet ghct " +
           "WHERE ghct.gioHang.khachHang.id = :khachHangId " +
           "AND ghct.sanPhamChiTiet.id = :sanPhamChiTietId " +
           "AND ghct.gioHang.trangThai = true")
    Integer getTotalSoLuongInCart(@Param("khachHangId") Integer khachHangId,
                                  @Param("sanPhamChiTietId") Integer sanPhamChiTietId);
}

