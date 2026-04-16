package com.example.watchaura.repository;

import com.example.watchaura.entity.HoaDonChiTiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface HoaDonChiTietRepository extends JpaRepository<HoaDonChiTiet, Integer> {
    List<HoaDonChiTiet> findByHoaDonId(Integer hoaDonId);

    @Query("SELECT DISTINCT hdct FROM HoaDonChiTiet hdct " +
           "LEFT JOIN FETCH hdct.sanPhamChiTiet spct " +
           "LEFT JOIN FETCH spct.sanPham sp " +
           "LEFT JOIN FETCH sp.loaiMay " +
           "LEFT JOIN FETCH spct.mauSac " +
           "LEFT JOIN FETCH spct.kichThuoc " +
           "LEFT JOIN FETCH spct.chatLieuDay " +
           "WHERE hdct.hoaDon.id = :hoaDonId")
    List<HoaDonChiTiet> findByHoaDonIdWithDetails(@Param("hoaDonId") Integer hoaDonId);

    /**
     * Tổng số lượng đang "giữ chỗ" ở các đơn chưa trừ kho (vd: CHO_XAC_NHAN, CHO_THANH_TOAN)
     * để chặn oversell khi user mở nhiều tab đặt cùng lúc.
     */
    @Query("""
            SELECT COALESCE(SUM(ct.soLuong), 0)
            FROM HoaDonChiTiet ct
            JOIN ct.hoaDon h
            WHERE ct.sanPhamChiTiet.id = :sanPhamChiTietId
              AND h.trangThai = true
              AND h.trangThaiDonHang IN :statuses
            """)
    Integer sumReservedQtyBySanPhamChiTietId(
            @Param("sanPhamChiTietId") Integer sanPhamChiTietId,
            @Param("statuses") Collection<String> statuses
    );

    void deleteByHoaDonId(Integer hoaDonId);
}

