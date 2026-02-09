//package com.example.watchaura.controller;
//
//import com.example.watchaura.entity.LoaiMay;
//import com.example.watchaura.repository.LoaiMayRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import java.util.List;
//
//@Controller
//@RequestMapping("/admin/loai-may")
//@RequiredArgsConstructor
//public class LoaiMayController {
//
//    private final LoaiMayRepository loaiMayRepository;
//
//    @GetMapping
//    public String list(Model model) {
//        List<LoaiMay> list = loaiMayRepository.findAll();
//        model.addAttribute("title", "Loại máy");
//        model.addAttribute("content", "admin/loaimay-list");
//        model.addAttribute("list", list);
//        return "layout/admin-layout";
//    }
//
//    @GetMapping("/them")
//    public String formCreate(Model model) {
//        model.addAttribute("title", "Thêm loại máy");
//        model.addAttribute("content", "admin/loaimay-form");
//        model.addAttribute("loaiMay", new LoaiMay());
//        model.addAttribute("formAction", "/admin/loai-may");
//        return "layout/admin-layout";
//    }
//
//    @GetMapping("/{id}/sua")
//    public String formEdit(@PathVariable Integer id, Model model) {
//        LoaiMay loaiMay = loaiMayRepository.findById(id).orElseThrow();
//        model.addAttribute("title", "Sửa loại máy");
//        model.addAttribute("content", "admin/loaimay-form");
//        model.addAttribute("loaiMay", loaiMay);
//        model.addAttribute("formAction", "/admin/loai-may/" + id);
//        return "layout/admin-layout";
//    }
//
//    @PostMapping
//    public String create(@ModelAttribute LoaiMay loaiMay, RedirectAttributes redirect) {
//        loaiMayRepository.save(loaiMay);
//        redirect.addFlashAttribute("message", "Thêm loại máy thành công.");
//        return "redirect:/admin/loai-may";
//    }
//
//    @PostMapping("/{id}")
//    public String update(@PathVariable Integer id, @ModelAttribute LoaiMay loaiMay, RedirectAttributes redirect) {
//        loaiMay.setId(id);
//        loaiMayRepository.save(loaiMay);
//        redirect.addFlashAttribute("message", "Cập nhật loại máy thành công.");
//        return "redirect:/admin/loai-may";
//    }
//
//    @PostMapping("/{id}/xoa")
//    public String delete(@PathVariable Integer id, RedirectAttributes redirect) {
//        loaiMayRepository.deleteById(id);
//        redirect.addFlashAttribute("message", "Xóa loại máy thành công.");
//        return "redirect:/admin/loai-may";
//    }
//}
package com.example.watchaura.controller;

import com.example.watchaura.entity.LoaiMay;
import com.example.watchaura.repository.LoaiMayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/loai-may")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LoaiMayController {

    private final LoaiMayRepository loaiMayRepository;

    /**
     * GET: Lấy tất cả loại máy
     */
    @GetMapping
    @ResponseBody
    public ResponseEntity<List<LoaiMay>> getAllLoaiMay() {
        try {
            List<LoaiMay> loaiMays = loaiMayRepository.findAll();
            return ResponseEntity.ok(loaiMays);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET: Lấy loại máy theo ID
     */
    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> getLoaiMayById(@PathVariable Integer id) {
        try {
            return loaiMayRepository.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST: Tạo mới loại máy
     */
    @PostMapping
    @ResponseBody
    public ResponseEntity<?> createLoaiMay(@RequestBody LoaiMay loaiMay) {
        try {
            LoaiMay saved = loaiMayRepository.save(loaiMay);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * PUT: Cập nhật loại máy
     */
    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> updateLoaiMay(@PathVariable Integer id, @RequestBody LoaiMay loaiMay) {
        try {
            return loaiMayRepository.findById(id)
                    .map(existing -> {
                        loaiMay.setId(id);
                        return ResponseEntity.ok(loaiMayRepository.save(loaiMay));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * DELETE: Xóa loại máy
     */
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteLoaiMay(@PathVariable Integer id) {
        try {
            if (loaiMayRepository.existsById(id)) {
                loaiMayRepository.deleteById(id);
                return ResponseEntity.ok("Xóa thành công");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
