package com.example.watchaura.service.impl;

import com.example.watchaura.entity.KhuyenMai;
import com.example.watchaura.repository.KhuyenMaiRepository;
import com.example.watchaura.service.KhuyenMaiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KhuyenMaiServiceImpl implements KhuyenMaiService {

    private final KhuyenMaiRepository khuyenMaiRepository;

    @Override
    public List<KhuyenMai> getAll() {
        return khuyenMaiRepository.findAll();
    }

    @Override
    public KhuyenMai getById(Integer id) {
        return khuyenMaiRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khuyến mãi ID: " + id));
    }

    @Override
    @Transactional
    public KhuyenMai create(KhuyenMai khuyenMai) {

        if (khuyenMaiRepository.existsByMaKhuyenMai(khuyenMai.getMaKhuyenMai())) {
            throw new RuntimeException("Mã khuyến mãi đã tồn tại");
        }

        khuyenMai.setNgayTao(LocalDateTime.now());
        khuyenMai.setNgayCapNhat(LocalDateTime.now());

        return khuyenMaiRepository.save(khuyenMai);
    }

    @Override
    @Transactional
    public KhuyenMai update(Integer id, KhuyenMai khuyenMai) {

        KhuyenMai existing = getById(id);

        if (khuyenMaiRepository.existsByMaKhuyenMaiAndIdNot(
                khuyenMai.getMaKhuyenMai(), id)) {
            throw new RuntimeException("Mã khuyến mãi đã tồn tại");
        }

        existing.setMaKhuyenMai(khuyenMai.getMaKhuyenMai());
        existing.setTenChuongTrinh(khuyenMai.getTenChuongTrinh());
        existing.setMoTa(khuyenMai.getMoTa());
        existing.setLoaiGiam(khuyenMai.getLoaiGiam());
        existing.setGiaTriGiam(khuyenMai.getGiaTriGiam());
        existing.setGiamToiDa(khuyenMai.getGiamToiDa());
        existing.setNgayBatDau(khuyenMai.getNgayBatDau());
        existing.setNgayKetThuc(khuyenMai.getNgayKetThuc());
        existing.setTrangThai(khuyenMai.getTrangThai());
        existing.setNgayCapNhat(LocalDateTime.now());

        return khuyenMaiRepository.save(existing);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (!khuyenMaiRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy khuyến mãi để xóa");
        }
        khuyenMaiRepository.deleteById(id);
    }
}
