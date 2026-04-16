package com.example.watchaura.repository;

import com.example.watchaura.entity.SerialSanPham;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SerialSanPhamRepository extends JpaRepository<SerialSanPham, Integer> {

    List<SerialSanPham> findByHoaDonChiTietIdOrderByIdAsc(Integer hoaDonChiTietId);

    long countByHoaDonChiTietId(Integer hoaDonChiTietId);

    List<SerialSanPham> findBySanPhamChiTietIdAndTrangThaiOrderByIdAsc(
            Integer sanPhamChiTietId,
            Integer trangThai,
            Pageable pageable
    );
}
