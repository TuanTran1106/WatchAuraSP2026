package com.example.watchaura.service.impl;

import com.example.watchaura.entity.KhuyenMai;
import com.example.watchaura.repository.KhuyenMaiRepository;
import com.example.watchaura.repository.SanPhamChiTietKhuyenMaiRepository;
import com.example.watchaura.service.KhuyenMaiService;
import com.example.watchaura.util.KhuyenMaiPricing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
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

    @Autowired
    private SanPhamChiTietKhuyenMaiRepository sanPhamChiTietKhuyenMaiRepository;

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
        return searchPage(q, trangThai, null, null, null, pageable);
    }

    @Override
    public Page<KhuyenMai> searchPage(String q,
                                      Boolean trangThai,
                                      LocalDate fromDate,
                                      LocalDate toDate,
                                      KhuyenMai.PhamViApDung phamViApDung,
                                      Pageable pageable) {
        LocalDateTime fromDateStart = fromDate != null ? fromDate.atStartOfDay() : null;
        LocalDateTime toDateEnd = toDate != null ? toDate.atTime(23, 59, 59) : null;
        return khuyenMaiRepository.searchByFilters(
                q,
                trangThai,
                phamViApDung,
                fromDate,
                fromDateStart,
                toDate,
                toDateEnd,
                pageable);
    }

    @Override
    public KhuyenMai findById(Integer id) {
        return khuyenMaiRepository.findById(id).orElse(null);
    }

    @Override
    public KhuyenMai save(KhuyenMai khuyenMai) {
        if (khuyenMai.getTrangThai() == null) {
            khuyenMai.setTrangThai(Boolean.FALSE);
        }
        apGiaTriMacDinh(khuyenMai);
        chuanHoaTruocKhiLuu(khuyenMai);
        khuyenMai.setNgayTao(LocalDateTime.now());
        khuyenMai.setNgayCapNhat(LocalDateTime.now());
        if (khuyenMai.getDanhMucApDung() == null) {
            khuyenMai.setDanhMucApDung("");
        }
        if (khuyenMai.getPhamViApDung() == null) {
            khuyenMai.setPhamViApDung(
                    (khuyenMai.getDanhMucApDung() != null && !khuyenMai.getDanhMucApDung().isBlank())
                            ? KhuyenMai.PhamViApDung.CATEGORY
                            : KhuyenMai.PhamViApDung.ALL
            );
        }
        khuyenMai.setGiamToiDa(null);
        khuyenMai.setDonToiThieu(null);
        khuyenMai.setSoLuotDaDung(null);
        khuyenMai.setGioiHanLuotDung(null);
        return khuyenMaiRepository.save(khuyenMai);
    }

    @Override
    @Transactional
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
        existing.setDanhMucApDung(khuyenMai.getDanhMucApDung() != null ? khuyenMai.getDanhMucApDung().trim() : "");
        existing.setLoaiGiam(khuyenMai.getLoaiGiam());
        existing.setGiaTriGiam(khuyenMai.getGiaTriGiam());
        existing.setGiamToiDa(null);
        existing.setDonToiThieu(null);
        existing.setPhamViApDung(khuyenMai.getPhamViApDung());
        // Form datetime-local đôi khi gửi rỗng / không bind → null; DB cột NOT NULL → không được xóa ngày cũ.
        if (khuyenMai.getNgayBatDau() != null) {
            existing.setNgayBatDau(khuyenMai.getNgayBatDau());
        }
        if (khuyenMai.getNgayKetThuc() != null) {
            existing.setNgayKetThuc(khuyenMai.getNgayKetThuc());
        }
        existing.setTrangThai(khuyenMai.getTrangThai());
        if (existing.getTrangThai() == null) {
            existing.setTrangThai(Boolean.FALSE);
        }
        apGiaTriMacDinh(existing);
        if (existing.getDanhMucApDung() == null) {
            existing.setDanhMucApDung("");
        }
        if (existing.getPhamViApDung() == null) {
            existing.setPhamViApDung(
                    !existing.getDanhMucApDung().isBlank()
                            ? KhuyenMai.PhamViApDung.CATEGORY
                            : KhuyenMai.PhamViApDung.ALL
            );
        }
        if (existing.getPhamViApDung() != KhuyenMai.PhamViApDung.SKU) {
            sanPhamChiTietKhuyenMaiRepository.deleteByKhuyenMaiId(existing.getId());
        }
        if (existing.getPhamViApDung() == KhuyenMai.PhamViApDung.ALL) {
            existing.setDanhMucApDung("");
        }
        if (existing.getPhamViApDung() == KhuyenMai.PhamViApDung.CATEGORY && existing.getDanhMucApDung().isBlank()) {
            existing.setPhamViApDung(KhuyenMai.PhamViApDung.ALL);
        }
        apGiaTriMacDinh(existing);
        chuanHoaTruocKhiLuu(existing);
        if (existing.getPhamViApDung() == null) {
            existing.setPhamViApDung(KhuyenMai.PhamViApDung.ALL);
        }
        existing.setGiamToiDa(null);
        existing.setDonToiThieu(null);
        existing.setSoLuotDaDung(null);
        existing.setGioiHanLuotDung(null);
        existing.setNgayCapNhat(LocalDateTime.now());

        return khuyenMaiRepository.save(existing);
    }

    private static void apGiaTriMacDinh(KhuyenMai km) {
        if (km == null) {
            return;
        }
        if (km.getPhamViApDung() == null) {
            km.setPhamViApDung(KhuyenMai.PhamViApDung.ALL);
        }
        km.setGiamToiDa(null);
        km.setDonToiThieu(null);
        km.setSoLuotDaDung(null);
        km.setGioiHanLuotDung(null);
    }

    /**
     * Chuẩn hóa theo hướng refactor: khuyến mãi sản phẩm chỉ còn giảm theo %.
     */
    private static void chuanHoaTruocKhiLuu(KhuyenMai km) {
        if (km == null) {
            return;
        }
        String canon = KhuyenMaiPricing.chuanHoaMaLuu(km.getLoaiGiam());
        if (canon != null) {
            km.setLoaiGiam(canon);
        }
        String kind = KhuyenMaiPricing.chuanHoaMaLuu(km.getLoaiGiam());
        if (kind == null || !"PHAN_TRAM".equals(kind)) {
            km.setLoaiGiam("PHAN_TRAM");
        }
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        sanPhamChiTietKhuyenMaiRepository.deleteByKhuyenMaiId(id);
        khuyenMaiRepository.deleteById(id);
    }

    @Override
    public String deactivate(Integer id) {
        KhuyenMai km = khuyenMaiRepository.findById(id).orElse(null);
        if (km == null) {
            return "Không tìm thấy khuyến mãi.";
        }
        if (Boolean.FALSE.equals(km.getTrangThai())) {
            return "Khuyến mãi đã ngừng hoạt động.";
        }
        km.setTrangThai(Boolean.FALSE);
        km.setNgayCapNhat(LocalDateTime.now());
        khuyenMaiRepository.save(km);
        return null;
    }

    @Override
    public String toggleTrangThai(Integer id) {
        KhuyenMai km = khuyenMaiRepository.findById(id).orElse(null);
        if (km == null) {
            return "Không tìm thấy khuyến mãi.";
        }
        boolean wasActive = Boolean.TRUE.equals(km.getTrangThai());
        boolean turningOn = !wasActive;
        if (turningOn) {
            LocalDateTime now = LocalDateTime.now();
            if (km.getNgayKetThuc() != null && km.getNgayKetThuc().isBefore(now)) {
                return "Không thể bật khuyến mãi đã hết hạn.";
            }
        }
        km.setTrangThai(!wasActive);
        km.setNgayCapNhat(LocalDateTime.now());
        khuyenMaiRepository.save(km);
        return null;
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
