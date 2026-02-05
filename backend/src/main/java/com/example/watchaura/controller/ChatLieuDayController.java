package com.example.watchaura.controller;

import com.example.watchaura.entity.ChatLieuDay;
import com.example.watchaura.repository.ChatLieuDayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/chat-lieu-day")
@RequiredArgsConstructor
public class ChatLieuDayController {

    private final ChatLieuDayRepository chatLieuDayRepository;

    @GetMapping
    public String list(Model model) {
        List<ChatLieuDay> list = chatLieuDayRepository.findAll();
        model.addAttribute("title", "Chất liệu dây");
        model.addAttribute("content", "admin/chatlieuday-list");
        model.addAttribute("list", list);
        model.addAttribute("chatLieuDay", new ChatLieuDay());
        model.addAttribute("chatLieuDayId", null);
        model.addAttribute("formAction", "/admin/chat-lieu-day");
        return "layout/admin-layout";
    }

    @GetMapping("/them")
    public String formCreate() {
        return "redirect:/admin/chat-lieu-day";
    }

    @GetMapping("/{id}/sua")
    public String formEdit(@PathVariable Integer id, Model model) {
        List<ChatLieuDay> list = chatLieuDayRepository.findAll();
        ChatLieuDay chatLieuDay = chatLieuDayRepository.findById(id).orElseThrow();
        model.addAttribute("title", "Chất liệu dây");
        model.addAttribute("content", "admin/chatlieuday-list");
        model.addAttribute("list", list);
        model.addAttribute("chatLieuDay", chatLieuDay);
        model.addAttribute("chatLieuDayId", id);
        model.addAttribute("formAction", "/admin/chat-lieu-day/" + id);
        return "layout/admin-layout";
    }

    @PostMapping
    public String create(@ModelAttribute ChatLieuDay chatLieuDay, RedirectAttributes redirect) {
        chatLieuDayRepository.save(chatLieuDay);
        redirect.addFlashAttribute("message", "Thêm chất liệu dây thành công.");
        return "redirect:/admin/chat-lieu-day";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id, @ModelAttribute ChatLieuDay chatLieuDay, RedirectAttributes redirect) {
        chatLieuDay.setId(id);
        chatLieuDayRepository.save(chatLieuDay);
        redirect.addFlashAttribute("message", "Cập nhật chất liệu dây thành công.");
        return "redirect:/admin/chat-lieu-day";
    }

    @PostMapping("/{id}/xoa")
    public String delete(@PathVariable Integer id, RedirectAttributes redirect) {
        chatLieuDayRepository.deleteById(id);
        redirect.addFlashAttribute("message", "Xóa chất liệu dây thành công.");
        return "redirect:/admin/chat-lieu-day";
    }
}
