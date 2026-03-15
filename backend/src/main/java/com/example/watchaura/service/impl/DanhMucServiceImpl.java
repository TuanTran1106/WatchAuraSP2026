package com.example.watchaura.service.impl;

import com.example.watchaura.entity.DanhMuc;
import com.example.watchaura.entity.SanPham;
import com.example.watchaura.repository.DanhMucRepository;
import com.example.watchaura.repository.SanPhamRepository;
import com.example.watchaura.service.DanhMucService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DanhMucServiceImpl implements DanhMucService {
    @Autowired
    private DanhMucRepository danhMucRepository;
    @Autowired
    private SanPhamRepository sanPhamRepository;

    @Override
    public List<DanhMuc> getAll() {
        return danhMucRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
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
        if (danhMuc.getHinhAnh() != null) {
            existing.setHinhAnh(danhMuc.getHinhAnh());
        }

        return danhMucRepository.save(existing);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        DanhMuc danhMuc = getById(id); // check tồn tại
        // Gỡ danh mục khỏi tất cả sản phẩm đang dùng (tránh lỗi foreign key)
        List<SanPham> sanPhams = sanPhamRepository.findByDanhMucId(id);
        for (SanPham sp : sanPhams) {
            sp.setDanhMuc(null);
        }
        sanPhamRepository.saveAll(sanPhams);
        danhMucRepository.delete(danhMuc);
    }
}
