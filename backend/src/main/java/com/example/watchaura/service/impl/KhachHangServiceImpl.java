
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class KhachHangServiceImpl implements KhachHangService {
    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private ChucVuRepository chucVuRepository;
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
        if (khachHangRepository.existsByMaNguoiDung(khachHang.getMaNguoiDung())) {
            throw new RuntimeException("Mã Khách Hàng Đã Tồn Tại");
        }


        String ma = khachHang.getMaNguoiDung();
        if (ma != null) {
            String upper = ma.toUpperCase();
            Integer tmpId = null;
            if (upper.startsWith("ADMIN")) {
                tmpId = 1;
            } else if (upper.startsWith("NV")) {
                tmpId = 2;
            } else if (upper.startsWith("KH")) {
                tmpId = 3;
            }

            if (tmpId != null) {
                final Integer roleId = tmpId;
                ChucVu chucVu = chucVuRepository
                        .findById(roleId)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy chức vụ với ID: " + roleId));
                khachHang.setChucVu(chucVu);
            }
        }


        khachHang.setNgayTao(LocalDateTime.now());
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

    @Override
    public void toggleTrangThai(Integer id) {
        KhachHang khachHang = khachHangRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng id = " + id));

        // đảo trạng thái
        khachHang.setTrangThai(!khachHang.getTrangThai());

        khachHangRepository.save(khachHang);
    }
    @Override
    public Page<KhachHang> searchPage(String q, Boolean trangThai, Pageable pageable) {
        // TODO: implement later
        return Page.empty(pageable);
    }

    @Override
    public Page<KhachHang> getPage(Pageable pageable) {
        // TODO: implement later
        return Page.empty(pageable);
    }
    @Override
    public String generateMaNguoiDung(ChucVu chucVu) {
        // TODO: implement later
        return null;
    }

    @Override
    public KhachHang registerKhachHang(
            String tenNguoiDung,
            String email,
            String sdt,
            String matKhau,
            LocalDate ngaySinh,
            String gioiTinh
    ) {
        // TODO: implement later
        return null;
    }

    @Override
    public boolean existsByEmail(String email) {
        return khachHangRepository.existsByEmail(email);
    }

    @Override
    public Optional<KhachHang> findByEmail(String email) {
        return khachHangRepository.findByEmail(email);
    }

    @Override
    public Optional<KhachHang> findByMaNguoiDung(String maNguoiDung) {
        return khachHangRepository.findByMaNguoiDung(maNguoiDung);
    }

}

