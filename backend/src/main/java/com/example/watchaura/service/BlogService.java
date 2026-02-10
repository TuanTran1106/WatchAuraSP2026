package com.example.watchaura.service;

import com.example.watchaura.entity.Blog;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BlogService {

    Page<Blog> findAll(int page, int size);

    Page<Blog> searchPage(String q, Pageable pageable);

    List<Blog> getRecentBlogs(int limit);

    Blog findById(Integer id);

    Blog save(Blog blog);

    Blog update(Integer id, Blog blog);

    void delete(Integer id);
}
