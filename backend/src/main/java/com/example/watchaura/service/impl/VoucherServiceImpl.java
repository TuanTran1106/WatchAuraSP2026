package com.example.watchaura.service.impl;

import com.example.watchaura.entity.Voucher;
import com.example.watchaura.repository.VoucherRepository;
import com.example.watchaura.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class VoucherServiceImpl implements VoucherService {

    @Autowired
    private VoucherRepository voucherRepository;

    @Override
    public List<Voucher> findAll() {
        return voucherRepository.findAll();
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
        return voucherRepository.save(voucher);
    }

    @Override
    public Voucher update(Integer id, Voucher voucher) {
        Voucher existing = voucherRepository.findById(id).orElse(null);
        if (existing == null) {
            return null;
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
        existing.setTrangThai(voucher.getTrangThai());
        existing.setNgayCapNhat(LocalDateTime.now());

        return voucherRepository.save(existing);
    }

    @Override
    public void delete(Integer id) {
        voucherRepository.deleteById(id);
    }

    @Override
    public boolean existsByMaVoucher(String maVoucher) {
        return voucherRepository.existsByMaVoucher(maVoucher);
    }
}
