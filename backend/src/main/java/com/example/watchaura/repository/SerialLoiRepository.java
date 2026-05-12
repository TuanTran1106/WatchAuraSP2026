package com.example.watchaura.repository;

import com.example.watchaura.entity.SerialLoi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SerialLoiRepository extends JpaRepository<SerialLoi, Integer> {

    Optional<SerialLoi> findByMaSerial(String maSerial);

    List<SerialLoi> findByTrangThai(String trangThai);

    Page<SerialLoi> findByTrangThai(String trangThai, Pageable pageable);

    @Query("SELECT s FROM SerialLoi s WHERE s.trangThai = :trangThai ORDER BY s.ngayTao DESC")
    Page<SerialLoi> findByTrangThaiOrderByNgayTaoDesc(@Param("trangThai") String trangThai, Pageable pageable);

    @Query("SELECT s FROM SerialLoi s WHERE " +
           "(:trangThai IS NULL OR :trangThai = '' OR s.trangThai = :trangThai) AND " +
           "(:keyword IS NULL OR :keyword = '' OR LOWER(s.maSerial) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.sanPhamTen) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<SerialLoi> searchSerialLoi(
            @Param("trangThai") String trangThai,
            @Param("keyword") String keyword,
            Pageable pageable);

    List<SerialLoi> findByHoanTraId(Integer hoanTraId);

    long countByTrangThai(String trangThai);

    boolean existsByMaSerial(String maSerial);
}
