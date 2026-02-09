//package com.example.watchaura.controller;
//
//import com.example.watchaura.entity.MauSac;
//import com.example.watchaura.repository.MauSacRepository;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import java.util.List;
//
//@Controller
//@RequestMapping("/admin/mau-sac")
//@RequiredArgsConstructor
//public class MauSacController {
//
//    private final MauSacRepository mauSacRepository;
//
//    @GetMapping
//    public String list(Model model) {
//        List<MauSac> list = mauSacRepository.findAll();
//        model.addAttribute("title", "Màu sắc");
//        model.addAttribute("content", "admin/mausac-list");
//        model.addAttribute("list", list);
//        return "layout/admin-layout";
//    }
//
//    @GetMapping("/them")
//    public String formCreate(Model model) {
//        model.addAttribute("title", "Thêm màu sắc");
//        model.addAttribute("content", "admin/mausac-form");
//        model.addAttribute("mauSac", new MauSac());
//        model.addAttribute("formAction", "/admin/mau-sac");
//        return "layout/admin-layout";
//    }
//
//    @GetMapping("/{id}/sua")
//    public String formEdit(@PathVariable Integer id, Model model) {
//        MauSac mauSac = mauSacRepository.findById(id).orElseThrow();
//        model.addAttribute("title", "Sửa màu sắc");
//        model.addAttribute("content", "admin/mausac-form");
//        model.addAttribute("mauSac", mauSac);
//        model.addAttribute("formAction", "/admin/mau-sac/" + id);
//        return "layout/admin-layout";
//    }
//
//    @PostMapping
//    public String create(@ModelAttribute MauSac mauSac, RedirectAttributes redirect) {
//        mauSacRepository.save(mauSac);
//        redirect.addFlashAttribute("message", "Thêm màu sắc thành công.");
//        return "redirect:/admin/mau-sac";
//    }
//
//    @PostMapping("/{id}")
//    public String update(@PathVariable Integer id, @ModelAttribute MauSac mauSac, RedirectAttributes redirect) {
//        mauSac.setId(id);
//        mauSacRepository.save(mauSac);
//        redirect.addFlashAttribute("message", "Cập nhật màu sắc thành công.");
//        return "redirect:/admin/mau-sac";
//    }
//
//    @PostMapping("/{id}/xoa")
//    public String delete(@PathVariable Integer id, RedirectAttributes redirect) {
//        mauSacRepository.deleteById(id);
//        redirect.addFlashAttribute("message", "Xóa màu sắc thành công.");
//        return "redirect:/admin/mau-sac";
//    }
//}
package com.example.watchaura.controller;

import com.example.watchaura.entity.MauSac;
import com.example.watchaura.repository.MauSacRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/mau-sac")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class MauSacController {

    private final MauSacRepository mauSacRepository;

    /**
     * GET: Lấy tất cả màu sắc
     */
    @GetMapping
    @ResponseBody
    public ResponseEntity<List<MauSac>> getAllMauSac() {
        try {
            List<MauSac> mauSacs = mauSacRepository.findAll();
            return ResponseEntity.ok(mauSacs);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET: Lấy màu sắc theo ID
     */
    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> getMauSacById(@PathVariable Integer id) {
        try {
            return mauSacRepository.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST: Tạo mới màu sắc
     */
    @PostMapping
    @ResponseBody
    public ResponseEntity<?> createMauSac(@RequestBody MauSac mauSac) {
        try {
            MauSac saved = mauSacRepository.save(mauSac);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * PUT: Cập nhật màu sắc
     */
    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> updateMauSac(@PathVariable Integer id, @RequestBody MauSac mauSac) {
        try {
            return mauSacRepository.findById(id)
                    .map(existing -> {
                        mauSac.setId(id);
                        return ResponseEntity.ok(mauSacRepository.save(mauSac));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * DELETE: Xóa màu sắc
     */
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteMauSac(@PathVariable Integer id) {
        try {
            if (mauSacRepository.existsById(id)) {
                mauSacRepository.deleteById(id);
                return ResponseEntity.ok("Xóa thành công");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
