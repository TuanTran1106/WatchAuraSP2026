//package com.example.watchaura.controller;
//
//import com.example.watchaura.dto.SanPhamDTO;
//import com.example.watchaura.dto.SanPhamRequest;
//import com.example.watchaura.service.DanhMucService;
//import com.example.watchaura.service.FileUploadService;
//import com.example.watchaura.service.SanPhamService;
//import com.example.watchaura.service.ThuongHieuService;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import java.util.List;
//
//@Controller
//@RequestMapping("/admin/san-pham")
//@RequiredArgsConstructor
//public class SanPhamController {
//
//    private final SanPhamService sanPhamService;
//    private final FileUploadService fileUploadService;
//    private final ThuongHieuService thuongHieuService;
//    private final DanhMucService danhMucService;
//
//    @GetMapping
//    public String list(Model model) {
//        List<SanPhamDTO> list = sanPhamService.getAllSanPham();
//        model.addAttribute("title", "Sản phẩm");
//        model.addAttribute("content", "admin/sanpham-list");
//        model.addAttribute("list", list);
//        return "layout/admin-layout";
//    }
//
//    @GetMapping("/them")
//    public String formCreate(Model model) {
//        model.addAttribute("title", "Thêm sản phẩm");
//        model.addAttribute("content", "admin/sanpham-form");
//        model.addAttribute("sanPham", new SanPhamRequest());
//        model.addAttribute("thuongHieuList", thuongHieuService.getAll());
//        model.addAttribute("danhMucList", danhMucService.getAll());
//        model.addAttribute("formAction", "/admin/san-pham");
//        return "layout/admin-layout";
//    }
//
//    @GetMapping("/{id}/sua")
//    public String formEdit(@PathVariable Integer id, Model model) {
//        SanPhamDTO dto = sanPhamService.getSanPhamById(id);
//        SanPhamRequest sanPham = new SanPhamRequest();
//        sanPham.setMaSanPham(dto.getMaSanPham());
//        sanPham.setTenSanPham(dto.getTenSanPham());
//        sanPham.setMoTa(dto.getMoTa());
//        sanPham.setHinhAnh(dto.getHinhAnh());
//        sanPham.setIdThuongHieu(dto.getIdThuongHieu());
//        sanPham.setIdDanhMuc(dto.getIdDanhMuc());
//        sanPham.setPhongCach(dto.getPhongCach());
//        sanPham.setTrangThai(dto.getTrangThai());
//        model.addAttribute("title", "Sửa sản phẩm");
//        model.addAttribute("content", "admin/sanpham-form");
//        model.addAttribute("sanPham", sanPham);
//        model.addAttribute("sanPhamId", id);
//        model.addAttribute("thuongHieuList", thuongHieuService.getAll());
//        model.addAttribute("danhMucList", danhMucService.getAll());
//        model.addAttribute("formAction", "/admin/san-pham/" + id);
//        return "layout/admin-layout";
//    }
//
//    @PostMapping
//    public String create(@Valid @ModelAttribute("sanPham") SanPhamRequest request, RedirectAttributes redirect) {
//        sanPhamService.createSanPham(request);
//        redirect.addFlashAttribute("message", "Thêm sản phẩm thành công.");
//        return "redirect:/admin/san-pham";
//    }
//
//    @PostMapping("/{id}")
//    public String update(@PathVariable Integer id, @Valid @ModelAttribute("sanPham") SanPhamRequest request, RedirectAttributes redirect) {
//        sanPhamService.updateSanPham(id, request);
//        redirect.addFlashAttribute("message", "Cập nhật sản phẩm thành công.");
//        return "redirect:/admin/san-pham";
//    }
//
//    @PostMapping("/{id}/xoa")
//    public String delete(@PathVariable Integer id, RedirectAttributes redirect) {
//        sanPhamService.deleteSanPham(id);
//        redirect.addFlashAttribute("message", "Xóa sản phẩm thành công.");
//        return "redirect:/admin/san-pham";
//    }
//
//    @PostMapping("/upload")
//    public String uploadImage(@RequestParam("file") MultipartFile file, RedirectAttributes redirect) {
//        try {
//            String filePath = fileUploadService.uploadFile(file);
//            redirect.addFlashAttribute("message", "Upload thành công: " + filePath);
//        } catch (Exception e) {
//            redirect.addFlashAttribute("error", e.getMessage());
//        }
//        return "redirect:/admin/san-pham";
//    }
//}
package com.example.watchaura.controller;

import com.example.watchaura.dto.SanPhamDTO;
import com.example.watchaura.dto.SanPhamRequest;
import com.example.watchaura.service.FileUploadService;
import com.example.watchaura.service.SanPhamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/api/san-pham")
@RequiredArgsConstructor
public class SanPhamController {

    private final SanPhamService sanPhamService;
    private final FileUploadService fileUploadService;
    // ============ API ENDPOINTS ============
    private static final int DEFAULT_PAGE_SIZE = 5;

    /**
     * GET: Lấy tất cả sản phẩm
     * URL: GET /api/san-pham
     */
    @GetMapping
    @ResponseBody
    public ResponseEntity<List<SanPhamDTO>> getAllSanPham() {
        try {
            List<SanPhamDTO> sanPhams = sanPhamService.getAllSanPham();
            return ResponseEntity.ok(sanPhams);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET: Tìm kiếm & phân trang sản phẩm (cho admin)
     * URL: GET /api/san-pham/search?keyword=...&trangThai=...&page=0&size=5
     */
    @GetMapping("/search")
    @ResponseBody
    public ResponseEntity<Page<SanPhamDTO>> searchPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean trangThai,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            Page<SanPhamDTO> result = sanPhamService.searchPage(keyword, trangThai, pageable);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET: Kiểm tra mã sản phẩm đã tồn tại chưa (dùng khi thêm mới).
     * URL: GET /api/san-pham/check-ma?ma=SP001
     */
    @GetMapping("/check-ma")
    @ResponseBody
    public ResponseEntity<?> checkMaSanPham(@RequestParam String ma) {
        if (ma == null || (ma = ma.trim()).isEmpty()) {
            return ResponseEntity.ok(false);
        }
        boolean exists = sanPhamService.existsByMaSanPham(ma);
        return ResponseEntity.ok(exists);
    }

    /**
     * GET: Lấy sản phẩm theo ID
     * URL: GET /api/san-pham/{id}
     */
    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> getSanPhamById(@PathVariable Integer id) {
        try {
            SanPhamDTO sanPham = sanPhamService.getSanPhamById(id);
            return ResponseEntity.ok(sanPham);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi hệ thống");
        }
    }

    /**
     * POST: Tạo mới sản phẩm
     * URL: POST /api/san-pham
     * Body: SanPhamRequest (JSON)
     */
    @PostMapping
    public ResponseEntity<?> createSanPham(@Valid @RequestBody SanPhamRequest request) {
        try {
            SanPhamDTO sanPham = sanPhamService.createSanPham(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(sanPham);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi hệ thống");
        }
    }

    /**
     * PUT: Cập nhật sản phẩm
     * URL: PUT /api/san-pham/{id}
     * Body: SanPhamRequest (JSON)
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateSanPham(
            @PathVariable Integer id,
            @Valid @RequestBody SanPhamRequest request) {
        try {
            SanPhamDTO sanPham = sanPhamService.updateSanPham(id, request);
            return ResponseEntity.ok(sanPham);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi hệ thống");
        }
    }

    /**
     * DELETE: Xóa sản phẩm
     * URL: DELETE /api/san-pham/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteSanPham(@PathVariable Integer id) {
        try {
            sanPhamService.deleteSanPham(id);
            return ResponseEntity.ok("Xóa sản phẩm thành công");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi hệ thống");
        }
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String filePath = fileUploadService.uploadFile(file);
            return ResponseEntity.ok(new UploadResponse(filePath, "Upload thành công"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi khi upload file"));
        }
    }


    @PutMapping("/{id}/upload")
    public ResponseEntity<?> updateProductImage(
            @PathVariable Integer id,
            @RequestParam("file") MultipartFile file) {
        try {
            String newFilePath = fileUploadService.uploadFile(file);
            sanPhamService.updateSanPhamImage(id, newFilePath);
            return ResponseEntity.ok(new UploadResponse(newFilePath, "Cập nhật ảnh thành công"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Lỗi khi cập nhật ảnh"));
        }
    }

    // Helper classes for response
    public static class UploadResponse {
        public String filePath;
        public String message;

        public UploadResponse(String filePath, String message) {
            this.filePath = filePath;
            this.message = message;
        }
    }

    public static class ErrorResponse {
        public String error;

        public ErrorResponse(String error) {
            this.error = error;
        }
    }

}