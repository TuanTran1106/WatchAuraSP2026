package com.example.watchaura.service.impl;

import com.example.watchaura.entity.Blog;
import com.example.watchaura.repository.BlogRepository;
import com.example.watchaura.service.BlogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Service
public class BlogServiceImpl implements BlogService {

    @Autowired
    private BlogRepository blogRepository;

    @Override
    public Page<Blog> findAll(int page, int size) {
        return blogRepository.findAll(
                PageRequest.of(page, size, Sort.by("ngayDang").descending())
        );
    }

    @Override
    public Blog findById(Integer id) {
        return blogRepository.findById(id).orElse(null);
    }

    @Override
    public Blog save(Blog blog) {
        blog.setNgayDang(LocalDateTime.now());
        return blogRepository.save(blog);
    }

    @Override
    public Blog update(Integer id, Blog blog) {
        Blog existing = blogRepository.findById(id).orElse(null);
        if (existing == null) {
            return null;
        }

        existing.setTieuDe(blog.getTieuDe());
        existing.setNoiDung(blog.getNoiDung());
        existing.setHinhAnh(blog.getHinhAnh());

        return blogRepository.save(existing);
    }

    @Override
    public void delete(Integer id) {
        blogRepository.deleteById(id);
    }
}
