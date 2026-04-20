package com.example.watchaura.service;

import com.example.watchaura.entity.DiaChi;

import java.util.List;
import java.util.Optional;

public interface DiaChiService {
    List<DiaChi> getByKhachHang(Integer khachHangId);

    Optional<DiaChi> getDiaChiMacDinhByKhachHang(Integer khachHangId);

    /** Đặt địa chỉ có id là mặc định cho khách hàng (các địa chỉ khác bỏ mặc định). */
    void setDiaChiMacDinh(Integer khachHangId, Integer diaChiId);

    DiaChi getById(Integer id);

    DiaChi create(Integer khachHangId, DiaChi diaChi);

    DiaChi update(Integer id, DiaChi diaChi);

    void delete(Integer id);

    /** Xóa địa chỉ của khách; nếu là mặc định thì gán mặc định cho địa chỉ còn lại (id nhỏ nhất). */
    void deleteForKhachHang(Integer khachHangId, Integer diaChiId);
}
