package com.example.imageprocessing.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.imageprocessing.services.ImageService;

@RestController
public class ImageController {

  @Autowired
  ImageService imageServiceImpl;

  @PostMapping("/images")
  public ResponseEntity<?> uploadImage(@RequestParam("image") MultipartFile file) throws IOException {
    String uploadImage = imageServiceImpl.uploadImage(file);
        return ResponseEntity.status(HttpStatus.OK).body(uploadImage);
  }
}
