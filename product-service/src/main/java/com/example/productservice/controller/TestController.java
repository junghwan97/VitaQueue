package com.example.productservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class TestController {
    private List<Integer> result = new ArrayList<>();

    @GetMapping("/test")
    public String test() {
        return "test";
    }

    @GetMapping("/test/increase")
    public String test2() {
        while (true){
            result.add(1);

            if(false){
                break;
            }
        }

        return "test";
    }

    @GetMapping("/test/clear")
    public String test3() {
        result = new ArrayList<>();
        return "test";
    }
}
