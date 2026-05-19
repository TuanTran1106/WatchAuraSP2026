package com.example.watchaura.service.impl;

import com.example.watchaura.entity.Voucher;
import com.example.watchaura.repository.VoucherRepository;
import com.example.watchaura.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VoucherServiceImpl implements VoucherService {

    @Autowired
    private VoucherRepository voucherRepository;

    @Override
    public Page<Voucher> findAll(Pageable pageable) {
        return voucherRepository.findAll(pageable);
    }

    @Override
    public Page<Voucher> searchPage(String q, Boolean trangThai, Pageable pageable) {
        return voucherRepository.searchByKeywordAndTrangThai(q, trangThai, pageable);
    }

    @Override
    public Voucher findById(Integer id) {
        return voucherRepository.findById(id).orElse(null);
    }

    @Override
    public Voucher findByMaVoucher(String maVoucher) {
        if (maVoucher == null || maVoucher.isBlank()) {
            return null;
        }
        return voucherRepository.findByMaVoucherIgnoreCase(maVoucher.trim()).orElse(null);
    }

    @Override
    public Voucher save(Voucher voucher) {
        normalizeAndApplyVoucherRules(voucher);
        if (voucher.getSoLuongTong() == null || voucher.getSoLuongTong() < 1) {
            voucher.setSoLuongTong(999_999_999);
        }
        voucher.setSoLuongDaDung(0);
        voucher.setNgayTao(LocalDateTime.now());
        voucher.setNgayCapNhat(LocalDateTime.now());
        if (voucher.getTrangThai() == null) {
            voucher.setTrangThai(Boolean.TRUE);
        }
        if (voucher.getDanhMucApDung() == null) {
            voucher.setDanhMucApDung("");
        }
        voucher.setGioiHanMoiUser(Boolean.TRUE.equals(voucher.getGioiHanMoiUser()));
        return voucherRepository.save(voucher);
    }

    @Override
    public Voucher update(Integer id, Voucher voucher) {
        Voucher existing = voucherRepository.findById(id).orElse(null);
        if (existing == null) {
            return null;
        }
        normalizeAndApplyVoucherRules(voucher);
        if (voucher.getMaVoucher() != null) {
            existing.setMaVoucher(voucher.getMaVoucher());
        }
        existing.setTenVoucher(voucher.getTenVoucher());
        existing.setMoTa(voucher.getMoTa());
        existing.setLoaiVoucher(voucher.getLoaiVoucher());
        existing.setGiaTri(voucher.getGiaTri());
        if ("TIEN".equals(voucher.getLoaiVoucher())) {
            existing.setGiaTriToiDa(null);
        } else {
            existing.setGiaTriToiDa(voucher.getGiaTriToiDa());
        }
        existing.setDonHangToiThieu(voucher.getDonHangToiThieu());
        if (voucher.getSoLuongTong() != null && voucher.getSoLuongTong() >= 1) {
            existing.setSoLuongTong(voucher.getSoLuongTong());
        } else if (existing.getSoLuongTong() == null || existing.getSoLuongTong() < 1) {
            existing.setSoLuongTong(999_999_999);
        }
        existing.setNgayBatDau(voucher.getNgayBatDau());
        existing.setNgayKetThuc(voucher.getNgayKetThuc());
        existing.setDanhMucApDung(voucher.getDanhMucApDung());
        existing.setGioiHanMoiUser(Boolean.TRUE.equals(voucher.getGioiHanMoiUser()));
        if (voucher.getTrangThai() != null) {
            existing.setTrangThai(voucher.getTrangThai());
        }
        existing.setNgayCapNhat(LocalDateTime.now());

        return voucherRepository.save(existing);
    }

    private static void normalizeAndApplyVoucherRules(Voucher voucher) {
        if (voucher.getLoaiVoucher() != null) {
            String t = voucher.getLoaiVoucher().trim().toUpperCase();
            if ("PERCENT".equals(t)) {
                t = "PHAN_TRAM";
            }
            voucher.setLoaiVoucher(t);
        }
        if ("TIEN".equals(voucher.getLoaiVoucher())) {
            voucher.setGiaTriToiDa(null);
        }
        if (voucher.getMoTa() != null) {
            voucher.setMoTa(voucher.getMoTa().trim());
        }
        if (voucher.getDanhMucApDung() != null) {
            voucher.setDanhMucApDung(voucher.getDanhMucApDung().trim());
        }
    }

    @Override
    public void delete(Integer id) {
        voucherRepository.deleteById(id);
    }

    @Override
    public String deactivate(Integer id) {
        Voucher v = voucherRepository.findById(id).orElse(null);
        if (v == null) {
            return "Không tìm thấy voucher.";
        }
        if (Boolean.FALSE.equals(v.getTrangThai())) {
            return "Voucher đã ngừng hoạt động.";
        }
        v.setTrangThai(false);
        v.setNgayCapNhat(LocalDateTime.now());
        voucherRepository.save(v);
        return null;
    }

    @Override
    public String toggleTrangThai(Integer id) {
        Voucher v = voucherRepository.findById(id).orElse(null);
        if (v == null) {
            return "Không tìm thấy voucher.";
        }
        boolean wasActive = Boolean.TRUE.equals(v.getTrangThai());
        boolean turningOn = !wasActive;
        if (turningOn) {
            LocalDateTime now = LocalDateTime.now();
            if (v.getNgayKetThuc() != null && v.getNgayKetThuc().isBefore(now)) {
                return "Không thể bật voucher đã hết hạn.";
            }
            Integer tong = v.getSoLuongTong();
            int daDung = v.getSoLuongDaDung() != null ? v.getSoLuongDaDung() : 0;
            if (tong != null && tong <= daDung) {
                return "Không thể bật voucher đã hết lượt.";
            }
        }
        v.setTrangThai(!wasActive);
        voucherRepository.save(v);
        return null;
    }

    @Override
    public boolean existsByMaVoucher(String maVoucher) {
        if (maVoucher == null || maVoucher.isBlank()) {
            return false;
        }
        return voucherRepository.existsByMaVoucherIgnoreCase(maVoucher.trim());
    }

    @Override
    public boolean existsByMaVoucherAndIdNot(String maVoucher, Integer id) {
        if (maVoucher == null || maVoucher.isBlank()) {
            return false;
        }
        return voucherRepository.existsByMaVoucherIgnoreCaseAndIdNot(maVoucher.trim(), id);
    }

    @Override
    public List<Voucher> findAllValidVouchers() {
        return voucherRepository.findAllValidVouchers();
    }
}
