//package com.example.watchaura.controller;
//
//import com.example.watchaura.entity.KichThuoc;
//import com.example.watchaura.repository.KichThuocRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import java.util.List;
//
//@Controller
//@RequestMapping("/admin/kich-thuoc")
//@RequiredArgsConstructor
//public class KichThuocController {
//
//    private final KichThuocRepository kichThuocRepository;
//
//    @GetMapping
//    public String list(Model model) {
//        List<KichThuoc> list = kichThuocRepository.findAll();
//        model.addAttribute("title", "Kích thước");
//        model.addAttribute("content", "admin/kichthuoc-list");
//        model.addAttribute("list", list);
//        return "layout/admin-layout";
//    }
//
//    @GetMapping("/them")
//    public String formCreate(Model model) {
//        model.addAttribute("title", "Thêm kích thước");
//        model.addAttribute("content", "admin/kichthuoc-form");
//        model.addAttribute("kichThuoc", new KichThuoc());
//        model.addAttribute("formAction", "/admin/kich-thuoc");
//        return "layout/admin-layout";
//    }
//
//    @GetMapping("/{id}/sua")
//    public String formEdit(@PathVariable Integer id, Model model) {
//        KichThuoc kichThuoc = kichThuocRepository.findById(id).orElseThrow();
//        model.addAttribute("title", "Sửa kích thước");
//        model.addAttribute("content", "admin/kichthuoc-form");
//        model.addAttribute("kichThuoc", kichThuoc);
//        model.addAttribute("formAction", "/admin/kich-thuoc/" + id);
//        return "layout/admin-layout";
//    }
//
//    @PostMapping
//    public String create(@ModelAttribute KichThuoc kichThuoc, RedirectAttributes redirect) {
//        kichThuocRepository.save(kichThuoc);
//        redirect.addFlashAttribute("message", "Thêm kích thước thành công.");
//        return "redirect:/admin/kich-thuoc";
//    }
//
//    @PostMapping("/{id}")
//    public String update(@PathVariable Integer id, @ModelAttribute KichThuoc kichThuoc, RedirectAttributes redirect) {
//        kichThuoc.setId(id);
//        kichThuocRepository.save(kichThuoc);
//        redirect.addFlashAttribute("message", "Cập nhật kích thước thành công.");
//        return "redirect:/admin/kich-thuoc";
//    }
//
//    @PostMapping("/{id}/xoa")
//    public String delete(@PathVariable Integer id, RedirectAttributes redirect) {
//        kichThuocRepository.deleteById(id);
//        redirect.addFlashAttribute("message", "Xóa kích thước thành công.");
//        return "redirect:/admin/kich-thuoc";
//    }
//}
package com.example.watchaura.controller;

import com.example.watchaura.entity.KichThuoc;
import com.example.watchaura.repository.KichThuocRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/kich-thuoc")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class KichThuocController {

    private final KichThuocRepository kichThuocRepository;

    /**
     * GET: Lấy tất cả kích thước
     */
    @GetMapping
    @ResponseBody
    public ResponseEntity<List<KichThuoc>> getAllKichThuoc() {
        try {
            List<KichThuoc> kichThuocs = kichThuocRepository.findAll();
            return ResponseEntity.ok(kichThuocs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET: Lấy kích thước theo ID
     */
    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> getKichThuocById(@PathVariable Integer id) {
        try {
            return kichThuocRepository.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST: Tạo mới kích thước
     */
    @PostMapping
    @ResponseBody
    public ResponseEntity<?> createKichThuoc(@RequestBody KichThuoc kichThuoc) {
        try {
            KichThuoc saved = kichThuocRepository.save(kichThuoc);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * PUT: Cập nhật kích thước
     */
    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> updateKichThuoc(@PathVariable Integer id, @RequestBody KichThuoc kichThuoc) {
        try {
            return kichThuocRepository.findById(id)
                    .map(existing -> {
                        kichThuoc.setId(id);
                        return ResponseEntity.ok(kichThuocRepository.save(kichThuoc));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * DELETE: Xóa kích thước
     */
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteKichThuoc(@PathVariable Integer id) {
        try {
            if (kichThuocRepository.existsById(id)) {
                kichThuocRepository.deleteById(id);
                return ResponseEntity.ok("Xóa thành công");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
