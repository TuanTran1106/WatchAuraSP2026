package com.example.watchaura.service.impl;

import com.example.watchaura.entity.Blog;
import com.example.watchaura.repository.BlogRepository;
import com.example.watchaura.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BlogServiceImpl implements BlogService {

    private final BlogRepository blogRepository;

    @Override
    public List<Blog> getAll() {
        return blogRepository.findAll();
    }

    @Override
    public List<Blog> getRecentBlogs(int limit) {
        return blogRepository.findAllByOrderByNgayDangDesc(PageRequest.of(0, limit));
    }

    @Override
    public Page<Blog> getPage(Pageable pageable) {
        return blogRepository.findAll(pageable);
    }

    @Override
    public Blog getById(Integer id) {
        return blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy blog ID: " + id));
    }

    @Override
    @Transactional
    public Blog create(Blog blog) {
        if (blog.getNgayDang() == null) {
            blog.setNgayDang(LocalDateTime.now());
        }
        return blogRepository.save(blog);
    }

    @Override
    @Transactional
    public Blog update(Integer id, Blog blog) {

        Blog existing = getById(id);

        existing.setTieuDe(blog.getTieuDe());
        existing.setNoiDung(blog.getNoiDung());
        existing.setHinhAnh(blog.getHinhAnh());
        existing.setNgayDang(LocalDateTime.now());

        return blogRepository.save(existing);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        if (!blogRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy blog để xóa");
        }
        blogRepository.deleteById(id);
    }
}
