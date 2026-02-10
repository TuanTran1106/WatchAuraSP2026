package com.example.watchaura.service.impl;

import com.example.watchaura.entity.KhuyenMai;
import com.example.watchaura.repository.KhuyenMaiRepository;
import com.example.watchaura.service.KhuyenMaiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Service
public class KhuyenMaiServiceImpl implements KhuyenMaiService {

    @Autowired
    private KhuyenMaiRepository khuyenMaiRepository;

    @Override
    public List<KhuyenMai> getActivePromotions(LocalDateTime now) {
        return khuyenMaiRepository.findActivePromotions(now);
    }

    @Override
    public Page<KhuyenMai> findAll(int page, int size) {
        return khuyenMaiRepository.findAll(
                PageRequest.of(page, size, Sort.by("id").descending())
        );
    }

    @Override
    public Page<KhuyenMai> searchPage(String q, Boolean trangThai, Pageable pageable) {
        return khuyenMaiRepository.searchByKeywordAndTrangThai(q, trangThai, pageable);
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

        if (khuyenMai.getMaKhuyenMai() != null) {
            existing.setMaKhuyenMai(khuyenMai.getMaKhuyenMai());
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
    public void toggleTrangThai(Integer id) {
        KhuyenMai km = khuyenMaiRepository.findById(id).orElse(null);
        if (km != null) {
            km.setTrangThai(Boolean.TRUE.equals(km.getTrangThai()) ? Boolean.FALSE : Boolean.TRUE);
            km.setNgayCapNhat(LocalDateTime.now());
            khuyenMaiRepository.save(km);
        }
    }

    @Override
    public boolean existsByMaKhuyenMai(String maKhuyenMai) {
        return khuyenMaiRepository.existsByMaKhuyenMai(maKhuyenMai);
    }

    @Override
    public boolean existsByMaKhuyenMaiAndIdNot(String maKhuyenMai, Integer id) {
        return khuyenMaiRepository.existsByMaKhuyenMaiAndIdNot(maKhuyenMai, id);
    }
}
