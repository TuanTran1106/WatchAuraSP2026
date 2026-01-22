package com.example.watchaura.repository;

import com.example.watchaura.entity.ChatLieuDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatLieuDayRepository extends JpaRepository<ChatLieuDay, Integer> {
}

