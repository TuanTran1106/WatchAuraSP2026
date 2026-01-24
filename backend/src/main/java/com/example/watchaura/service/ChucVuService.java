package com.example.watchaura.service;

import com.example.watchaura.entity.ChucVu;

import java.util.List;

public interface ChucVuService {
    List<ChucVu> getAll();

    ChucVu getById(Integer id);

    ChucVu create(ChucVu chucVu);

    ChucVu update(Integer id, ChucVu chucVu);

    void delete(Integer id);
}
