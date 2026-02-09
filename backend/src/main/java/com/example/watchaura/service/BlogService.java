package com.example.watchaura.service;

import com.example.watchaura.entity.Blog;

import java.util.List;

import org.springframework.data.domain.Page;

public interface BlogService {

    Page<Blog> findAll(int page, int size);


    Blog findById(Integer id);

    Blog save(Blog blog);

    Blog update(Integer id, Blog blog);

    void delete(Integer id);
}
