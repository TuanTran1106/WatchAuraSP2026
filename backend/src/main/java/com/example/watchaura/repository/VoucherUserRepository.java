package com.example.watchaura.repository;

import com.example.watchaura.entity.VoucherUser;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoucherUserRepository extends JpaRepository<VoucherUser, Integer> {

    Optional<VoucherUser> findByVoucherIdAndKhachHangId(Integer voucherId, Integer khachHangId);
}

