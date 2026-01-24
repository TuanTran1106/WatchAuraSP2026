package com.example.watchaura.service;

import com.example.watchaura.entity.DiaChi;

import java.util.List;

public interface DiaChiService {
    List<DiaChi> getByKhachHang(Integer khachHangId);

    DiaChi getById(Integer id);

    DiaChi create(Integer khachHangId, DiaChi diaChi);

    DiaChi update(Integer id, DiaChi diaChi);

    void delete(Integer id);
}
