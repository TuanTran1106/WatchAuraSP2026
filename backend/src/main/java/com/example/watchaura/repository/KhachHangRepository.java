package com.example.watchaura.repository;

import com.example.watchaura.entity.KhachHang;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {
    boolean checkMa(String maNguoiDung);
}

