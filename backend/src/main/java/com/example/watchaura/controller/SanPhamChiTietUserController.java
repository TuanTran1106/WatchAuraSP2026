package com.example.watchaura.controller;

import com.example.watchaura.dto.KhuyenMaiPriceResult;
import com.example.watchaura.dto.VariantPriceView;
import com.example.watchaura.entity.KhuyenMai;
import com.example.watchaura.entity.SanPhamChiTiet;
import com.example.watchaura.repository.SanPhamChiTietRepository;
import com.example.watchaura.service.KhuyenMaiService;
import com.example.watchaura.service.SanPhamChiTietKhuyenMaiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/san_phamct")
@RequiredArgsConstructor
public class SanPhamChiTietUserController {

    private final SanPhamChiTietRepository sanPhamChiTietRepository;
    private final SanPhamChiTietKhuyenMaiService sanPhamChiTietKhuyenMaiService;
    private final KhuyenMaiService khuyenMaiService;

    @GetMapping("/chi-tiet/{id}")
    public String hienThi(@PathVariable("id") Integer id, Model model) {
        SanPhamChiTiet chiTiet = sanPhamChiTietRepository.findByIdWithDetails(id).orElse(null);

        if (chiTiet != null) {
            try {
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
                if (chiTiet.getSanPham() != null && chiTiet.getSanPham().getLoaiMay() != null) {
                    chiTiet.getSanPham().getLoaiMay().getId();
                }

                List<SanPhamChiTiet> danhSachBienThe = sanPhamChiTietRepository
                        .findBySanPhamId(chiTiet.getSanPham().getId());

                for (SanPhamChiTiet bt : danhSachBienThe) {
                    if (bt.getMauSac() != null) {
                        bt.getMauSac().getId();
                    }
                    if (bt.getKichThuoc() != null) {
                        bt.getKichThuoc().getId();
                    }
                    if (bt.getChatLieuDay() != null) {
                        bt.getChatLieuDay().getId();
                    }
                    if (bt.getSanPham() != null && bt.getSanPham().getLoaiMay() != null) {
                        bt.getSanPham().getLoaiMay().getId();
                    }
                }

                LocalDateTime promoNow = LocalDateTime.now();
                List<KhuyenMai> khuyenMaiDangChay = khuyenMaiService.getActivePromotions(promoNow);
                Map<Integer, VariantPriceView> variantPriceBySpctId = new LinkedHashMap<>();
                for (SanPhamChiTiet bt : danhSachBienThe) {
                    if (bt.getId() != null) {
                        KhuyenMaiPriceResult vr = sanPhamChiTietKhuyenMaiService
                                .resolveBestForCartOrOrderLine(bt, promoNow, khuyenMaiDangChay);
                        variantPriceBySpctId.put(bt.getId(), VariantPriceView.from(vr));
                    }
                }
                model.addAttribute("variantPriceBySpctId", variantPriceBySpctId);

                KhuyenMaiPriceResult pr = sanPhamChiTietKhuyenMaiService
                        .resolveBestForCartOrOrderLine(chiTiet, promoNow, khuyenMaiDangChay);
                BigDecimal giaGoc = pr.giaGoc();
                BigDecimal giaKhuyenMai = pr.giaSauGiam();
                BigDecimal soTienGiam = pr.soTienGiam();
                BigDecimal phanTramGiam = pr.phanTramHienThi() != null ? pr.phanTramHienThi() : BigDecimal.ZERO;
                boolean coKhuyenMai = pr.coKhuyenMai();
                String tenKhuyenMai = pr.tenChuongTrinh() != null ? pr.tenChuongTrinh() : "";

                model.addAttribute("chiTiet", chiTiet);
                model.addAttribute("sanPham", chiTiet.getSanPham());
                model.addAttribute("mauSac", chiTiet.getMauSac());
                model.addAttribute("kichThuoc", chiTiet.getKichThuoc());
                model.addAttribute("chatLieuDay", chiTiet.getChatLieuDay());
                model.addAttribute("loaiMay",
                        chiTiet.getSanPham() != null ? chiTiet.getSanPham().getLoaiMay() : null);
                model.addAttribute("giaGoc", giaGoc);
                model.addAttribute("giaKhuyenMai", giaKhuyenMai);
                model.addAttribute("soTienGiam", soTienGiam);
                model.addAttribute("phanTramGiam", phanTramGiam);
                model.addAttribute("loaiGiamHienThi",
                        coKhuyenMai && pr.loaiGiamApDung() != KhuyenMaiPriceResult.LoaiGiamApDung.KHONG
                                ? pr.loaiGiamApDung().name()
                                : null);
                model.addAttribute("coKhuyenMai", coKhuyenMai);
                model.addAttribute("tenKhuyenMai", tenKhuyenMai);
                model.addAttribute("danhSachBienThe", danhSachBienThe);
            } catch (Exception e) {
                System.err.println("Lỗi khi load chi tiết sản phẩm: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            model.addAttribute("errorMessage", "Sản phẩm chi tiết không tồn tại");
        }

        return "user/SanPhamChiTiet";
    }
}
