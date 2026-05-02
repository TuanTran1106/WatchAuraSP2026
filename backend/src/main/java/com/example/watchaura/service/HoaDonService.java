package com.example.watchaura.service;



import com.example.watchaura.dto.CheckoutStockResponse;

import com.example.watchaura.dto.HoaDonDTO;

import com.example.watchaura.dto.HoaDonRequest;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;



import java.time.LocalDate;

import java.util.List;

import java.math.BigDecimal;

import java.util.Map;



public interface HoaDonService {

    List<HoaDonDTO> getAll();

    List<HoaDonDTO> search(String keyword);

    Page<HoaDonDTO> searchPage(String keyword, String trangThai, Pageable pageable, String loaiDon);

    HoaDonDTO getById(Integer id);

    HoaDonDTO getByMaDonHang(String maDonHang);

    List<HoaDonDTO> getByKhachHangId(Integer khachHangId);

    List<HoaDonDTO> getByTrangThaiDonHang(String trangThaiDonHang);

    HoaDonDTO create(HoaDonRequest request);

    HoaDonDTO update(Integer id, HoaDonRequest request);

    HoaDonDTO updateTrangThaiDonHang(Integer id, String trangThaiDonHang);

    void delete(Integer id);

    String generateMaDonHang();

    byte[] exportPdf(HoaDonDTO dto);

    byte[] exportRevenueReportPdf(List<HoaDonDTO> orders, LocalDate fromDate, LocalDate toDate, String statusFilter);



    Page<HoaDonDTO> filterDonHang(

            Integer userId,

            String trangThai,

            String thanhToan,

            LocalDate ngay,

            String maDon,

            Pageable pageable

    );

    BigDecimal tinhTienGiamVoucher(Integer khachHangId, Integer voucherId, BigDecimal tongTien);



    /**

     * Cập nhật đơn hàng (chỉnh sửa số lượng sản phẩm) cho đơn hàng ở trạng thái CAN_XU_LY.

     * Nếu số lượng yêu cầu vượt tồn kho thì ném lỗi, không ghi DB. Ngược lại cập nhật chi tiết,

     * tính lại tổng tiền và chuyển trạng thái sang CHO_XAC_NHAN.

     *

     * @param hoaDonId ID hóa đơn

     * @param itemsData Map với key = sanPhamChiTietId, value = soLuongMoi

     * @return HoaDonDTO đã cập nhật

     */

    HoaDonDTO editOrderItems(Integer hoaDonId, Map<Integer, Integer> itemsData);



    /**

     * Kiểm tra và điều chỉnh số lượng sản phẩm trong giỏ hàng theo tồn kho thực tế.

     * Khi người dùng thanh toán, hệ thống sẽ kiểm tra tồn kho cho từng sản phẩm.

     * Nếu số lượng trong giỏ > tồn kho khả dụng, sẽ tự động giảm số lượng xuống mức tối đa có thể.

     *

     * @param gioHangId ID giỏ hàng cần kiểm tra

     * @param khachHangId ID khách hàng (để tính tổng số lượng trong tất cả giỏ hàng nếu cần)

     * @return CheckoutStockResponse chứa danh sách cảnh báo cho các sản phẩm bị điều chỉnh

     */

    CheckoutStockResponse checkAndAdjustStockBeforeCheckout(Integer gioHangId, Integer khachHangId);

}

