package com.example.watchaura.controller;

import com.example.watchaura.dto.SerialLoiDTO;
import com.example.watchaura.service.SerialLoiService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/serial-loi")
@RequiredArgsConstructor
public class SerialLoiApiController {

    private final SerialLoiService serialLoiService;

    @GetMapping
    public ResponseEntity<List<SerialLoiDTO>> getAll() {
        return ResponseEntity.ok(serialLoiService.getAllSerialLoi(PageRequest.of(0, 1000)).getContent());
    }

    @GetMapping("/paged")
    public ResponseEntity<Map<String, Object>> getPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String trangThai,
            @RequestParam(required = false) String keyword) {
        try {
            Page<SerialLoiDTO> result = serialLoiService.searchSerialLoi(trangThai, keyword,
                    PageRequest.of(page, size, Sort.by("ngayTao").descending()));

            Map<String, Object> response = new HashMap<>();
            response.put("content", result.getContent());
            response.put("totalElements", result.getTotalElements());
            response.put("totalPages", result.getTotalPages());
            response.put("currentPage", page);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<SerialLoiDTO> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(serialLoiService.getSerialLoiById(id));
    }

    @GetMapping("/ma/{maSerial}")
    public ResponseEntity<SerialLoiDTO> getByMaSerial(@PathVariable String maSerial) {
        return ResponseEntity.ok(serialLoiService.getSerialLoiByMaSerial(maSerial));
    }

    @GetMapping("/hoan-tra/{hoanTraId}")
    public ResponseEntity<List<SerialLoiDTO>> getByHoanTraId(@PathVariable Integer hoanTraId) {
        return ResponseEntity.ok(serialLoiService.getSerialLoiByHoanTraId(hoanTraId));
    }

    @GetMapping("/thong-ke")
    public ResponseEntity<Map<String, Long>> getThongKe() {
        return ResponseEntity.ok(serialLoiService.getThongKe());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody SerialLoiDTO dto, HttpSession session) {
        try {
            Integer nhanVienId = (Integer) session.getAttribute("nhanVienId");
            if (nhanVienId != null) {
                dto.setNguoiTaoTen(nhanVienId.toString());
            }
            SerialLoiDTO created = serialLoiService.createSerialLoi(dto);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", created);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/{id}/xu-ly")
    public ResponseEntity<?> xuLy(@PathVariable Integer id, HttpSession session) {
        try {
            Integer nhanVienId = (Integer) session.getAttribute("nhanVienId");
            if (nhanVienId == null) {
                nhanVienId = 1;
            }
            SerialLoiDTO updated = serialLoiService.xuLySerialLoi(id, nhanVienId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    @PutMapping("/{id}/huy")
    public ResponseEntity<?> huy(@PathVariable Integer id, HttpSession session) {
        try {
            Integer nhanVienId = (Integer) session.getAttribute("nhanVienId");
            if (nhanVienId == null) {
                nhanVienId = 1;
            }
            SerialLoiDTO updated = serialLoiService.huySerialLoi(id, nhanVienId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", updated);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }
}
