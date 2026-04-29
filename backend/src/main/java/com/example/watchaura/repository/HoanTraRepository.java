package com.example.watchaura.repository;

import com.example.watchaura.entity.HoanTra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HoanTraRepository extends JpaRepository<HoanTra, Integer> {

    Page<HoanTra> findAll(Pageable pageable);

    Page<HoanTra> findByTrangThai(String trangThai, Pageable pageable);

    @Query("SELECT h FROM HoanTra h WHERE h.trangThai = :trangThai")
    Page<HoanTra> findByTrangThaiOrderByNgayYeuCauDesc(@Param("trangThai") String trangThai, Pageable pageable);

    @Query("SELECT h FROM HoanTra h")
    Page<HoanTra> findAllOrderByNgayYeuCauDesc(Pageable pageable);

    @Query("SELECT h FROM HoanTra h WHERE " +
           "(:trangThai IS NULL OR h.trangThai = :trangThai) AND " +
           "(:keyword IS NULL OR LOWER(h.maHoanTra) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(h.hoaDon.maDonHang) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(h.khachHang.tenNguoiDung) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<HoanTra> searchHoanTra(
            @Param("trangThai") String trangThai,
            @Param("keyword") String keyword,
            Pageable pageable);

    @Query("SELECT h FROM HoanTra h LEFT JOIN FETCH h.chiTietList WHERE h.id = :id")
    Optional<HoanTra> findByIdWithChiTiet(@Param("id") Integer id);

    @Query("SELECT h FROM HoanTra h LEFT JOIN FETCH h.chiTietList WHERE h.maHoanTra = :maHoanTra")
    Optional<HoanTra> findByMaHoanTraWithChiTiet(@Param("maHoanTra") String maHoanTra);

    @Query("SELECT h FROM HoanTra h LEFT JOIN FETCH h.chiTietList WHERE h.hoaDon.id = :hoaDonId")
    Optional<HoanTra> findFirstByHoaDonIdWithChiTiet(@Param("hoaDonId") Integer hoaDonId);

    Optional<HoanTra> findByMaHoanTra(String maHoanTra);

    List<HoanTra> findByHoaDonId(Integer hoaDonId);

    List<HoanTra> findByKhachHangId(Integer khachHangId);

    List<HoanTra> findByTrangThai(String trangThai);

    @Query("SELECT h FROM HoanTra h WHERE h.trangThai = :trangThai ORDER BY h.ngayYeuCau DESC")
    List<HoanTra> findByTrangThaiOrderByNgayYeuCauDesc(@Param("trangThai") String trangThai);

    @Query("SELECT h FROM HoanTra h WHERE h.khachHang.id = :khachHangId ORDER BY h.ngayYeuCau DESC")
    List<HoanTra> findByKhachHangIdOrderByNgayYeuCauDesc(@Param("khachHangId") Integer khachHangId);

    @Query("SELECT COUNT(h) FROM HoanTra h WHERE h.trangThai = :trangThai")
    long countByTrangThai(@Param("trangThai") String trangThai);

    boolean existsByHoaDonId(Integer hoaDonId);

    Optional<HoanTra> findFirstByHoaDonId(Integer hoaDonId);
}
