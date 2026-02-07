//package com.example.watchaura.controller;
//
//import com.example.watchaura.entity.ThuongHieu;
//import com.example.watchaura.service.ThuongHieuService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import java.util.List;
//
//@Controller
//@RequestMapping("/admin/thuong-hieu")
//@RequiredArgsConstructor
//public class ThuongHieuController {
//
//    private final ThuongHieuService thuongHieuService;
//
//    @GetMapping
//    public String list(Model model) {
//        List<ThuongHieu> list = thuongHieuService.getAll();
//        model.addAttribute("title", "Thương hiệu");
//        model.addAttribute("content", "admin/thuonghieu-list");
//        model.addAttribute("list", list);
//        return "layout/admin-layout";
//    }
//
//    @GetMapping("/them")
//    public String formCreate(Model model) {
//        model.addAttribute("title", "Thêm thương hiệu");
//        model.addAttribute("content", "admin/thuonghieu-form");
//        model.addAttribute("thuongHieu", new ThuongHieu());
//        model.addAttribute("formAction", "/admin/thuong-hieu");
//        return "layout/admin-layout";
//    }
//
//    @GetMapping("/{id}/sua")
//    public String formEdit(@PathVariable Integer id, Model model) {
//        ThuongHieu thuongHieu = thuongHieuService.getById(id);
//        model.addAttribute("title", "Sửa thương hiệu");
//        model.addAttribute("content", "admin/thuonghieu-form");
//        model.addAttribute("thuongHieu", thuongHieu);
//        model.addAttribute("formAction", "/admin/thuong-hieu/" + id);
//        return "layout/admin-layout";
//    }
//
//    @PostMapping
//    public String create(@ModelAttribute ThuongHieu thuongHieu, RedirectAttributes redirect) {
//        thuongHieuService.create(thuongHieu);
//        redirect.addFlashAttribute("message", "Thêm thương hiệu thành công.");
//        return "redirect:/admin/thuong-hieu";
//    }
//
//    @PostMapping("/{id}")
//    public String update(@PathVariable Integer id, @ModelAttribute ThuongHieu thuongHieu, RedirectAttributes redirect) {
//        thuongHieuService.update(id, thuongHieu);
//        redirect.addFlashAttribute("message", "Cập nhật thương hiệu thành công.");
//        return "redirect:/admin/thuong-hieu";
//    }
//
//    @PostMapping("/{id}/xoa")
//    public String delete(@PathVariable Integer id, RedirectAttributes redirect) {
//        thuongHieuService.delete(id);
//        redirect.addFlashAttribute("message", "Xóa thương hiệu thành công.");
//        return "redirect:/admin/thuong-hieu";
//    }
//}
package com.example.watchaura.controller;

import com.example.watchaura.entity.ThuongHieu;
import com.example.watchaura.service.ThuongHieuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/thuong-hieu")
public class ThuongHieuController {

    @Autowired
    private ThuongHieuService thuongHieuService;

    @GetMapping
    @ResponseBody
    public ResponseEntity<List<ThuongHieu>> getAll() {
        return ResponseEntity.ok(thuongHieuService.getAll());
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<ThuongHieu> getById(
            @PathVariable Integer id) {

        return ResponseEntity.ok(thuongHieuService.getById(id));
    }

    @PostMapping
    @ResponseBody
    public ResponseEntity<ThuongHieu> create(
            @RequestBody ThuongHieu thuongHieu) {

        ThuongHieu saved = thuongHieuService.create(thuongHieu);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(saved);
    }

    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<ThuongHieu> update(
            @PathVariable Integer id,
            @RequestBody ThuongHieu thuongHieu) {

        return ResponseEntity.ok(
                thuongHieuService.update(id, thuongHieu)
        );
    }

    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<Void> delete(
            @PathVariable Integer id) {

        thuongHieuService.delete(id);
        return ResponseEntity.noContent().build();
    }
}

