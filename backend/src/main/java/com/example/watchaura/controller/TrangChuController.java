package com.example.watchaura.controller;

import com.example.watchaura.entity.Blog;
import com.example.watchaura.entity.DanhMuc;
import com.example.watchaura.entity.KhachHang;
import com.example.watchaura.entity.SanPham;
import com.example.watchaura.entity.SanPhamChiTiet;
import com.example.watchaura.entity.ThuongHieu;
import com.example.watchaura.repository.BlogRepository;
import com.example.watchaura.repository.DanhMucRepository;
import com.example.watchaura.repository.KhachHangRepository;
import com.example.watchaura.repository.SanPhamChiTietRepository;
import com.example.watchaura.repository.SanPhamRepository;
import com.example.watchaura.repository.ThuongHieuRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.Optional;

@Controller
@RequestMapping("/san_pham")
@RequiredArgsConstructor
public class TrangChuController {
    @Autowired
    private SanPhamRepository sanPhamRepository;
    @Autowired
    private ThuongHieuRepository thuongHieuRepository;
    @Autowired
    private DanhMucRepository danhMucRepository;
    @Autowired
    private BlogRepository blogRepository;
    @Autowired
    private KhachHangRepository khachHangRepository;
    @Autowired
    private SanPhamChiTietRepository sanPhamChiTietRepository;

    // Thư mục lưu ảnh
    private final String UPLOAD_DIR = new File("uploads/images/").getAbsolutePath();

    @GetMapping("/home")
    public String hienThi(Model model) {
        // Chỉ lấy sản phẩm có trạng thái = true (1)
        List<SanPham> listSP = sanPhamRepository.findByTrangThai(true);
        List<DanhMuc> ListDM = danhMucRepository.findAll();
        List<ThuongHieu> listTH = thuongHieuRepository.findAll();
        List<Blog> listBL = blogRepository.findAll();
        List<KhachHang> listND = khachHangRepository.findAll();

        model.addAttribute("listSP", listSP);
        model.addAttribute("listDM", ListDM);
        model.addAttribute("listTH", listTH);
        model.addAttribute("listBL", listBL);
        model.addAttribute("listND", listND);
        // Thêm thông tin giá bán & màu sắc cho từng sản phẩm
        addSanPhamMetaToModel(listSP, model);
        model.addAttribute("sanPham", new SanPham());
        model.addAttribute("action", "add");
        model.addAttribute("buttonText", "Thêm sản phẩm");
        return "user/TrangChu";
    }

    @GetMapping("/detail/{id}")
    public String chiTiet(@PathVariable Integer id, Model model) {
        Optional<SanPham> sanPhamOpt = sanPhamRepository.findById(id);
        if (sanPhamOpt.isPresent()) {
            List<SanPham> listSP = sanPhamRepository.findAll();
            List<DanhMuc> ListDM = danhMucRepository.findAll();
            List<ThuongHieu> listTH = thuongHieuRepository.findAll();

            model.addAttribute("listSP", listSP);
            model.addAttribute("listDM", ListDM);
            model.addAttribute("listTH", listTH);
            addSanPhamMetaToModel(listSP, model);
            model.addAttribute("sanPham", sanPhamOpt.get());
            model.addAttribute("action", "detail");
            model.addAttribute("buttonText", "Xem chi tiết");
            return "user/TrangChu";
        }
        return "redirect:/san_pham/home";
    }

    @GetMapping("/blog/home")
    public String hienThiBlog(Model model) {
        List<Blog> listBL = blogRepository.findAll();
        List<KhachHang> listND = khachHangRepository.findAll();
        List<SanPham> listSP = sanPhamRepository.findAll();
        List<DanhMuc> ListDM = danhMucRepository.findAll();
        List<ThuongHieu> listTH = thuongHieuRepository.findAll();

        model.addAttribute("listBL", listBL);
        model.addAttribute("listND", listND);
        model.addAttribute("listSP", listSP);
        model.addAttribute("listDM", ListDM);
        model.addAttribute("listTH", listTH);
        addSanPhamMetaToModel(listSP, model);
        model.addAttribute("blog", new Blog());
        model.addAttribute("action", "add");
        model.addAttribute("buttonText", "Thêm blog");
        return "user/TrangChu";
    }

    @GetMapping("/blog/detail/{id}")
    public String chiTietBlog(@PathVariable Integer id, Model model) {
        Optional<Blog> blogOpt = blogRepository.findById(id);
        if (blogOpt.isPresent()) {
            List<Blog> listBL = blogRepository.findAll();
            List<KhachHang> listND = khachHangRepository.findAll();
            List<SanPham> listSP = sanPhamRepository.findAll();
            List<DanhMuc> ListDM = danhMucRepository.findAll();
            List<ThuongHieu> listTH = thuongHieuRepository.findAll();

            model.addAttribute("listBL", listBL);
            model.addAttribute("listND", listND);
            model.addAttribute("listSP", listSP);
            model.addAttribute("listDM", ListDM);
            model.addAttribute("listTH", listTH);
            addSanPhamMetaToModel(listSP, model);
            model.addAttribute("blog", blogOpt.get());
            model.addAttribute("action", "detail");
            model.addAttribute("buttonText", "Xem chi tiết");
            return "user/TrangChu";
        }
        return "redirect:/san_pham/home";
    }

    @GetMapping("/loc")
    public String locSanPham(@RequestParam(required = false) String thuongHieu,
            @RequestParam(required = false) String danhMuc,
            Model model) {

        List<SanPham> listSP = sanPhamRepository.findByTrangThai(true);

        if (thuongHieu != null && !thuongHieu.isEmpty()) {
            listSP = listSP.stream()
                    .filter(sp -> sp.getThuongHieu().getTenThuongHieu().equalsIgnoreCase(thuongHieu))
                    .toList();
        }

        if (danhMuc != null && !danhMuc.isEmpty()) {
            listSP = listSP.stream()
                    .filter(sp -> sp.getDanhMuc().getTenDanhMuc().equalsIgnoreCase(danhMuc))
                    .toList();
        }

        model.addAttribute("listSP", listSP);
        model.addAttribute("listDM", danhMucRepository.findAll());
        model.addAttribute("listTH", thuongHieuRepository.findAll());
        model.addAttribute("listBL", blogRepository.findAll());
        model.addAttribute("listND", khachHangRepository.findAll());
        addSanPhamMetaToModel(listSP, model);
        model.addAttribute("sanPham", new SanPham());
        model.addAttribute("action", "add");
        model.addAttribute("buttonText", "Thêm sản phẩm");
        model.addAttribute("selectedTH", thuongHieu);
        model.addAttribute("selectedDM", danhMuc);

        return "user/TrangChu";
    }

    /**
     * Thêm map giá bán thấp nhất và danh sách màu sắc cho từng sản phẩm vào model.
     */
    private void addSanPhamMetaToModel(List<SanPham> listSP, Model model) {
        Map<Integer, BigDecimal> giaBanByProductId = new HashMap<>();
        Map<Integer, String> mauSacByProductId = new HashMap<>();
        Map<Integer, Integer> firstVariantIdByProductId = new HashMap<>();

        for (SanPham sp : listSP) {
            List<SanPhamChiTiet> chiTietList = sanPhamChiTietRepository
                    .findBySanPhamIdAndTrangThai(sp.getId(), true);

            if (chiTietList == null || chiTietList.isEmpty()) {
                continue;
            }

            // Lưu ID của biến thể đầu tiên
            if (!chiTietList.isEmpty()) {
                firstVariantIdByProductId.put(sp.getId(), chiTietList.get(0).getId());
            }

            // Giá bán thấp nhất trong các biến thể
            BigDecimal minGiaBan = chiTietList.stream()
                    .map(SanPhamChiTiet::getGiaBan)
                    .filter(Objects::nonNull)
                    .min(BigDecimal::compareTo)
                    .orElse(null);

            if (minGiaBan != null) {
                giaBanByProductId.put(sp.getId(), minGiaBan);
            }

            // Danh sách màu sắc (không trùng, theo thứ tự xuất hiện)
            String mauSacHienThi = chiTietList.stream()
                    .map(SanPhamChiTiet::getMauSac)
                    .filter(Objects::nonNull)
                    .map(m -> m.getTenMauSac())
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.joining(", "));

            if (!mauSacHienThi.isEmpty()) {
                mauSacByProductId.put(sp.getId(), mauSacHienThi);
            }
        }

        model.addAttribute("giaBanByProductId", giaBanByProductId);
        model.addAttribute("mauSacByProductId", mauSacByProductId);
        model.addAttribute("firstVariantIdByProductId", firstVariantIdByProductId);
    }
}
