package com.example.watchaura.service;

import com.example.watchaura.entity.ThuongHieu;

import java.util.List;

public interface ThuongHieuService {
    List<ThuongHieu> getAll();

    ThuongHieu getById(Integer id);

    ThuongHieu create(ThuongHieu thuongHieu);

    ThuongHieu update(Integer id, ThuongHieu thuongHieu);

    void delete(Integer id);
}
