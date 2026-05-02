
package com.example.watchaura.repository;

import com.example.watchaura.entity.DiaChi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DiaChiRepository extends JpaRepository<DiaChi, Integer> {
    List<DiaChi> findByKhachHangId(Integer khachHangId);

    Optional<DiaChi> findFirstByKhachHangIdAndMacDinhTrue(Integer khachHangId);

    Optional<DiaChi> findByIdAndKhachHangIdAndIsDeletedFalse(Integer id, Integer khachHangId);
}


