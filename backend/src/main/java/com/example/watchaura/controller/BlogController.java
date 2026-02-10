package com.example.watchaura.controller;

import com.example.watchaura.entity.Blog;
import com.example.watchaura.service.BlogService;
import com.example.watchaura.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/admin/blog")
@RequiredArgsConstructor
public class BlogController {

    private final BlogService blogService;
    private final FileUploadService fileUploadService;

    private static final int PAGE_SIZE = 6;

    @GetMapping
    public String list(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(required = false) String q,
                       Model model) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").descending());
        Page<Blog> pageResult = blogService.searchPage(q, pageable);
        model.addAttribute("title", "Blog");
        model.addAttribute("content", "admin/blog-list");
        model.addAttribute("list", pageResult.getContent());
        model.addAttribute("page", pageResult);
        model.addAttribute("searchKeyword", q != null ? q : "");
        model.addAttribute("blog", new Blog());
        model.addAttribute("formAction", "/admin/blog");
        return "layout/admin-layout";
    }

    @GetMapping("/them")
    public String formCreate() {
        return "redirect:/admin/blog";
    }

    @GetMapping("/{id}/sua")
    public String formEdit(@PathVariable Integer id,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(required = false) String q,
                           Model model) {
        Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").descending());
        Page<Blog> pageResult = blogService.searchPage(q, pageable);
        Blog blog = blogService.findById(id);
        if (blog == null) {
            return "redirect:/admin/blog";
        }
        model.addAttribute("title", "Blog");
        model.addAttribute("content", "admin/blog-list");
        model.addAttribute("list", pageResult.getContent());
        model.addAttribute("page", pageResult);
        model.addAttribute("searchKeyword", q != null ? q : "");
        model.addAttribute("blog", blog);
        String formAction = "/admin/blog/" + id + "?page=" + page;
        if (q != null && !q.isBlank()) formAction += "&q=" + URLEncoder.encode(q, StandardCharsets.UTF_8);
        model.addAttribute("formAction", formAction);
        return "layout/admin-layout";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("blog") Blog blog, BindingResult result,
                         @RequestParam(value = "hinhAnhFile", required = false) MultipartFile hinhAnhFile,
                         @RequestParam(required = false) String q,
                         Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            Pageable pageable = PageRequest.of(0, PAGE_SIZE, Sort.by("id").descending());
            Page<Blog> pageResult = blogService.searchPage(q, pageable);
            model.addAttribute("title", "Blog");
            model.addAttribute("content", "admin/blog-list");
            model.addAttribute("list", pageResult.getContent());
            model.addAttribute("page", pageResult);
            model.addAttribute("searchKeyword", q != null ? q : "");
            model.addAttribute("formAction", "/admin/blog");
            return "layout/admin-layout";
        }
        if (hinhAnhFile != null && !hinhAnhFile.isEmpty()) {
            String path = fileUploadService.uploadFile(hinhAnhFile);
            blog.setHinhAnh(path);
        }
        blogService.save(blog);
        redirect.addFlashAttribute("message", "Thêm blog thành công.");
        if (q != null && !q.isBlank()) redirect.addAttribute("q", q);
        return "redirect:/admin/blog#listBlog";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Integer id, @RequestParam(defaultValue = "0") int page,
                         @RequestParam(required = false) String q,
                         @Valid @ModelAttribute("blog") Blog blog, BindingResult result,
                         @RequestParam(value = "hinhAnhFile", required = false) MultipartFile hinhAnhFile,
                         Model model, RedirectAttributes redirect) {
        if (result.hasErrors()) {
            Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("id").descending());
            Page<Blog> pageResult = blogService.searchPage(q, pageable);
            model.addAttribute("title", "Blog");
            model.addAttribute("content", "admin/blog-list");
            model.addAttribute("list", pageResult.getContent());
            model.addAttribute("page", pageResult);
            model.addAttribute("searchKeyword", q != null ? q : "");
            model.addAttribute("blog", blog);
            String formAction = "/admin/blog/" + id + "?page=" + page;
            if (q != null && !q.isBlank()) formAction += "&q=" + URLEncoder.encode(q, StandardCharsets.UTF_8);
            model.addAttribute("formAction", formAction);
            return "layout/admin-layout";
        }
        if (hinhAnhFile != null && !hinhAnhFile.isEmpty()) {
            String path = fileUploadService.uploadFile(hinhAnhFile);
            blog.setHinhAnh(path);
        } else {
            Blog existing = blogService.findById(id);
            if (existing != null) blog.setHinhAnh(existing.getHinhAnh());
        }
        blogService.update(id, blog);
        redirect.addFlashAttribute("message", "Cập nhật blog thành công.");
        redirect.addAttribute("page", page);
        if (q != null && !q.isBlank()) redirect.addAttribute("q", q);
        return "redirect:/admin/blog#listBlog";
    }

    @PostMapping("/{id}/xoa")
    public String delete(@PathVariable Integer id, @RequestParam(defaultValue = "0") int page,
                         @RequestParam(required = false) String q,
                         RedirectAttributes redirect) {
        blogService.delete(id);
        redirect.addFlashAttribute("message", "Xóa blog thành công.");
        redirect.addAttribute("page", page);
        if (q != null && !q.isBlank()) redirect.addAttribute("q", q);
        return "redirect:/admin/blog#listBlog";
    }
}
