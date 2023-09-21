package com.budget.project.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/demo")
public class Controller {

    @GetMapping
    public ResponseEntity<String> helloUser() {
        return ResponseEntity.ok("hello!");
    }

}

