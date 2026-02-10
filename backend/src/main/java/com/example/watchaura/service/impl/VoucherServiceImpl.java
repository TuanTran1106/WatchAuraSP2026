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
    public Voucher save(Voucher voucher) {
        voucher.setSoLuongDaDung(0);
        voucher.setNgayTao(LocalDateTime.now());
        voucher.setNgayCapNhat(LocalDateTime.now());
        // DB không cho phép NULL cột trang_thai: mặc định Đang hoạt động khi tạo mới
        if (voucher.getTrangThai() == null) {
            voucher.setTrangThai(Boolean.TRUE);
        }
        return voucherRepository.save(voucher);
    }

    @Override
    public Voucher update(Integer id, Voucher voucher) {
        Voucher existing = voucherRepository.findById(id).orElse(null);
        if (existing == null) {
            return null;
        }
        if (voucher.getMaVoucher() != null) {
            existing.setMaVoucher(voucher.getMaVoucher());
        }
        existing.setTenVoucher(voucher.getTenVoucher());
        existing.setMoTa(voucher.getMoTa());
        existing.setLoaiVoucher(voucher.getLoaiVoucher());
        existing.setGiaTri(voucher.getGiaTri());
        existing.setGiaTriToiDa(voucher.getGiaTriToiDa());
        existing.setDonHangToiThieu(voucher.getDonHangToiThieu());
        existing.setSoLuongTong(voucher.getSoLuongTong());
        existing.setNgayBatDau(voucher.getNgayBatDau());
        existing.setNgayKetThuc(voucher.getNgayKetThuc());
        // Chỉ cập nhật trạng thái khi form gửi giá trị; tránh ghi NULL (DB không cho phép)
        if (voucher.getTrangThai() != null) {
            existing.setTrangThai(voucher.getTrangThai());
        }
        existing.setNgayCapNhat(LocalDateTime.now());

        return voucherRepository.save(existing);
    }

    @Override
    public void delete(Integer id) {
        voucherRepository.deleteById(id);
    }

    @Override
    public void toggleTrangThai(Integer id) {
        Voucher v = voucherRepository.findById(id).orElse(null);
        if (v != null) {
            v.setTrangThai(v.getTrangThai() == null || !v.getTrangThai());
            voucherRepository.save(v);
        }
    }

    @Override
    public boolean existsByMaVoucher(String maVoucher) {
        return voucherRepository.existsByMaVoucher(maVoucher);
    }

    @Override
    public boolean existsByMaVoucherAndIdNot(String maVoucher, Integer id) {
        return voucherRepository.existsByMaVoucherAndIdNot(maVoucher, id);
    }
}
