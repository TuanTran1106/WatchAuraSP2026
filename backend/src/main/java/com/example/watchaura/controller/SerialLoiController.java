package com.example.watchaura.controller;

import com.example.watchaura.dto.SerialLoiDTO;
import com.example.watchaura.service.SerialLoiService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/serial-loi")
@RequiredArgsConstructor
public class SerialLoiController {

    private final SerialLoiService serialLoiService;

    @GetMapping
    public String listSerialLoi(Model model,
                                @RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size) {
        Page<SerialLoiDTO> serialLoiPage = serialLoiService.getAllSerialLoi(
                PageRequest.of(page, size, Sort.by("ngayTao").descending()));

        model.addAttribute("serialLoiPage", serialLoiPage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", serialLoiPage.getTotalPages());

        return "admin/serial-loi";
    }
}
