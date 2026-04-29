package com.example.watchaura.repository;

import com.example.watchaura.entity.SanPhamChiTiet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SanPhamChiTietRepository extends JpaRepository<SanPhamChiTiet, Integer> {

    @Query("SELECT DISTINCT spct FROM SanPhamChiTiet spct " +
            "LEFT JOIN FETCH spct.sanPham sp " +
            "LEFT JOIN FETCH sp.danhMuc " +
            "LEFT JOIN FETCH sp.loaiMay " +
            "LEFT JOIN FETCH spct.mauSac " +
            "LEFT JOIN FETCH spct.kichThuoc " +
            "LEFT JOIN FETCH spct.chatLieuDay " +
            "LEFT JOIN FETCH spct.serialSanPhams " +
            "WHERE sp.id = :sanPhamId")
    List<SanPhamChiTiet> findBySanPhamId(@Param("sanPhamId") Integer sanPhamId);

    // Tìm theo trạng thái
    List<SanPhamChiTiet> findByTrangThai(Boolean trangThai);

    // Tìm theo sản phẩm và trạng thái
    List<SanPhamChiTiet> findBySanPhamIdAndTrangThai(Integer sanPhamId, Boolean trangThai);

    // Lấy chi tiết kèm thông tin liên quan (tránh N+1 query)
    @Query("SELECT spct FROM SanPhamChiTiet spct " +
            "LEFT JOIN FETCH spct.sanPham sp " +
            "LEFT JOIN FETCH sp.danhMuc " +
            "LEFT JOIN FETCH sp.loaiMay " +
            "LEFT JOIN FETCH spct.mauSac " +
            "LEFT JOIN FETCH spct.kichThuoc " +
            "LEFT JOIN FETCH spct.chatLieuDay " +
            "LEFT JOIN FETCH spct.serialSanPhams " +
            "WHERE spct.id = :id")
    Optional<SanPhamChiTiet> findByIdWithDetails(@Param("id") Integer id);

    // Lấy tất cả kèm thông tin
    @Query("SELECT DISTINCT spct FROM SanPhamChiTiet spct " +
            "LEFT JOIN FETCH spct.sanPham sp " +
            "LEFT JOIN FETCH sp.loaiMay " +
            "LEFT JOIN FETCH spct.mauSac " +
            "LEFT JOIN FETCH spct.kichThuoc " +
            "LEFT JOIN FETCH spct.chatLieuDay " +
            "LEFT JOIN FETCH spct.serialSanPhams ")
    List<SanPhamChiTiet> findAllWithDetails();

    /**
     * Biến thể đủ điều kiện bán (POS / tại quầy): sản phẩm cha và biến thể đều hoạt động, còn hàng trong kho.
     */
    @Query("SELECT DISTINCT spct FROM SanPhamChiTiet spct " +
            "LEFT JOIN FETCH spct.sanPham sp " +
            "LEFT JOIN FETCH sp.danhMuc " +
            "LEFT JOIN FETCH sp.thuongHieu " +
            "LEFT JOIN FETCH sp.loaiMay " +
            "LEFT JOIN FETCH spct.mauSac " +
            "LEFT JOIN FETCH spct.kichThuoc " +
            "LEFT JOIN FETCH spct.chatLieuDay " +
            "WHERE sp.trangThai = true AND spct.trangThai = true AND spct.soLuongTon > 0")
    List<SanPhamChiTiet> findActiveForSaleWithDetails();

    /**
     * Trừ tồn kho một cách nguyên tử. Trả về số dòng bị update (1 = thành công, 0 = không đủ hàng).
     * Tránh race condition khi nhiều đơn cùng mua 1 sản phẩm.
     */
    @Modifying
    @Query("UPDATE SanPhamChiTiet spct SET spct.soLuongTon = spct.soLuongTon - :qty WHERE spct.id = :id AND spct.soLuongTon >= :qty")
    int deductStock(@Param("id") Integer id, @Param("qty") Integer qty);

    /**
     * Hoàn tồn kho khi hủy đơn.
     */
    @Modifying
    @Query("UPDATE SanPhamChiTiet spct SET spct.soLuongTon = spct.soLuongTon + :qty WHERE spct.id = :id")
    int restoreStock(@Param("id") Integer id, @Param("qty") Integer qty);

    /**
     * Lấy sản phẩm với pessimistic write lock - khóa dòng database để tránh race condition.
     * Khi lock, các transaction khác phải chờ cho đến khi lock được giải phóng.
     * Dùng cho các thao tác thêm sản phẩm, tăng số lượng, thanh toán.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT spct FROM SanPhamChiTiet spct WHERE spct.id = :id")
    Optional<SanPhamChiTiet> findByIdWithLock(@Param("id") Integer id);

    /**
     * Kiểm tra biến thể đã tồn tại theo Màu sắc + Kích thước + Chất liệu dây
     */
    @Query("SELECT spct FROM SanPhamChiTiet spct " +
            "WHERE spct.sanPham.id = :sanPhamId " +
            "AND spct.mauSac.id = :mauSacId " +
            "AND spct.kichThuoc.id = :kichThuocId " +
            "AND spct.chatLieuDay.id = :chatLieuDayId")
    Optional<SanPhamChiTiet> findBySanPhamAndVariant(
            @Param("sanPhamId") Integer sanPhamId,
            @Param("mauSacId") Integer mauSacId,
            @Param("kichThuocId") Integer kichThuocId,
            @Param("chatLieuDayId") Integer chatLieuDayId);

    /**
     * Kiểm tra biến thể đã tồn tại (không cần điều kiện null)
     */
    @Query("SELECT CASE WHEN COUNT(spct) > 0 THEN true ELSE false END FROM SanPhamChiTiet spct " +
            "WHERE spct.sanPham.id = :sanPhamId " +
            "AND (:mauSacId IS NULL OR spct.mauSac.id = :mauSacId) " +
            "AND (:kichThuocId IS NULL OR spct.kichThuoc.id = :kichThuocId) " +
            "AND (:chatLieuDayId IS NULL OR spct.chatLieuDay.id = :chatLieuDayId)")
    boolean existsBySanPhamAndVariant(
            @Param("sanPhamId") Integer sanPhamId,
            @Param("mauSacId") Integer mauSacId,
            @Param("kichThuocId") Integer kichThuocId,
            @Param("chatLieuDayId") Integer chatLieuDayId);
}

