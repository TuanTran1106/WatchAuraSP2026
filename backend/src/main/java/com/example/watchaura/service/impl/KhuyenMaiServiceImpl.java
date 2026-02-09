package com.example.watchaura.service.impl;

import com.example.watchaura.entity.KhuyenMai;
import com.example.watchaura.repository.KhuyenMaiRepository;
import com.example.watchaura.service.KhuyenMaiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Service
public class KhuyenMaiServiceImpl implements KhuyenMaiService {

    @Autowired
    private KhuyenMaiRepository khuyenMaiRepository;

    @Override
    public Page<KhuyenMai> findAll(int page, int size) {
        return khuyenMaiRepository.findAll(
                PageRequest.of(page, size, Sort.by("id").descending())
        );
    }

    @Override
    public KhuyenMai findById(Integer id) {
        return khuyenMaiRepository.findById(id).orElse(null);
    }

    @Override
    public KhuyenMai save(KhuyenMai khuyenMai) {
        khuyenMai.setNgayTao(LocalDateTime.now());
        khuyenMai.setNgayCapNhat(LocalDateTime.now());
        return khuyenMaiRepository.save(khuyenMai);
    }

    @Override
    public KhuyenMai update(Integer id, KhuyenMai khuyenMai) {
        KhuyenMai existing = khuyenMaiRepository.findById(id).orElse(null);
        if (existing == null) {
            return null;
        }

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
    public void delete(Integer id) {
        khuyenMaiRepository.deleteById(id);
    }

    @Override
    public boolean existsByMaKhuyenMai(String maKhuyenMai) {
        return khuyenMaiRepository.existsByMaKhuyenMai(maKhuyenMai);
    }
}
