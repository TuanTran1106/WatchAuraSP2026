package com.example.watchaura.service;

import com.example.watchaura.entity.ChucVu;
import com.example.watchaura.entity.KhachHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface KhachHangService {

    String generateMaNguoiDung(ChucVu chucVu);

    List<KhachHang> getAll();

    Page<KhachHang> getPage(Pageable pageable);

    KhachHang getById(Integer id);

    KhachHang create(KhachHang khachHang);

    KhachHang update(Integer id, KhachHang khachHang);

    void delete(Integer id);

    List<KhachHang> getByTenChucVu(String tenChucVu);

}
