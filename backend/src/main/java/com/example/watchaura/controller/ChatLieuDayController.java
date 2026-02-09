package com.example.watchaura.controller;

import com.example.watchaura.entity.ChatLieuDay;
import com.example.watchaura.repository.ChatLieuDayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/chat-lieu-day")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ChatLieuDayController {

    private final ChatLieuDayRepository chatLieuDayRepository;

    /**
     * GET: Lấy tất cả chất liệu dây
     */
    @GetMapping
    @ResponseBody
    public ResponseEntity<List<ChatLieuDay>> getAllChatLieuDay() {
        try {
            List<ChatLieuDay> chatLieuDays = chatLieuDayRepository.findAll();
            return ResponseEntity.ok(chatLieuDays);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * GET: Lấy chất liệu dây theo ID
     */
    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> getChatLieuDayById(@PathVariable Integer id) {
        try {
            return chatLieuDayRepository.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * POST: Tạo mới chất liệu dây
     */
    @PostMapping
    @ResponseBody
    public ResponseEntity<?> createChatLieuDay(@RequestBody ChatLieuDay chatLieuDay) {
        try {
            ChatLieuDay saved = chatLieuDayRepository.save(chatLieuDay);
            return ResponseEntity.status(HttpStatus.CREATED).body(saved);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * PUT: Cập nhật chất liệu dây
     */
    @PutMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> updateChatLieuDay(@PathVariable Integer id, @RequestBody ChatLieuDay chatLieuDay) {
        try {
            return chatLieuDayRepository.findById(id)
                    .map(existing -> {
                        chatLieuDay.setId(id);
                        return ResponseEntity.ok(chatLieuDayRepository.save(chatLieuDay));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * DELETE: Xóa chất liệu dây
     */
    @DeleteMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> deleteChatLieuDay(@PathVariable Integer id) {
        try {
            if (chatLieuDayRepository.existsById(id)) {
                chatLieuDayRepository.deleteById(id);
                return ResponseEntity.ok("Xóa thành công");
            }
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
