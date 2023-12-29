package com.budget.project.controller;

import com.budget.project.service.ImportService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequiredArgsConstructor
public class FileUploadController {
    private final ImportService importService;

    @PostMapping("/import")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) {
        importService.importCSV(file);
        return ResponseEntity.status(HttpStatus.OK).body("success");
    }
    @PostMapping("/onemoney")
    public ResponseEntity<String> importFromOneMoney(@RequestParam("file") MultipartFile file) {
        importService.importFromOneMoney(file);
        return ResponseEntity.status(HttpStatus.OK).body("success");
    }
}
