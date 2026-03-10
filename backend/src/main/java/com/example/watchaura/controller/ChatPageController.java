package com.example.watchaura.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatPageController {

    @GetMapping("/tu-van-ai")
    public String tuVanAIPage() {
        return "index";
    }
}

