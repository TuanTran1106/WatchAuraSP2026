package com.example.watchaura.controller;

import com.example.watchaura.dto.CartResponse;
import com.example.watchaura.entity.*;
import com.example.watchaura.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/ban-hang")
public class BanHangTaiQuayController {

    @Autowired
    private SanPhamChiTietRepository sanPhamChiTietRepository;

    @Autowired
    private KhachHangRepository khachHangRepository;

    @Autowired
    private HoaDonRepository hoaDonRepository;

    @Autowired
    private HoaDonChiTietRepository hoaDonChiTietRepository;
    @Autowired
    private VoucherRepository voucherRepository;


    // =============================
    // MỞ TRANG BÁN HÀNG
    // =============================

    @GetMapping
    public String banHang(Model model){

        List<SanPhamChiTiet> sanPhamList =
                sanPhamChiTietRepository.findAllWithDetails();

        model.addAttribute("sanPhamList", sanPhamList);

        // Tạo hóa đơn mới cho khách vãng lai (không cần lưu KhachHang)
        HoaDon hoaDon = new HoaDon();

        hoaDon.setMaDonHang("HD"+System.currentTimeMillis());
        hoaDon.setTenKhachHang("Khách vãng lai");
        hoaDon.setSdtKhachHang("0000000000");

        hoaDon.setDiaChi("Mua tại cửa hàng");

        hoaDon.setLoaiHoaDon("OFFLINE");
        hoaDon.setTrangThaiDonHang("CHO_THANH_TOAN");

        hoaDon.setPhuongThucThanhToan("TIEN_MAT");

        hoaDon.setTongTienTamTinh(BigDecimal.ZERO);
        hoaDon.setTienGiam(BigDecimal.ZERO);
        hoaDon.setTongTienThanhToan(BigDecimal.ZERO);

        hoaDon.setTrangThai(true);
        hoaDon.setNgayDat(LocalDateTime.now());

        hoaDonRepository.save(hoaDon);

        model.addAttribute("hoaDonId", hoaDon.getId());

        return "admin/banhang/ban-hang";
    }


    // =============================
    // THÊM SẢN PHẨM VÀO GIỎ
    // =============================

    @PostMapping("/them-san-pham")
    @ResponseBody
    public String themSanPham(Integer hoaDonId,Integer sanPhamChiTietId){

        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).get();

        SanPhamChiTiet sp =
                sanPhamChiTietRepository.findById(sanPhamChiTietId).get();

        List<HoaDonChiTiet> list =
                hoaDonChiTietRepository.findByHoaDonId(hoaDonId);

        HoaDonChiTiet existing = null;

        for(HoaDonChiTiet ct:list){
            if(ct.getSanPhamChiTiet().getId().equals(sanPhamChiTietId)){
                existing = ct;
                break;
            }
        }

        if(existing != null){

            existing.setSoLuong(existing.getSoLuong()+1);

            hoaDonChiTietRepository.save(existing);

        }else{

            HoaDonChiTiet ct = new HoaDonChiTiet();

            ct.setHoaDon(hoaDon);
            ct.setSanPhamChiTiet(sp);
            ct.setSoLuong(1);
            ct.setDonGia(sp.getGiaBan());

            hoaDonChiTietRepository.save(ct);

        }

        updateTongTien(hoaDonId);

        return "OK";
    }


    // =============================
    // LOAD GIỎ HÀNG
    // =============================

    @GetMapping("/cart/{hoaDonId}")
    @ResponseBody
    public CartResponse getCart(@PathVariable Integer hoaDonId){

        List<HoaDonChiTiet> list =
                hoaDonChiTietRepository.findByHoaDonId(hoaDonId);

        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).get();

        CartResponse res = new CartResponse();

        res.setItems(list);
        res.setTamTinh(hoaDon.getTongTienTamTinh());
        res.setGiamGia(hoaDon.getTienGiam());
        res.setTongThanhToan(hoaDon.getTongTienThanhToan());

        return res;
    }


    // =============================
    // CẬP NHẬT TỔNG TIỀN
    // =============================

    public void updateTongTien(Integer hoaDonId){

        List<HoaDonChiTiet> list =
                hoaDonChiTietRepository.findByHoaDonId(hoaDonId);

        BigDecimal tong = BigDecimal.ZERO;

        for(HoaDonChiTiet ct:list){

            BigDecimal thanhTien =
                    ct.getDonGia().multiply(
                            BigDecimal.valueOf(ct.getSoLuong())
                    );

            tong = tong.add(thanhTien);

        }

        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).get();

        hoaDon.setTongTienTamTinh(tong);
        hoaDon.setTongTienThanhToan(tong);

        hoaDonRepository.save(hoaDon);
    }

    @PostMapping("/tang-so-luong")
    @ResponseBody
    public String tangSoLuong(Integer id){

        HoaDonChiTiet ct = hoaDonChiTietRepository.findById(id).get();

        ct.setSoLuong(ct.getSoLuong() + 1);

        hoaDonChiTietRepository.save(ct);

        updateTongTien(ct.getHoaDon().getId());

        return "OK";

    }

    @PostMapping("/giam-so-luong")
    @ResponseBody
    public String giamSoLuong(Integer id){

        HoaDonChiTiet ct = hoaDonChiTietRepository.findById(id).get();

        if(ct.getSoLuong() > 1){

            ct.setSoLuong(ct.getSoLuong() - 1);

            hoaDonChiTietRepository.save(ct);

        }else{

            hoaDonChiTietRepository.delete(ct);

        }

        updateTongTien(ct.getHoaDon().getId());

        return "OK";

    }

    @PostMapping("/xoa-san-pham")
    @ResponseBody
    public String xoaSanPham(Integer id){

        HoaDonChiTiet ct = hoaDonChiTietRepository.findById(id).get();

        Integer hoaDonId = ct.getHoaDon().getId();

        hoaDonChiTietRepository.delete(ct);

        updateTongTien(hoaDonId);

        return "OK";

    }

    @PostMapping("/thanh-toan")
    @ResponseBody
    public String thanhToan(Integer hoaDonId,String phuongThuc){

        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).get();

        List<HoaDonChiTiet> list =
                hoaDonChiTietRepository.findByHoaDonId(hoaDonId);

        for(HoaDonChiTiet ct:list){

            SanPhamChiTiet sp = ct.getSanPhamChiTiet();

            sp.setSoLuongTon(
                    sp.getSoLuongTon() - ct.getSoLuong()
            );

            sanPhamChiTietRepository.save(sp);

        }

        hoaDon.setTrangThaiDonHang("DA_THANH_TOAN");

        hoaDon.setPhuongThucThanhToan(phuongThuc);

        hoaDonRepository.save(hoaDon);

        return "OK";
    }

    @PostMapping("/ap-dung-voucher")
    @ResponseBody
    public String apDungVoucher(String maVoucher, Integer hoaDonId){

        Voucher voucher = voucherRepository
                .findByMaVoucherIgnoreCase(maVoucher)
                .orElse(null);

        if(voucher == null){
            return "Voucher không tồn tại";
        }

        HoaDon hoaDon = hoaDonRepository.findById(hoaDonId).get();

        BigDecimal tongTien = hoaDon.getTongTienTamTinh();

        // kiểm tra đơn tối thiểu
        if(voucher.getDonHangToiThieu() != null &&
                tongTien.compareTo(voucher.getDonHangToiThieu()) < 0){

            return "Đơn chưa đạt giá trị tối thiểu";
        }

        BigDecimal giam = BigDecimal.ZERO;

// giảm theo %
        if(voucher.getLoaiVoucher().equalsIgnoreCase("PERCENT")){

            giam = tongTien
                    .multiply(voucher.getGiaTri())
                    .divide(BigDecimal.valueOf(100));

            if(voucher.getGiaTriToiDa()!=null &&
                    giam.compareTo(voucher.getGiaTriToiDa()) > 0){

                giam = voucher.getGiaTriToiDa();
            }
        }

// giảm tiền trực tiếp
        else if(voucher.getLoaiVoucher().equalsIgnoreCase("FIXED")){

            giam = voucher.getGiaTri();

        }


        hoaDon.setTienGiam(giam);

        hoaDon.setTongTienThanhToan(
                tongTien.subtract(giam)
        );

        hoaDonRepository.save(hoaDon);

        return "Áp dụng voucher thành công";
    }

}