package com.example.watchaura.repository;

import com.example.watchaura.entity.SerialSanPham;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SerialSanPhamRepository extends JpaRepository<SerialSanPham, Integer> {

    Optional<SerialSanPham> findByMaSerial(String maSerial);

    List<SerialSanPham> findByHoaDonChiTietIdOrderByIdAsc(Integer hoaDonChiTietId);

    long countByHoaDonChiTietId(Integer hoaDonChiTietId);

    // With pagination (for large lists)
    List<SerialSanPham> findBySanPhamChiTietIdAndTrangThaiOrderByIdAsc(
            Integer sanPhamChiTietId,
            Integer trangThai,
            Pageable pageable
    );

    // Simple query without pagination (for DOI_HANG)
    @Query("SELECT s FROM SerialSanPham s WHERE s.sanPhamChiTiet.id = :sanPhamChiTietId AND s.trangThai = :trangThai ORDER BY s.id ASC")
    List<SerialSanPham> findBySanPhamChiTietIdAndTrangThai(
            @Param("sanPhamChiTietId") Integer sanPhamChiTietId,
            @Param("trangThai") Integer trangThai
    );

    @Modifying
    @Query("DELETE FROM SerialSanPham s WHERE s.sanPhamChiTiet.id = :sanPhamChiTietId")
    void deleteBySanPhamChiTietId(@Param("sanPhamChiTietId") Integer sanPhamChiTietId);

    @Modifying
    @Query("DELETE FROM SerialSanPham s WHERE s.sanPhamChiTiet.id = :sanPhamChiTietId AND s.hoaDonChiTiet IS NULL")
    void deleteBySanPhamChiTietIdAndNotSold(@Param("sanPhamChiTietId") Integer sanPhamChiTietId);

    long countBySanPhamChiTietIdAndTrangThai(Integer sanPhamChiTietId, Integer trangThai);
}
