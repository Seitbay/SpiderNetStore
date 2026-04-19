package ru.SeitbayBulat.SpiderNetStore.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PageController {

    @GetMapping("/")
    public String index() {
        // Имя шаблона не "index": иначе Spring Boot регистрирует WelcomePageHandlerMapping
        // на шаблон index и может перехватывать "/" с ответом 404.
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }
    @GetMapping("profile")
    public String profile() {
        return "profile";
    }
    @GetMapping("/product/{id}")
    public String product() { return "product"; }

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("query", q != null ? q : "");
        return "search";
    }
    @GetMapping({"/admin", "/api/admin"})
    public String admin() {
        return "admin";
    }
}