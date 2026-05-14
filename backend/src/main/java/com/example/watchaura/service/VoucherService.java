package com.example.watchaura.service;

import com.example.watchaura.entity.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface VoucherService {

    Page<Voucher> findAll(Pageable pageable);

    Page<Voucher> searchPage(String q, Boolean trangThai, Pageable pageable);

    Voucher findById(Integer id);

    Voucher findByMaVoucher(String maVoucher);

    Voucher save(Voucher voucher);

    Voucher update(Integer id, Voucher voucher);

    void delete(Integer id);

    /** Đặt trạng thái không hoạt động (giữ bản ghi, giống khuyến mãi «ngừng hoạt động»). */
    void deactivate(Integer id);

    /** @return null nếu thành công; ngược lại thông báo lỗi hiển thị cho admin */
    String toggleTrangThai(Integer id);

    boolean existsByMaVoucher(String maVoucher);

    boolean existsByMaVoucherAndIdNot(String maVoucher, Integer id);

    List<Voucher> findAllValidVouchers();
}
