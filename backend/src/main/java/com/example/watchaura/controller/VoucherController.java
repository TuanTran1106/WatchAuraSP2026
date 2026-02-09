package com.example.watchaura.controller;

import com.example.watchaura.entity.Voucher;
import com.example.watchaura.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;


@Controller
@RequestMapping("/admin/voucher")
public class VoucherController {

    @Autowired
    private VoucherService voucherService;

    // ================== LIST ==================
    @GetMapping
    public String list(
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Pageable pageable = PageRequest.of(page, 7);
        Page<Voucher> pageVoucher = voucherService.findAll(pageable);

        model.addAttribute("listVoucher", pageVoucher.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageVoucher.getTotalPages());

        return "voucher/list";
    }


    // ================== ADD FORM ==================
    @GetMapping("/them")
    public String showAddForm(Model model) {
        model.addAttribute("voucher", new Voucher());
        return "voucher/form";
    }

    // ================== SAVE ==================
    @PostMapping("/luu")
    public String save(@ModelAttribute("voucher") Voucher voucher,
                       Model model) {

        if (voucherService.existsByMaVoucher(voucher.getMaVoucher())) {
            model.addAttribute("error", "Mã voucher đã tồn tại!");
            return "voucher/form";
        }

        voucherService.save(voucher);
        return "redirect:/admin/voucher";
    }

    // ================== EDIT FORM ==================
    @GetMapping("/sua/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Voucher voucher = voucherService.findById(id);
        if (voucher == null) {
            return "redirect:/admin/voucher";
        }
        model.addAttribute("voucher", voucher);
        return "voucher/form";
    }

    // ================== UPDATE ==================
    @PostMapping("/cap-nhat/{id}")
    public String update(@PathVariable Integer id,
                         @ModelAttribute("voucher") Voucher voucher) {

        voucherService.update(id, voucher);
        return "redirect:/admin/voucher";
    }

    // ================== DELETE ==================
    @GetMapping("/xoa/{id}")
    public String delete(@PathVariable Integer id) {
        voucherService.delete(id);
        return "redirect:/admin/voucher";
    }
}
