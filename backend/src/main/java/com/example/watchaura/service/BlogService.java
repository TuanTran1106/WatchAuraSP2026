package com.example.watchaura.service;

import com.example.watchaura.entity.Blog;

import java.util.List;

public interface BlogService {

    List<Blog> findAll();

    Blog findById(Integer id);

    Blog save(Blog blog);

    Blog update(Integer id, Blog blog);

    void delete(Integer id);
}
