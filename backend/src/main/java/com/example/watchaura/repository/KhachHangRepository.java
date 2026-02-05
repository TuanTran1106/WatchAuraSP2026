package com.example.watchaura.repository;

import com.example.watchaura.entity.KhachHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {
    boolean existsByMaNguoiDung(String maNguoiDung);

    boolean existsByEmail(String email);

    Optional<KhachHang> findByEmailIgnoreCase(String email);

    Optional<KhachHang> findByMaNguoiDung(String maNguoiDung);

    List<KhachHang> findByChucVu_TenChucVu(String tenChucVu);

    Optional<KhachHang> findFirstByMaNguoiDungStartingWithOrderByIdDesc(String prefix);

    @Query("SELECT k FROM KhachHang k WHERE " +
            "(:q IS NULL OR :q = '' OR LOWER(k.maNguoiDung) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(k.tenNguoiDung) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(k.email) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(k.sdt) LIKE LOWER(CONCAT('%', :q, '%'))) AND " +
            "(:trangThai IS NULL OR k.trangThai = :trangThai)")
    Page<KhachHang> searchByKeywordAndTrangThai(@Param("q") String q, @Param("trangThai") Boolean trangThai, Pageable pageable);
}


