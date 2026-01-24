package com.example.watchaura.repository;

import com.example.watchaura.entity.ThuongHieu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThuongHieuRepository extends JpaRepository<ThuongHieu, Integer> {
    boolean existsByTenThuongHieu(String tenThuongHieu);
}

