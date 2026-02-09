package com.example.watchaura.service;

import com.example.watchaura.dto.SanPhamDTO;
import com.example.watchaura.dto.SanPhamRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
public interface SanPhamService {
    //Lấy tất cả sản phẩm
    List<SanPhamDTO> getAllSanPham();
    //Lấy sản phẩm theo ID
    SanPhamDTO getSanPhamById(Integer id);
    //Tạo mới sản phẩm
    SanPhamDTO createSanPham(SanPhamRequest request);
    //Cập nhật sản phẩm
    SanPhamDTO updateSanPham(Integer id, SanPhamRequest request);
    //Xóa sản phẩm
    void deleteSanPham(Integer id);
    //Cập nhật ảnh sản phẩm
    SanPhamDTO updateSanPhamImage(Integer id, String newFilePath);
    // Kiểm tra mã sản phẩm
    boolean existsByMaSanPham(String maSanPham);
    /** Sản phẩm nổi bật cho trang chủ (đang bán, mới nhất) */
    List<SanPhamDTO> getSanPhamTrangChu(int limit);

    Page<SanPhamDTO> searchPage(String keyword, Boolean trangThai, Pageable pageable);
}

