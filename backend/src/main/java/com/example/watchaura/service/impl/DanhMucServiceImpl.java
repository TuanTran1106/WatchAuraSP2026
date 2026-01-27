package com.example.watchaura.service.impl;

import com.example.watchaura.entity.DanhMuc;
import com.example.watchaura.repository.DanhMucRepository;
import com.example.watchaura.service.DanhMucService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class DanhMucServiceImpl implements DanhMucService {
    @Autowired
    private DanhMucRepository danhMucRepository;

    @Override
    public List<DanhMuc> getAll() {
        return danhMucRepository.findAll();
    }

    @Override
    public DanhMuc getById(Integer id) {
        return danhMucRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy danh mục"));
    }

    @Override
    public DanhMuc create(DanhMuc danhMuc) {

        if (danhMucRepository.existsByTenDanhMuc(danhMuc.getTenDanhMuc())) {
            throw new RuntimeException("Tên danh mục đã tồn tại");
        }

        return danhMucRepository.save(danhMuc);
    }

    @Override
    public DanhMuc update(Integer id, DanhMuc danhMuc) {

        DanhMuc existing = getById(id);

        existing.setTenDanhMuc(danhMuc.getTenDanhMuc());

        return danhMucRepository.save(existing);
    }

    @Override
    public void delete(Integer id) {
        getById(id); // check tồn tại
        danhMucRepository.deleteById(id);
    }
}
