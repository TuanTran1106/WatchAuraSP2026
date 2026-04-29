package com.example.watchaura.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SanPhamDTO {

    private Integer id;
    private String maSanPham;
    private String tenSanPham;
    private String moTa;
    private String hinhAnh;
    private Integer idThuongHieu;
    private String tenThuongHieu;
    private Integer idDanhMuc;
    private String tenDanhMuc;
    private String phongCach;
    private Boolean trangThai;
    /** Giá bán (thấp nhất trong các biến thể — giá gốc hiển thị), null nếu chưa có chi tiết */
    private BigDecimal giaBan;
    /** Giá sau khuyến mãi (cấp sản phẩm hoặc KM biến thể — giá tốt nhất trên mức giá gốc trên), null nếu không có KM hợp lệ */
    private BigDecimal giaSauKhuyenMai;
    /** Phần trăm giảm để hiển thị badge (0–100), null khi không có KM */
    private BigDecimal phanTramGiamHienThi;
    /**
     * {@code PHAN_TRAM} hoặc {@code TIEN}: loại KM đang hiển thị trên card (tránh hiển thị % khi thực tế là giảm tiền).
     */
    private String loaiGiamHienThi;
    /** Số tiền giảm mỗi đơn vị (VNĐ), dùng cho badge và tiện so khớp với chi tiết/giỏ. */
    private BigDecimal soTienGiamHienThi;
    private Boolean coKhuyenMaiHopLe;
    /**
     * Cấu hình KM gắn {@link com.example.watchaura.entity.SanPham} (admin / API; không phải giá sau giảm hiển thị).
     */
    private String loaiKhuyenMai;
    private BigDecimal giaTriKhuyenMai;
    private LocalDateTime ngayBatDauKhuyenMai;
    private LocalDateTime ngayKetThucKhuyenMai;
    private Boolean trangThaiKhuyenMai;
    /** Tổng số lượng tồn của tất cả biến thể */
    private Integer soLuongTon;

    private LocalDateTime ngayTao;

    /**
     * Có từ 2 biến thể có giá và (min niêm yết ≠ max hoặc min giá hiệu lực ≠ max) — card hiển thị khoảng giá; badge KM vẫn dùng mức giảm của biến thể “tốt nhất” (giá sau KM thấp nhất).
     */
    private Boolean coHienThiKhoangGia;
    private BigDecimal giaNiemyetThapNhat;
    private BigDecimal giaNiemyetCaoNhat;
    /** Giá thực tế từng biến thể (sau KM nếu có). */
    private BigDecimal giaHienThiThapNhat;
    private BigDecimal giaHienThiCaoNhat;

    private Integer idMauSac;
    private Integer idKichThuoc;
    private Integer idChatLieuDay;
    private Integer idLoaiMay;
    private String tenLoaiMay;

    private Double duongKinh;
    private Integer doChiuNuoc;
    private Double beRongDay;
    private Double trongLuong;
}