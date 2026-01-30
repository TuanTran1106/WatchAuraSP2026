package com.example.watchaura.service;

import com.example.watchaura.entity.Blog;

import java.util.List;

public interface BlogService {

    List<Blog> getAll();

    Blog getById(Integer id);

    Blog create(Blog blog);

    Blog update(Integer id, Blog blog);

    void delete(Integer id);
}
