
package com.example.watchaura.service.impl;

import com.example.watchaura.entity.ChucVu;
import com.example.watchaura.entity.KhachHang;
import com.example.watchaura.repository.ChucVuRepository;
import com.example.watchaura.repository.KhachHangRepository;
import com.example.watchaura.service.KhachHangService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    @Autowired
    private PasswordEncoder passwordEncoder;
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
        // Gán ChucVu từ form (chucVu.id được bind, cần load entity)
        Integer chucVuId = khachHang.getChucVu() != null ? khachHang.getChucVu().getId() : null;
        if (chucVuId != null) {
            ChucVu chucVu = chucVuRepository.findById(chucVuId)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy chức vụ với ID: " + chucVuId));
            khachHang.setChucVu(chucVu);
        }

        // Tự sinh mã người dùng theo role nếu chưa có
        String ma = khachHang.getMaNguoiDung();
        if (ma == null || ma.isBlank()) {
            ma = generateMaNguoiDung(khachHang.getChucVu());
            if (ma == null) {
                throw new RuntimeException("Vui lòng chọn chức vụ để tự sinh mã người dùng.");
            }
            khachHang.setMaNguoiDung(ma);
        }

        if (khachHangRepository.existsByMaNguoiDung(khachHang.getMaNguoiDung())) {
            throw new RuntimeException("Mã Khách Hàng Đã Tồn Tại");
        }

        // Mã hóa mật khẩu bằng BCrypt trước khi lưu (mọi role)
        if (khachHang.getMatKhau() != null && !khachHang.getMatKhau().isBlank()) {
            khachHang.setMatKhau(passwordEncoder.encode(khachHang.getMatKhau()));
        }

        khachHang.setNgayTao(LocalDateTime.now());
        if (khachHang.getTrangThai() == null) {
            khachHang.setTrangThai(true);
        }
        return khachHangRepository.save(khachHang);
    }

    @Override
    public KhachHang update(Integer id, KhachHang khachHang) {
        KhachHang kh = getById(id);

        kh.setTenNguoiDung(khachHang.getTenNguoiDung());
        kh.setEmail(khachHang.getEmail());
        kh.setSdt(khachHang.getSdt());
        // Chỉ đổi mật khẩu khi người dùng nhập mới; mã hóa BCrypt rồi mới lưu
        if (khachHang.getMatKhau() != null && !khachHang.getMatKhau().isBlank()) {
            kh.setMatKhau(passwordEncoder.encode(khachHang.getMatKhau()));
        }
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
        return khachHangRepository.searchByKeywordAndTrangThai(q, trangThai, pageable);
    }

    @Override
    public Page<KhachHang> getPage(Pageable pageable) {
        // TODO: implement later
        return Page.empty(pageable);
    }
    /** Sinh mã theo chức vụ: Admin (id 1) -> AD + số, Nhân viên (id 2) -> NV + số, Khách hàng (id 3) -> KH + số. */
    @Override
    public String generateMaNguoiDung(ChucVu chucVu) {
        if (chucVu == null || chucVu.getId() == null) {
            return null;
        }
        String prefix;
        switch (chucVu.getId()) {
            case 1: prefix = "AD"; break;
            case 2: prefix = "NV"; break;
            case 3: prefix = "KH"; break;
            default: prefix = "KH"; break;
        }
        Integer maxSeq = khachHangRepository.getMaxSequenceForPrefix(prefix);
        int next = (maxSeq == null ? 0 : maxSeq) + 1;
        return prefix + next;
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
        if (khachHangRepository.existsByEmail(email)) {
            throw new RuntimeException("Email này đã được sử dụng. Vui lòng dùng email khác hoặc đăng nhập.");
        }
        ChucVu chucVuKhachHang = chucVuRepository.findById(3)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chức vụ Khách hàng."));
        String maNguoiDung = generateMaNguoiDung(chucVuKhachHang);
        KhachHang kh = new KhachHang();
        kh.setMaNguoiDung(maNguoiDung);
        kh.setTenNguoiDung(tenNguoiDung);
        kh.setEmail(email);
        kh.setSdt(sdt != null ? sdt.trim() : null);
        kh.setMatKhau(passwordEncoder.encode(matKhau));
        kh.setNgaySinh(ngaySinh);
        kh.setGioiTinh(gioiTinh);
        kh.setChucVu(chucVuKhachHang);
        kh.setTrangThai(true);
        kh.setNgayTao(LocalDateTime.now());
        return khachHangRepository.save(kh);
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

