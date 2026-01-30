package com.example.watchaura.service.impl;

import com.example.watchaura.entity.Voucher;
import com.example.watchaura.repository.VoucherRepository;
import com.example.watchaura.service.VoucherService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;

    @Override
    public List<Voucher> getAll() {
        return voucherRepository.findAll();
    }

    @Override
    public Voucher getById(Integer id) {
        return voucherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy voucher ID: " + id));
    }

    @Override
    @Transactional
    public Voucher create(Voucher voucher) {

        if (voucherRepository.existsByMaVoucher(voucher.getMaVoucher())) {
            throw new RuntimeException("Mã voucher đã tồn tại");
        }

        voucher.setSoLuongDaDung(0);
        voucher.setNgayTao(LocalDateTime.now());
        voucher.setNgayCapNhat(LocalDateTime.now());

        return voucherRepository.save(voucher);
    }

    @Override
    @Transactional
    public Voucher update(Integer id, Voucher voucher) {

        Voucher existing = getById(id);

        if (voucherRepository.existsByMaVoucherAndIdNot(
                voucher.getMaVoucher(), id)) {
            throw new RuntimeException("Mã voucher đã tồn tại");
        }

        existing.setMaVoucher(voucher.getMaVoucher());
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
    @Transactional
    public void delete(Integer id) {
        if (!voucherRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy voucher để xóa");
        }
        voucherRepository.deleteById(id);
    }
}