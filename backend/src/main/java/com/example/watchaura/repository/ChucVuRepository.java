package com.example.watchaura.repository;

import com.example.watchaura.entity.ChucVu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChucVuRepository extends JpaRepository<ChucVu, Integer> {
    boolean existsByTenChucVu(String tenChucVu);
}

