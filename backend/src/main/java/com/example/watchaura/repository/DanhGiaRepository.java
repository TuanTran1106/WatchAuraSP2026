package com.example.watchaura.repository;

import com.example.watchaura.entity.DanhGia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface DanhGiaRepository extends JpaRepository<DanhGia, Integer> {

    @Query("""
    SELECT dg 
    FROM DanhGia dg 
    JOIN FETCH dg.khachHang 
    WHERE dg.idSanPhamChiTiet = :id
    ORDER BY dg.ngayDanhGia DESC
""")
    List<DanhGia> findByIdSanPhamChiTietOrderByNgayDanhGiaDesc(@Param("id") Integer id);

    @Query("""
    SELECT dg
    FROM DanhGia dg
    JOIN FETCH dg.khachHang
    JOIN FETCH dg.sanPhamChiTiet spct
    JOIN FETCH spct.sanPham sp
    LEFT JOIN FETCH sp.loaiMay
    LEFT JOIN FETCH spct.mauSac
    LEFT JOIN FETCH spct.kichThuoc
    LEFT JOIN FETCH spct.chatLieuDay
    WHERE sp.id = :sanPhamId
    ORDER BY dg.ngayDanhGia DESC
    """)
    List<DanhGia> findBySanPhamIdOrderByNgayDanhGiaDesc(@Param("sanPhamId") Integer sanPhamId);

    @Query("""
    SELECT dg 
    FROM DanhGia dg 
    WHERE dg.idSanPhamChiTiet = :sanPhamChiTietId 
    AND dg.khachHang.id = :khachHangId
""")
    Optional<DanhGia> findBySanPhamChiTietIdAndKhachHangId(
            @Param("sanPhamChiTietId") Integer sanPhamChiTietId,
            @Param("khachHangId") Integer khachHangId);

    @Query("""
    SELECT CASE WHEN COUNT(dg) > 0 THEN true ELSE false END
    FROM DanhGia dg
    WHERE dg.idSanPhamChiTiet = :sanPhamChiTietId
    AND dg.khachHang.id = :khachHangId
""")
    boolean existsBySanPhamChiTietIdAndKhachHangId(
            @Param("sanPhamChiTietId") Integer sanPhamChiTietId,
            @Param("khachHangId") Integer khachHangId);

    /** Batch query: lấy tất cả sanPhamChiTietId mà user đã đánh giá */
    @Query("""
    SELECT DISTINCT dg.idSanPhamChiTiet
    FROM DanhGia dg
    WHERE dg.idSanPhamChiTiet IN :sanPhamChiTietIds
    AND dg.khachHang.id = :khachHangId
""")
    List<Integer> findReviewedProductIds(
            @Param("sanPhamChiTietIds") Set<Integer> sanPhamChiTietIds,
            @Param("khachHangId") Integer khachHangId);

}
