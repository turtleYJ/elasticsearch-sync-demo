package com.y1.elsdemo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/mail")
    public String hello() {
        return "aaa";
    }
}
