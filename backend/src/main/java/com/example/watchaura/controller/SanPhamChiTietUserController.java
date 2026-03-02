package com.example.watchaura.controller;

import com.example.watchaura.entity.SanPhamChiTiet;
import com.example.watchaura.repository.SanPhamChiTietRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/san_phamct")
@AllArgsConstructor
public class SanPhamChiTietUserController {

    private final SanPhamChiTietRepository sanPhamChiTietRepository;

    @GetMapping("/chi-tiet/{id}")
    public String hienThi(@PathVariable("id") Integer id, Model model) {
        SanPhamChiTiet chiTiet = sanPhamChiTietRepository.findById(id).orElse(null);

        if (chiTiet != null) {
            try {
                // Force load của các lazy relationships để tránh LazyInitializationException
                chiTiet.getSanPham().getId();
                if (chiTiet.getMauSac() != null) {
                    chiTiet.getMauSac().getId();
                }
                if (chiTiet.getKichThuoc() != null) {
                    chiTiet.getKichThuoc().getId();
                }
                if (chiTiet.getChatLieuDay() != null) {
                    chiTiet.getChatLieuDay().getId();
                }
                if (chiTiet.getLoaiMay() != null) {
                    chiTiet.getLoaiMay().getId();
                }

                // Lấy tất cả các biến thể của sản phẩm này
                List<SanPhamChiTiet> danhSachBienThe = sanPhamChiTietRepository
                        .findBySanPham_Id(chiTiet.getSanPham().getId());

                // Force load từng biến thể
                for (SanPhamChiTiet bt : danhSachBienThe) {
                    if (bt.getMauSac() != null)
                        bt.getMauSac().getId();
                    if (bt.getKichThuoc() != null)
                        bt.getKichThuoc().getId();
                    if (bt.getChatLieuDay() != null)
                        bt.getChatLieuDay().getId();
                    if (bt.getLoaiMay() != null)
                        bt.getLoaiMay().getId();
                }

                // Tìm khuyến mãi đang hoạt động cho sản phẩm này
                // List<SanPhamChiTietKhuyenMai> activeKhuyenMai = sanPhamCtKhuyenMaiRepository
                // .findActiveKhuyenMaiBySpctId(id, now);

                BigDecimal giaGoc = chiTiet.getGiaBan();
                BigDecimal giaKhuyenMai = giaGoc;
                BigDecimal soTienGiam = BigDecimal.ZERO;
                BigDecimal phanTramGiam = BigDecimal.ZERO;
                boolean coKhuyenMai = false;
                String tenKhuyenMai = "";

                // if (!activeKhuyenMai.isEmpty()) {
                // // Lấy khuyến mãi đầu tiên (có thể cải thiện logic để chọn khuyến mãi tốt
                // nhất)
                // SanPhamChiTietKhuyenMai khuyenMai = activeKhuyenMai.get(0);
                // String loaiGiam = khuyenMai.getKhuyenMai().getLoaiGiam();
                // BigDecimal giaTriGiam = khuyenMai.getKhuyenMai().getGiaTriGiam();
                // BigDecimal giamToiDa = khuyenMai.getKhuyenMai().getGiamToiDa();
                //
                // if (loaiGiam != null && loaiGiam) {
                // // Giảm theo phần trăm
                // phanTramGiam = giamToiDa;
                // soTienGiam = giaGoc.multiply(giamToiDa).divide(BigDecimal.valueOf(100));
                // giaKhuyenMai = giaGoc.subtract(soTienGiam);
                // } else {
                // // Giảm theo số tiền cố định
                // soTienGiam = giaTriGiam.min(giaGoc);
                // giaKhuyenMai = giaGoc.subtract(soTienGiam);
                // phanTramGiam = soTienGiam.multiply(BigDecimal.valueOf(100))
                // .divide(giaGoc, 2, BigDecimal.ROUND_HALF_UP);
                // }
                //
                // coKhuyenMai = true;
                // tenKhuyenMai = khuyenMai.getKhuyenMai().getTenChuongTrinh();
                // }

                // Thêm các thuộc tính vào model
                model.addAttribute("chiTiet", chiTiet);
                model.addAttribute("sanPham", chiTiet.getSanPham());
                model.addAttribute("mauSac", chiTiet.getMauSac());
                model.addAttribute("kichThuoc", chiTiet.getKichThuoc());
                model.addAttribute("chatLieuDay", chiTiet.getChatLieuDay());
                model.addAttribute("loaiMay", chiTiet.getLoaiMay());
                model.addAttribute("giaGoc", giaGoc);
                model.addAttribute("giaKhuyenMai", giaKhuyenMai);
                model.addAttribute("soTienGiam", soTienGiam);
                model.addAttribute("phanTramGiam", phanTramGiam);
                model.addAttribute("coKhuyenMai", coKhuyenMai);
                model.addAttribute("tenKhuyenMai", tenKhuyenMai);
                model.addAttribute("danhSachBienThe", danhSachBienThe);
            } catch (Exception e) {
                System.err.println("Lỗi khi load chi tiết sản phẩm: " + e.getMessage());
                e.printStackTrace();
                // Vẫn trả về template để hiển thị lỗi
            }
        } else {
            // Nếu không tìm thấy sản phẩm chi tiết
            model.addAttribute("errorMessage", "Sản phẩm chi tiết không tồn tại");
        }

        return "user/SanPhamChiTiet";
    }
}