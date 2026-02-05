package com.example.watchaura.controller;

import com.example.watchaura.entity.Blog;
import com.example.watchaura.service.BlogService;
import com.example.watchaura.service.FileUploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/blog")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;
    private final FileUploadService fileUploadService;

    private static final int PAGE_SIZE = 6;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<Blog> pageResult = blogService.getPage(pageable);
        model.addAttribute("title", "Blog");
        model.addAttribute("content", "admin/blog-list");
        model.addAttribute("list", pageResult.getContent());
        model.addAttribute("page", pageResult);
        model.addAttribute("blog", new Blog());
        model.addAttribute("formAction", "/admin/blog");
        return "layout/admin-layout";
    }

    @GetMapping("/them")
    public String formCreate() {
        return "redirect:/admin/blog";
    }

    @GetMapping("/{id}/sua")
    public String formEdit(@PathVariable Integer id, @RequestParam(defaultValue = "0") int page, Model model) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE);
        Page<Blog> pageResult = blogService.getPage(pageable);
        Blog blog = blogService.getById(id);
        model.addAttribute("title", "Blog");
        model.addAttribute("content", "admin/blog-list");
        model.addAttribute("list", pageResult.getContent());
        model.addAttribute("page", pageResult);
        model.addAttribute("blog", blog);
        model.addAttribute("blogId", id);
        model.addAttribute("formAction", "/admin/blog/" + id + "?page=" + page);
        return "layout/admin-layout";
    }

    @PostMapping
    public String create(@Validated(Blog.OnCreate.class) @ModelAttribute("blog") Blog blog,
                         BindingResult result,
                         @RequestParam(value = "hinhAnhFile", required = false) MultipartFile hinhAnhFile,
                         Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            Pageable pageable = PageRequest.of(0, PAGE_SIZE);
            Page<Blog> pageResult = blogService.getPage(pageable);
            model.addAttribute("title", "Blog");
            model.addAttribute("content", "admin/blog-list");
            model.addAttribute("list", pageResult.getContent());
            model.addAttribute("page", pageResult);
            model.addAttribute("formAction", "/admin/blog");
            return "layout/admin-layout";
        }
        if (hinhAnhFile != null && !hinhAnhFile.isEmpty()) {
            try {
                String path = fileUploadService.uploadFile(hinhAnhFile);
                blog.setHinhAnh(path);
            } catch (Exception e) {
                Pageable pageable = PageRequest.of(0, PAGE_SIZE);
                Page<Blog> pageResult = blogService.getPage(pageable);
                model.addAttribute("title", "Blog");
                model.addAttribute("content", "admin/blog-list");
                model.addAttribute("list", pageResult.getContent());
                model.addAttribute("page", pageResult);
                model.addAttribute("blog", blog);
                model.addAttribute("formAction", "/admin/blog");
                model.addAttribute("error", e.getMessage());
                return "layout/admin-layout";
            }
        }
        blogService.create(blog);
        redirect.addFlashAttribute("message", "Thêm blog thành công.");
        return "redirect:/admin/blog#listBlog";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id, @RequestParam(defaultValue = "0") int page,
                         @Valid @ModelAttribute("blog") Blog blog,
                         BindingResult result,
                         @RequestParam(value = "hinhAnhFile", required = false) MultipartFile hinhAnhFile,
                         Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            Pageable pageable = PageRequest.of(page, PAGE_SIZE);
            Page<Blog> pageResult = blogService.getPage(pageable);
            model.addAttribute("title", "Blog");
            model.addAttribute("content", "admin/blog-list");
            model.addAttribute("list", pageResult.getContent());
            model.addAttribute("page", pageResult);
            model.addAttribute("blogId", id);
            model.addAttribute("formAction", "/admin/blog/" + id + "?page=" + page);
            return "layout/admin-layout";
        }
        if (hinhAnhFile != null && !hinhAnhFile.isEmpty()) {
            try {
                String path = fileUploadService.uploadFile(hinhAnhFile);
                blog.setHinhAnh(path);
            } catch (Exception e) {
                Pageable pageable = PageRequest.of(page, PAGE_SIZE);
                Page<Blog> pageResult = blogService.getPage(pageable);
                model.addAttribute("title", "Blog");
                model.addAttribute("content", "admin/blog-list");
                model.addAttribute("list", pageResult.getContent());
                model.addAttribute("page", pageResult);
                model.addAttribute("blog", blog);
                model.addAttribute("blogId", id);
                model.addAttribute("formAction", "/admin/blog/" + id + "?page=" + page);
                model.addAttribute("error", e.getMessage());
                return "layout/admin-layout";
            }
        } else {
            Blog existing = blogService.getById(id);
            blog.setHinhAnh(existing.getHinhAnh());
        }
        blogService.update(id, blog);
        redirect.addFlashAttribute("message", "Cập nhật blog thành công.");
        redirect.addAttribute("page", page);
        return "redirect:/admin/blog#listBlog";
    }

    @PostMapping("/{id}/xoa")
    public String delete(@PathVariable Integer id, @RequestParam(defaultValue = "0") int page, RedirectAttributes redirect) {
        blogService.delete(id);
        redirect.addFlashAttribute("message", "Xóa blog thành công.");
        redirect.addAttribute("page", page);
        return "redirect:/admin/blog#listBlog";
    }
}
