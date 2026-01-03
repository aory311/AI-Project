package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * ホームコントローラー
 */
@Controller
public class HomeController {
    
    /**
     * ルートパス: 入力画面にリダイレクト
     */
    @GetMapping("/")
    public String index() {
        return "redirect:/simulation/input";
    }
}

