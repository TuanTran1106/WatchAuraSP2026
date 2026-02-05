package com.example.watchaura.service;

import com.example.watchaura.entity.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BlogService {

    List<Blog> getAll();

    Page<Blog> getPage(Pageable pageable);

    Blog getById(Integer id);

    Blog create(Blog blog);

    Blog update(Integer id, Blog blog);

    void delete(Integer id);
}
