package com.example.watchaura.service;

import com.example.watchaura.dto.SanPhamDTO;
import com.example.watchaura.dto.SanPhamRequest;
import com.example.watchaura.entity.SanPham;
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

    /**
     * Map danh sách entity sang DTO kèm giá/KM (dùng TrangChu /san_pham/home cùng logic với user/home).
     */
    List<SanPhamDTO> getSanPhamDtosForDisplay(List<SanPham> sanPhams);

    Page<SanPhamDTO> searchPage(String keyword, Boolean trangThai, Pageable pageable);
    // Sinh mã sản phẩm tự động
    String generateMaSanPham();
}

