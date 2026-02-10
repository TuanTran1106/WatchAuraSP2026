
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
    List<KhachHang> findByChucVu_TenChucVu(String tenChucVu);

    boolean existsByEmail(String email);

    Optional<KhachHang> findByEmail(String email);

    Optional<KhachHang> findByMaNguoiDung(String maNguoiDung);

    @Query("SELECT k FROM KhachHang k LEFT JOIN FETCH k.chucVu WHERE " +
            "(:q IS NULL OR :q = '' OR LOWER(k.maNguoiDung) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(k.tenNguoiDung) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(k.email) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(k.sdt) LIKE LOWER(CONCAT('%', :q, '%'))) AND " +
            "(:trangThai IS NULL OR k.trangThai = :trangThai)")
    Page<KhachHang> searchByKeywordAndTrangThai(@Param("q") String q, @Param("trangThai") Boolean trangThai, Pageable pageable);

    /** Lấy số thứ tự lớn nhất (phần số sau prefix) trong mã người dùng, ví dụ AD1, AD2 -> 2. Prefix: AD, NV, KH. */
    @Query(value = "SELECT MAX(TRY_CAST(SUBSTRING(ma_nguoi_dung, LEN(:prefix)+1, 100) AS INT)) FROM KhachHang WHERE ma_nguoi_dung LIKE CONCAT(:prefix, '%')", nativeQuery = true)
    Integer getMaxSequenceForPrefix(@Param("prefix") String prefix);
}


