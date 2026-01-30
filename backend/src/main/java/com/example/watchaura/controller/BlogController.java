package com.example.watchaura.controller;

import com.example.watchaura.entity.Blog;
import com.example.watchaura.service.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blog")
public class BlogController {

    @Autowired
    private BlogService blogService;

    @GetMapping
    public List<Blog> getAll() {
        return blogService.getAll();
    }

    @GetMapping("/{id}")
    public Blog getById(@PathVariable Integer id) {
        return blogService.getById(id);
    }

    @PostMapping
    public Blog create(@RequestBody Blog blog) {
        return blogService.create(blog);
    }

    @PutMapping("/{id}")
    public Blog update(
            @PathVariable Integer id,
            @RequestBody Blog blog) {
        return blogService.update(id, blog);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        blogService.delete(id);
    }
}

