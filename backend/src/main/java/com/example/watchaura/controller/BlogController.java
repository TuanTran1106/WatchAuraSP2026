package com.example.watchaura.controller;

import com.example.watchaura.entity.Blog;
import com.example.watchaura.service.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/blog")
public class BlogController {

    @Autowired
    private BlogService blogService;

    // ================== LIST ==================
    @GetMapping
    public String list(Model model) {
        model.addAttribute("listBlog", blogService.findAll());
        return "blog/list";
    }

    // ================== ADD FORM ==================
    @GetMapping("/them")
    public String showAddForm(Model model) {
        model.addAttribute("blog", new Blog());
        return "blog/form";
    }

    // ================== SAVE ==================
    @PostMapping("/luu")
    public String save(@ModelAttribute("blog") Blog blog) {
        blogService.save(blog);
        return "redirect:/admin/blog";
    }

    // ================== EDIT FORM ==================
    @GetMapping("/sua/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Blog blog = blogService.findById(id);
        if (blog == null) {
            return "redirect:/admin/blog";
        }
        model.addAttribute("blog", blog);
        return "blog/form";
    }

    // ================== UPDATE ==================
    @PostMapping("/cap-nhat/{id}")
    public String update(@PathVariable Integer id,
                         @ModelAttribute("blog") Blog blog) {

        blogService.update(id, blog);
        return "redirect:/admin/blog";
    }

    // ================== DELETE ==================
    @GetMapping("/xoa/{id}")
    public String delete(@PathVariable Integer id) {
        blogService.delete(id);
        return "redirect:/admin/blog";
    }
}
