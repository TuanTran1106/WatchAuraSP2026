package com.example.watchaura.service;

import com.example.watchaura.entity.ChucVu;
import com.example.watchaura.entity.KhachHang;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface KhachHangService {

    Optional<KhachHang> findByMaNguoiDung(String maNguoiDung);

    Optional<KhachHang> findByEmail(String email);

    boolean existsByEmail(String email);

    /** Đăng ký tài khoản khách hàng: mã đăng nhập tự sinh KH001, KH002... */
    KhachHang registerKhachHang(String tenNguoiDung, String email, String sdt, String matKhau, LocalDate ngaySinh, String gioiTinh);

    String generateMaNguoiDung(ChucVu chucVu);

    List<KhachHang> getAll();

    Page<KhachHang> getPage(Pageable pageable);

    Page<KhachHang> searchPage(String q, Boolean trangThai, Pageable pageable);

    KhachHang getById(Integer id);

    KhachHang create(KhachHang khachHang);

    KhachHang update(Integer id, KhachHang khachHang);

    void delete(Integer id);

    void toggleTrangThai(Integer id);

    List<KhachHang> getByTenChucVu(String tenChucVu);

}
