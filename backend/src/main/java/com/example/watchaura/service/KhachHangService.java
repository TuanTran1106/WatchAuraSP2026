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

    /** true nếu email (so sánh không phân biệt hoa thường) đã thuộc user khác; excludeId null = thêm mới. */
    boolean existsEmailTakenByOther(String email, Integer excludeId);

    /** true nếu SĐT (chuẩn hóa chỉ số) đã thuộc user khác; excludeId null = thêm mới. */
    boolean existsSdtTakenByOther(String normalizedSdt, Integer excludeId);

    /** Đăng ký tài khoản khách hàng: mã đăng nhập tự sinh KH001, KH002... */
    KhachHang registerKhachHang(String tenNguoiDung, String email, String sdt, String matKhau, LocalDate ngaySinh, String gioiTinh);

    String generateMaNguoiDung(ChucVu chucVu);

    List<KhachHang> getAll();

    Page<KhachHang> getPage(Pageable pageable);

    Page<KhachHang> searchPage(String q, Boolean trangThai, Pageable pageable);

    KhachHang getById(Integer id);

    /** Lấy KhachHang đã load sẵn ChucVu, dùng cho view để tránh lazy-load khi render (giảm lỗi ERR_INCOMPLETE_CHUNKED_ENCODING). */
    Optional<KhachHang> getByIdForView(Integer id);

    KhachHang create(KhachHang khachHang);

    KhachHang update(Integer id, KhachHang khachHang);

    void delete(Integer id);

    void toggleTrangThai(Integer id);

    List<KhachHang> getByTenChucVu(String tenChucVu);

    /**
     * Đổi mật khẩu cho user đã đăng nhập.
     * @param userId ID khách hàng (từ session)
     * @param currentPassword Mật khẩu hiện tại (plain)
     * @param newPassword Mật khẩu mới (sẽ được mã hóa BCrypt)
     * @throws RuntimeException nếu mật khẩu hiện tại không đúng hoặc user không tồn tại
     */
    void changePassword(Integer userId, String currentPassword, String newPassword);

}
