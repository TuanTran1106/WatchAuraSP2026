package com.example.watchaura.repository;

import com.example.watchaura.entity.LoaiMay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoaiMayRepository extends JpaRepository<LoaiMay, Integer> {
}

