
package com.example.watchaura.repository;

import com.example.watchaura.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {
    boolean existsByMaNguoiDung(String maNguoiDung);

    List<KhachHang> findByChucVu_TenChucVu(String tenChucVu);

    Optional<KhachHang> findFirstByMaNguoiDungStartingWithOrderByIdDesc(String prefix);
}


