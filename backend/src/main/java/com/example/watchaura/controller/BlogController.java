package com.example.watchaura.controller;

import com.example.watchaura.entity.Blog;
import com.example.watchaura.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/blog")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;

    @GetMapping
    public String list(Model model) {
        List<Blog> list = blogService.getAll();
        model.addAttribute("title", "Blog");
        model.addAttribute("content", "admin/blog-list");
        model.addAttribute("list", list);
        return "layout/admin-layout";
    }

    @GetMapping("/them")
    public String formCreate(Model model) {
        model.addAttribute("title", "Thêm blog");
        model.addAttribute("content", "admin/blog-form");
        model.addAttribute("blog", new Blog());
        model.addAttribute("formAction", "/admin/blog");
        return "layout/admin-layout";
    }

    @GetMapping("/{id}/sua")
    public String formEdit(@PathVariable Integer id, Model model) {
        Blog blog = blogService.getById(id);
        model.addAttribute("title", "Sửa blog");
        model.addAttribute("content", "admin/blog-form");
        model.addAttribute("blog", blog);
        model.addAttribute("formAction", "/admin/blog/" + id);
        return "layout/admin-layout";
    }

    @PostMapping
    public String create(@ModelAttribute Blog blog, RedirectAttributes redirect) {
        blogService.create(blog);
        redirect.addFlashAttribute("message", "Thêm blog thành công.");
        return "redirect:/admin/blog";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id, @ModelAttribute Blog blog, RedirectAttributes redirect) {
        blogService.update(id, blog);
        redirect.addFlashAttribute("message", "Cập nhật blog thành công.");
        return "redirect:/admin/blog";
    }

    @PostMapping("/{id}/xoa")
    public String delete(@PathVariable Integer id, RedirectAttributes redirect) {
        blogService.delete(id);
        redirect.addFlashAttribute("message", "Xóa blog thành công.");
        return "redirect:/admin/blog";
    }
}
