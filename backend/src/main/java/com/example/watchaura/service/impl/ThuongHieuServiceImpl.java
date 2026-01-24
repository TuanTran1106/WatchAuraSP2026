package com.example.watchaura.service.impl;

import com.example.watchaura.entity.ThuongHieu;
import com.example.watchaura.repository.ThuongHieuRepository;
import com.example.watchaura.service.ThuongHieuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class ThuongHieuServiceImpl implements ThuongHieuService {
    @Autowired
    private ThuongHieuRepository thuongHieuRepository;

    @Override
    public List<ThuongHieu> getAll() {
        return thuongHieuRepository.findAll();
    }

    @Override
    public ThuongHieu getById(Integer id) {
        return thuongHieuRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy thương hiệu"));
    }

    @Override
    public ThuongHieu create(ThuongHieu thuongHieu) {

        if (thuongHieuRepository.existsByTenThuongHieu(
                thuongHieu.getTenThuongHieu())) {

            throw new RuntimeException("Tên thương hiệu đã tồn tại");
        }

        return thuongHieuRepository.save(thuongHieu);
    }

    @Override
    public ThuongHieu update(Integer id, ThuongHieu thuongHieu) {

        ThuongHieu existing = getById(id);

        existing.setTenThuongHieu(
                thuongHieu.getTenThuongHieu()
        );

        return thuongHieuRepository.save(existing);
    }

    @Override
    public void delete(Integer id) {
        getById(id);
        thuongHieuRepository.deleteById(id);
    }
}
