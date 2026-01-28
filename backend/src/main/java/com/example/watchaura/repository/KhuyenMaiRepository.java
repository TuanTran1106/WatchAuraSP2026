package com.example.watchaura.repository;

import com.example.watchaura.entity.KhuyenMai;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KhuyenMaiRepository extends JpaRepository<KhuyenMai, Integer> {

    Optional<KhuyenMai> findByMaKhuyenMai(String maKhuyenMai);

    boolean existsByMaKhuyenMai(String maKhuyenMai);
}
