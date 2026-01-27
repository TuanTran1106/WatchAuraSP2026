package com.example.watchaura.repository;

import com.example.watchaura.entity.SanPham;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SanPhamRepository extends JpaRepository<SanPham, Integer> {
    /**
     * Tìm sản phẩm theo mã sản phẩm
     */
    Optional<SanPham> findByMaSanPham(String maSanPham);

    /**
     * Kiểm tra mã sản phẩm đã tồn tại chưa
     */
    boolean existsByMaSanPham(String maSanPham);

    /**
     * Tìm tất cả sản phẩm theo trạng thái
     */
    List<SanPham> findByTrangThai(Boolean trangThai);

    /**
     * Tìm sản phẩm theo trạng thái với phân trang
     */
    Page<SanPham> findByTrangThai(Boolean trangThai, Pageable pageable);

    /**
     * Tìm sản phẩm theo tên (tìm kiếm gần đúng)
     */
    List<SanPham> findByTenSanPhamContainingIgnoreCase(String tenSanPham);

    /**
     * Tìm sản phẩm theo tên với phân trang
     */
    Page<SanPham> findByTenSanPhamContainingIgnoreCase(String tenSanPham, Pageable pageable);

    /**
     * Tìm sản phẩm theo thương hiệu
     */
    List<SanPham> findByThuongHieuId(Integer thuongHieuId);

    /**
     * Tìm sản phẩm theo thương hiệu với phân trang
     */
    Page<SanPham> findByThuongHieuId(Integer thuongHieuId, Pageable pageable);

    /**
     * Tìm sản phẩm theo danh mục
     */
    List<SanPham> findByDanhMucId(Integer danhMucId);

    /**
     * Tìm sản phẩm theo danh mục với phân trang
     */
    Page<SanPham> findByDanhMucId(Integer danhMucId, Pageable pageable);

    /**
     * Tìm sản phẩm theo phong cách
     */
    List<SanPham> findByPhongCachContainingIgnoreCase(String phongCach);

    /**
     * Tìm sản phẩm theo thương hiệu và trạng thái
     */
    List<SanPham> findByThuongHieuIdAndTrangThai(Integer thuongHieuId, Boolean trangThai);

    /**
     * Tìm sản phẩm theo danh mục và trạng thái
     */
    List<SanPham> findByDanhMucIdAndTrangThai(Integer danhMucId, Boolean trangThai);

    /**
     * Đếm số sản phẩm theo thương hiệu
     */
    long countByThuongHieuId(Integer thuongHieuId);

    /**
     * Đếm số sản phẩm theo danh mục
     */
    long countByDanhMucId(Integer danhMucId);

    /**
     * Đếm số sản phẩm theo trạng thái
     */
    long countByTrangThai(Boolean trangThai);

    // ===================================
    // CUSTOM @Query - Tìm kiếm nâng cao
    // ===================================

    /**
     * Tìm kiếm sản phẩm theo nhiều tiêu chí
     */
    @Query("SELECT sp FROM SanPham sp WHERE " +
            "(:tenSanPham IS NULL OR LOWER(sp.tenSanPham) LIKE LOWER(CONCAT('%', :tenSanPham, '%'))) AND " +
            "(:thuongHieuId IS NULL OR sp.thuongHieu.id = :thuongHieuId) AND " +
            "(:danhMucId IS NULL OR sp.danhMuc.id = :danhMucId) AND " +
            "(:trangThai IS NULL OR sp.trangThai = :trangThai)")
    Page<SanPham> searchSanPham(
            @Param("tenSanPham") String tenSanPham,
            @Param("thuongHieuId") Integer thuongHieuId,
            @Param("danhMucId") Integer danhMucId,
            @Param("trangThai") Boolean trangThai,
            Pageable pageable
    );

    /**
     * Lấy danh sách sản phẩm kèm thông tin thương hiệu và danh mục (tránh N+1 query)
     */
    @Query("SELECT DISTINCT sp FROM SanPham sp " +
            "LEFT JOIN FETCH sp.thuongHieu " +
            "LEFT JOIN FETCH sp.danhMuc " +
            "WHERE sp.trangThai = :trangThai")
    List<SanPham> findAllWithDetails(@Param("trangThai") Boolean trangThai);

    /**
     * Lấy sản phẩm theo ID kèm thông tin chi tiết
     */
    @Query("SELECT sp FROM SanPham sp " +
            "LEFT JOIN FETCH sp.thuongHieu " +
            "LEFT JOIN FETCH sp.danhMuc " +
            "WHERE sp.id = :id")
    Optional<SanPham> findByIdWithDetails(@Param("id") Integer id);

    /**
     * Tìm sản phẩm mới nhất
     */
    @Query("SELECT sp FROM SanPham sp ORDER BY sp.ngayTao DESC")
    List<SanPham> findLatestProducts(Pageable pageable);

    /**
     * Tìm sản phẩm theo khoảng thời gian tạo
     */
    @Query("SELECT sp FROM SanPham sp WHERE sp.ngayTao BETWEEN :startDate AND :endDate")
    List<SanPham> findByNgayTaoBetween(
            @Param("startDate") java.time.LocalDateTime startDate,
            @Param("endDate") java.time.LocalDateTime endDate
    );

    /**
     * Cập nhật trạng thái sản phẩm
     */
    @Query("UPDATE SanPham sp SET sp.trangThai = :trangThai WHERE sp.id = :id")
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    int updateTrangThai(@Param("id") Integer id, @Param("trangThai") Boolean trangThai);

    /**
     * Cập nhật trạng thái nhiều sản phẩm theo danh sách ID
     */
    @Query("UPDATE SanPham sp SET sp.trangThai = :trangThai WHERE sp.id IN :ids")
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    int updateTrangThaiByIds(@Param("ids") List<Integer> ids, @Param("trangThai") Boolean trangThai);

    /**
     * Xóa mềm sản phẩm (set trạng thái = false)
     */
    @Query("UPDATE SanPham sp SET sp.trangThai = false WHERE sp.id = :id")
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    int softDelete(@Param("id") Integer id);

    /**
     * Thống kê số lượng sản phẩm theo thương hiệu
     */
    @Query("SELECT sp.thuongHieu.tenThuongHieu, COUNT(sp) FROM SanPham sp " +
            "GROUP BY sp.thuongHieu.tenThuongHieu")
    List<Object[]> countByThuongHieu();

    /**
     * Thống kê số lượng sản phẩm theo danh mục
     */
    @Query("SELECT sp.danhMuc.tenDanhMuc, COUNT(sp) FROM SanPham sp " +
            "GROUP BY sp.danhMuc.tenDanhMuc")
    List<Object[]> countByDanhMuc();


    /**
     * Kiểm tra mã sản phẩm đã tồn tại (ngoại trừ ID hiện tại - dùng khi update)
     */
    boolean existsByMaSanPhamAndIdNot(String maSanPham, Integer id);
}


    /**
     * Kiểm tra mã sản phẩm đã tồn tại (ngoại trừ ID hiện tại - dùng khi update)
     */
    boolean existsByMaSanPhamAndIdNot(String maSanPham, Integer id);
}