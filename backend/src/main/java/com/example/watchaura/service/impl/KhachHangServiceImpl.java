package com.example.watchaura.service.impl;

import com.example.watchaura.entity.ChucVu;
import com.example.watchaura.entity.KhachHang;
import com.example.watchaura.repository.ChucVuRepository;
import com.example.watchaura.repository.KhachHangRepository;
import com.example.watchaura.service.KhachHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class KhachHangServiceImpl implements KhachHangService {
    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private ChucVuRepository chucVuRepository;
    @Override
    public String generateMaNguoiDung(ChucVu chucVu) {
        if (chucVu == null || chucVu.getTenChucVu() == null) {
            throw new RuntimeException("Chưa chọn chức vụ");
        }
        String tenChucVu = chucVu.getTenChucVu().trim();
        String prefix;
        if (tenChucVu.contains("Nhân viên") || "Nhân viên".equalsIgnoreCase(tenChucVu)) {
            prefix = "NV";
        } else if (tenChucVu.contains("Khách hàng") || "Khách hàng".equalsIgnoreCase(tenChucVu)) {
            prefix = "KH";
        } else {
            prefix = "ND";
        }
        Optional<KhachHang> last = khachHangRepository.findFirstByMaNguoiDungStartingWithOrderByIdDesc(prefix);
        int nextNum = 1;
        if (last.isPresent() && last.get().getMaNguoiDung() != null) {
            String ma = last.get().getMaNguoiDung();
            String numPart = ma.substring(prefix.length());
            if (Pattern.matches("\\d+", numPart)) {
                nextNum = Integer.parseInt(numPart) + 1;
            }
        }
        return prefix + String.format("%03d", nextNum);
    }

    @Override
    public List<KhachHang> getAll() {
        return khachHangRepository.findAll();
    }

    @Override
    public Page<KhachHang> getPage(Pageable pageable) {
        return khachHangRepository.findAll(pageable);
    }

    @Override
    public KhachHang getById(Integer id) {
        return khachHangRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy Khách Hàng"));
    }

    @Override
    public KhachHang create(KhachHang khachHang) {
        if (khachHang.getChucVu() == null || khachHang.getChucVu().getId() == null) {
            throw new RuntimeException("Chưa chọn chức vụ");
        }

        ChucVu chucVu = chucVuRepository.findById(
                khachHang.getChucVu().getId()
        ).orElseThrow(() -> new RuntimeException("Chức vụ không tồn tại"));

        khachHang.setChucVu(chucVu);
        khachHang.setMaNguoiDung(generateMaNguoiDung(chucVu));
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
        if (khachHang.getTrangThai() != null) {
            kh.setTrangThai(khachHang.getTrangThai());
        }
        /* Không cho sửa chức vụ - giữ nguyên chức vụ hiện tại */

        return khachHangRepository.save(kh);
    }

    @Override
    public void delete(Integer id) {
        khachHangRepository.deleteById(id);
    }
    @Override
    public List<KhachHang> getByTenChucVu(String tenChucVu) {
        return khachHangRepository.findByChucVu_TenChucVu(tenChucVu);
    }

}
