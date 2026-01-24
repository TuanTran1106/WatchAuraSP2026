package com.example.watchaura.service;

import com.example.watchaura.entity.DanhMuc;

import java.util.List;

public interface DanhMucService {
    List<DanhMuc> getAll();

    DanhMuc getById(Integer id);

    DanhMuc create(DanhMuc danhMuc);

    DanhMuc update(Integer id, DanhMuc danhMuc);

    void delete(Integer id);
}
