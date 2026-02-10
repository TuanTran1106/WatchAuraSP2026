package com.example.watchaura.repository;

import com.example.watchaura.entity.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Integer> {

    List<Blog> findAllByOrderByNgayDangDesc(Pageable pageable);

    @Query("SELECT b FROM Blog b WHERE :q IS NULL OR :q = '' OR LOWER(b.tieuDe) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(b.noiDung) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<Blog> searchByKeyword(@Param("q") String q, Pageable pageable);
}
