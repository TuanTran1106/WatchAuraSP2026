package com.example.watchaura.repository;



import com.example.watchaura.entity.HoaDon;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Modifying;

import org.springframework.data.jpa.repository.Query;

import org.springframework.data.repository.query.Param;

import org.springframework.stereotype.Repository;



import java.time.LocalDate;

import java.time.LocalDateTime;

import java.util.Collection;

import java.util.List;

import java.util.Optional;



@Repository

public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {

    List<HoaDon> findByKhachHangId(Integer khachHangId);

    List<HoaDon> findByTrangThaiDonHang(String trangThaiDonHang);

    Optional<HoaDon> findByMaDonHang(String maDonHang);

    List<HoaDon> findByKhachHangIdOrderByNgayDatDesc(Integer khachHangId);

    List<HoaDon> findByLoaiHoaDon(String loaiHoaDon);



    List<HoaDon> findByMaDonHangContainingIgnoreCaseOrTenKhachHangContainingIgnoreCase(String maDonHang, String tenKhachHang);



    @Query("SELECT h FROM HoaDon h WHERE h.trangThaiDonHang = :trangThai AND h.trangThai = true AND h.trangThaiDonHang <> 'DRAFT_OFFLINE'")

    Page<HoaDon> findByTrangThaiDonHangAndTrangThai(@Param("trangThai") String trangThaiDonHang, Pageable pageable);



    @Query("SELECT h FROM HoaDon h WHERE (h.trangThai IS NULL OR h.trangThai = true) AND (h.trangThaiDonHang IS NULL OR h.trangThaiDonHang <> 'DRAFT_OFFLINE')")

    Page<HoaDon> findActiveOrders(Pageable pageable);



    @Query("SELECT h FROM HoaDon h WHERE h.trangThai = true AND (h.trangThaiDonHang IS NULL OR h.trangThaiDonHang <> 'DRAFT_OFFLINE') AND (LOWER(h.maDonHang) LIKE LOWER(CONCAT('%', :maDonHang, '%')) OR LOWER(h.tenKhachHang) LIKE LOWER(CONCAT('%', :tenKhachHang, '%')))")

    Page<HoaDon> findByMaDonHangContainingIgnoreCaseOrTenKhachHangContainingIgnoreCaseAndTrangThai(@Param("maDonHang") String maDonHang, @Param("tenKhachHang") String tenKhachHang, Pageable pageable);



    @Query("SELECT h FROM HoaDon h WHERE h.trangThaiDonHang = :trangThai AND h.trangThai = true AND h.trangThaiDonHang <> 'DRAFT_OFFLINE' AND (LOWER(h.maDonHang) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(h.tenKhachHang) LIKE LOWER(CONCAT('%', :q, '%')))")

    Page<HoaDon> findByTrangThaiAndKeyword(@Param("trangThai") String trangThai, @Param("q") String q, Pageable pageable);



    /** Lọc theo một trong nhiều mã trạng thái (ví dụ Đã thanh toán: DA_THANH_TOAN và DA THANH TOAN). */

    @Query("SELECT h FROM HoaDon h WHERE h.trangThaiDonHang IN :trangThaiList AND h.trangThai = true AND h.trangThaiDonHang <> 'DRAFT_OFFLINE'")

    Page<HoaDon> findByTrangThaiDonHangInAndTrangThai(@Param("trangThaiList") Collection<String> trangThaiList, Pageable pageable);



    @Query("SELECT h FROM HoaDon h WHERE h.trangThaiDonHang IN :trangThaiList AND h.trangThai = true AND h.trangThaiDonHang <> 'DRAFT_OFFLINE' AND (LOWER(h.maDonHang) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(h.tenKhachHang) LIKE LOWER(CONCAT('%', :q, '%')))")

    Page<HoaDon> findByTrangThaiInAndKeyword(@Param("trangThaiList") Collection<String> trangThaiList, @Param("q") String q, Pageable pageable);



    Page<HoaDon> findByKhachHangIdOrderByNgayDatDesc(Integer khachHangId, Pageable pageable);





    /**

     * Lọc đơn theo ngày: so khớp cả ngày (LocalDate) bằng khoảng [ngayStart, ngayEnd).

     * Tránh FUNCTION('FORMAT'...) — Hibernate + SQL Server dễ lỗi runtime.

     */

    @Query("""

SELECT h FROM HoaDon h

WHERE h.khachHang.id = :userId

AND (:trangThai IS NULL OR h.trangThaiDonHang = :trangThai)

AND (:thanhToan IS NULL OR h.phuongThucThanhToan = :thanhToan)

AND (:ngayStart IS NULL OR (h.ngayDat >= :ngayStart AND h.ngayDat < :ngayEnd))

AND (:maDon IS NULL OR LOWER(h.maDonHang) LIKE LOWER(CONCAT('%', :maDon, '%')))

ORDER BY h.ngayDat DESC

""")

    Page<HoaDon> filterDonHang(

            @Param("userId") Integer userId,

            @Param("trangThai") String trangThai,

            @Param("thanhToan") String thanhToan,

            @Param("ngayStart") LocalDateTime ngayStart,

            @Param("ngayEnd") LocalDateTime ngayEnd,

            @Param("maDon") String maDon,

            Pageable pageable

    );



    /**

     * Tính tổng số lượng đã đặt (nhưng chưa xác nhận) của một sản phẩm.

     * Bao gồm các đơn hàng ở trạng thái: CHO_XAC_NHAN, CHO_THANH_TOAN

     */

    @Query("SELECT COALESCE(SUM(hdct.soLuong), 0) FROM HoaDonChiTiet hdct " +

           "JOIN hdct.hoaDon hd " +

           "WHERE hdct.sanPhamChiTiet.id = :sanPhamChiTietId " +

           "AND hd.trangThaiDonHang IN ('CHO_XAC_NHAN', 'CHO_THANH_TOAN') " +

           "AND hd.trangThai = true")

    Integer getReservedQuantity(@Param("sanPhamChiTietId") Integer sanPhamChiTietId);



    /**

     * Tìm hóa đơn OFFLINE đang trong trạng thái chờ (chưa thanh toán)

     */

    Optional<HoaDon> findFirstByLoaiHoaDonAndTrangThaiDonHangAndTrangThai(

            String loaiHoaDon,

            String trangThaiDonHang,

            Boolean trangThai

    );



    /**

     * Giỏ bán tại quầy đang soạn (chưa chốt hóa đơn). Lấy bản ghi mới nhất theo id.

     */

    Optional<HoaDon> findFirstByLoaiHoaDonAndTrangThaiDonHangAndTrangThaiOrderByIdDesc(

            String loaiHoaDon,

            String trangThaiDonHang,

            Boolean trangThai

    );





    /**

     * Tìm tất cả đơn hàng online (khách không đăng nhập) theo email và chưa có khachHang

     */

    @Query("SELECT h FROM HoaDon h WHERE h.email = :email AND h.khachHang IS NULL AND h.loaiHoaDon = 'ONLINE'")

    List<HoaDon> findOnlineOrdersByEmailWithoutAccount(@Param("email") String email);



    /**

     * Liên kết đơn hàng với khách hàng theo email

     */

    @Modifying(clearAutomatically = true, flushAutomatically = true)

    @Query("UPDATE HoaDon h SET h.khachHang.id = :khachHangId WHERE h.email = :email AND h.khachHang IS NULL AND h.loaiHoaDon = 'ONLINE'")

    int linkOrdersToCustomerByEmail(@Param("email") String email, @Param("khachHangId") Integer khachHangId);



    List<HoaDon> findByEmailIgnoreCaseAndKhachHangIsNull(String email);





}



