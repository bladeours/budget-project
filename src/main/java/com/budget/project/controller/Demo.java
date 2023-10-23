package com.budget.project.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.budget.project.exception.model.ServerExceptionResponse;

@RestController
@RequestMapping("/api/demo")
public class Demo {
    @GetMapping
    public ServerExceptionResponse demo() {
        return new ServerExceptionResponse(200, HttpStatus.OK, "it works!");
    }
}
