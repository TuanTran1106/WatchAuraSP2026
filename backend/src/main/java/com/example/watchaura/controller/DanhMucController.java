//package com.example.watchaura.controller;
//
//import com.example.watchaura.entity.DanhMuc;
//import com.example.watchaura.service.DanhMucService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import java.util.List;
//
//@Controller
//@RequestMapping("/admin/danh-muc")
//@RequiredArgsConstructor
//public class DanhMucController {
//
//    private final DanhMucService danhMucService;
//
//    @GetMapping
//    public String list(Model model) {
//        model.addAttribute("title", "Danh mục");
//        return "admin/quan-ly-danh-muc";
//    }
//
//    @GetMapping("/them")
//    public String formCreate(Model model) {
//        model.addAttribute("title", "Thêm danh mục");
//        model.addAttribute("content", "admin/danhmuc-form");
//        model.addAttribute("danhMuc", new DanhMuc());
//        model.addAttribute("formAction", "/admin/danh-muc");
//        return "layout/admin-layout";
//    }
//
//    @GetMapping("/{id}/sua")
//    public String formEdit(@PathVariable Integer id, Model model) {
//        DanhMuc danhMuc = danhMucService.getById(id);
//        model.addAttribute("title", "Sửa danh mục");
//        model.addAttribute("content", "admin/danhmuc-form");
//        model.addAttribute("danhMuc", danhMuc);
//        model.addAttribute("formAction", "/admin/danh-muc/" + id);
//        return "layout/admin-layout";
//    }
//
//    @PostMapping
//    public String create(@ModelAttribute DanhMuc danhMuc, RedirectAttributes redirect) {
//        danhMucService.create(danhMuc);
//        redirect.addFlashAttribute("message", "Thêm danh mục thành công.");
//        return "redirect:/admin/danh-muc";
//    }
//
//    @PostMapping("/{id}")
//    public String update(@PathVariable Integer id, @ModelAttribute DanhMuc danhMuc, RedirectAttributes redirect) {
//        danhMucService.update(id, danhMuc);
//        redirect.addFlashAttribute("message", "Cập nhật danh mục thành công.");
//        return "redirect:/admin/danh-muc";
//    }
//
//    @PostMapping("/{id}/xoa")
//    public String delete(@PathVariable Integer id, RedirectAttributes redirect) {
//        danhMucService.delete(id);
//        redirect.addFlashAttribute("message", "Xóa danh mục thành công.");
//        return "redirect:/admin/danh-muc";
//    }
//}
package com.example.watchaura.controller;

import com.example.watchaura.entity.DanhMuc;
import com.example.watchaura.service.DanhMucService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/danh-muc")
@CrossOrigin(origins = "*")
public class DanhMucController {

    @Autowired
    private DanhMucService danhMucService;

    @GetMapping
    @ResponseBody
    public ResponseEntity<List<DanhMuc>> getAll() {
        return ResponseEntity.ok(danhMucService.getAll());
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<DanhMuc> getById(
            @PathVariable Integer id) {

        return ResponseEntity.ok(danhMucService.getById(id));
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<DanhMuc> create(
            @RequestBody DanhMuc danhMuc) {

        DanhMuc saved = danhMucService.create(danhMuc);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(saved);
    }

    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<DanhMuc> update(
            @PathVariable Integer id,
            @RequestBody DanhMuc danhMuc) {

        return ResponseEntity.ok(
                danhMucService.update(id, danhMuc)
        );
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Void> delete(
            @PathVariable Integer id) {

        danhMucService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
