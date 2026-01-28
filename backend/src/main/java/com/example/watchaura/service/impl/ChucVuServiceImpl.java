/*
package com.example.watchaura.service.impl;

import com.example.watchaura.entity.ChucVu;
import com.example.watchaura.repository.ChucVuRepository;
import com.example.watchaura.service.ChucVuService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class ChucVuServiceImpl implements ChucVuService {
    @Autowired
    private ChucVuRepository chucVuRepository;

    @Override
    public List<ChucVu> getAll() {
        return chucVuRepository.findAll();
    }

    @Override
    public ChucVu getById(Integer id) {
        return chucVuRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy chức vụ"));
    }

    @Override
    public ChucVu create(ChucVu chucVu) {

        if (chucVuRepository.CheckByTenChucVu(chucVu.getTenChucVu())) {
            throw new RuntimeException("Tên chức vụ đã tồn tại");
        }

        return chucVuRepository.save(chucVu);
    }

    @Override
    public ChucVu update(Integer id, ChucVu chucVu) {

        ChucVu existing = getById(id);

        existing.setTenChucVu(chucVu.getTenChucVu());

        return chucVuRepository.save(existing);
    }

    @Override
    public void delete(Integer id) {
        getById(id); // check tồn tại
        chucVuRepository.deleteById(id);
    }
}
*/
