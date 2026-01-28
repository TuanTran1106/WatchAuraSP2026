/*
package com.example.watchaura.service.impl;

import com.example.watchaura.entity.KhachHang;
import com.example.watchaura.repository.KhachHangRepository;
import com.example.watchaura.service.KhachHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class KhachHangServiceImpl implements KhachHangService {
    @Autowired
    private KhachHangRepository khachHangRepository;
    @Override
    public List<KhachHang> getAll() {
        return khachHangRepository.findAll();
    }
    @Override
    public KhachHang getById(Integer id) {
        return khachHangRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy Khách Hàng"));
    }

    @Override
    public KhachHang create(KhachHang khachHang) {
        if (khachHangRepository.checkMa(khachHang.getMaNguoiDung())) {
            throw new RuntimeException("Mã Khách Hàng Đã Tồn Tại");
        }
        khachHang.setTrangThai(true);
        return khachHangRepository.save(khachHang);
    }

    @Override
    public KhachHang update(Integer id, KhachHang khachHang) {
        KhachHang kh = getById(id);

        kh.setTenNguoiDung(khachHang.getTenNguoiDung());
        kh.setEmail(khachHang.getEmail());
        kh.setSdt(khachHang.getSdt());
        kh.setMatKhau(khachHang.getMatKhau());
        kh.setGioiTinh(khachHang.getGioiTinh());
        kh.setNgaySinh(khachHang.getNgaySinh());
        kh.setHinhAnh(khachHang.getHinhAnh());
        kh.setTrangThai(khachHang.getTrangThai());
        kh.setNgayTao(khachHang.getNgayTao());
        kh.setChucVu(khachHang.getChucVu());


        return khachHangRepository.save(kh);
    }

    @Override
    public void delete(Integer id) {
        khachHangRepository.deleteById(id);
    }
}
*/
