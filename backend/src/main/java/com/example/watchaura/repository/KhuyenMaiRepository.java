package com.example.watchaura.repository;

import com.example.watchaura.entity.KhuyenMai;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface KhuyenMaiRepository extends JpaRepository<KhuyenMai, Integer> {

    boolean existsByMaKhuyenMai(String maKhuyenMai);

    boolean existsByMaKhuyenMaiAndIdNot(String maKhuyenMai, Integer id);

    @Query("SELECT km FROM KhuyenMai km WHERE " +
            "(:q IS NULL OR :q = '' OR LOWER(km.maKhuyenMai) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(km.tenChuongTrinh) LIKE LOWER(CONCAT('%', :q, '%'))) AND " +
            "(:trangThai IS NULL OR km.trangThai = :trangThai)")
    Page<KhuyenMai> searchByKeywordAndTrangThai(@Param("q") String q, @Param("trangThai") Boolean trangThai, Pageable pageable);

    /** Khuyến mãi đang diễn ra (trạng thái bật, trong khoảng ngày) */
    @Query("SELECT km FROM KhuyenMai km WHERE km.trangThai = true " +
            "AND km.ngayBatDau <= :now AND km.ngayKetThuc >= :now ORDER BY km.ngayKetThuc ASC")
    List<KhuyenMai> findActivePromotions(@Param("now") LocalDateTime now);
}
