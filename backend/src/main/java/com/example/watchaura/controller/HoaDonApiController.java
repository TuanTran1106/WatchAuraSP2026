package com.example.watchaura.controller;

import com.example.watchaura.entity.HoaDonChiTiet;
import com.example.watchaura.entity.SanPhamChiTiet;
import com.example.watchaura.repository.HoaDonChiTietRepository;
import com.example.watchaura.repository.SanPhamChiTietRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/hoa-don")
@RequiredArgsConstructor
public class HoaDonApiController {

    private final HoaDonChiTietRepository hoaDonChiTietRepository;
    private final SanPhamChiTietRepository sanPhamChiTietRepository;

    public static class TonKhoItem {
        public int sanPhamChiTietId;
        public String tenSanPham;
        public int soLuongCan;
        public int soLuongTon;
        public boolean du;
        public TonKhoItem() {}
        public TonKhoItem(int id, String ten, int can, int ton, boolean du) {
            this.sanPhamChiTietId = id;
            this.tenSanPham = ten;
            this.soLuongCan = can;
            this.soLuongTon = ton;
            this.du = du;
        }
    }

    public static class TonKhoResult {
        public boolean du;
        public List<TonKhoItem> items = new ArrayList<>();
    }

    @GetMapping("/{id}/check-ton-kho")
    @Transactional(readOnly = true)
    public ResponseEntity<TonKhoResult> checkTonKho(@PathVariable Integer id) {
        List<HoaDonChiTiet> chiTiets = hoaDonChiTietRepository.findByHoaDonId(id);
        TonKhoResult result = new TonKhoResult();
        result.du = true;

        for (HoaDonChiTiet ct : chiTiets) {
            SanPhamChiTiet spct = sanPhamChiTietRepository.findById(ct.getSanPhamChiTiet().getId())
                    .orElse(null);
            if (spct == null) continue;

            int can = ct.getSoLuong() != null ? ct.getSoLuong() : 0;
            int ton = spct.getSoLuongTon() != null ? spct.getSoLuongTon() : 0;
            String ten = spct.getSanPham() != null ? spct.getSanPham().getTenSanPham()
                    : "ID " + ct.getSanPhamChiTiet().getId();
            boolean itemDu = ton >= can;

            result.items.add(new TonKhoItem(
                    ct.getSanPhamChiTiet().getId(), ten, can, ton, itemDu));
            if (!itemDu) result.du = false;
        }

        return ResponseEntity.ok(result);
    }
}
