package com.rohit.kafka.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rohit.kafka.service.KafkaService;

@RestController
@RequestMapping("/kafka")
public class KafkaController {
    
    @Autowired
    private KafkaService service;

    @GetMapping("/get-course")
    public ResponseEntity<String> getCourse() {
        String response = service.getMessage();

        return new ResponseEntity<String>(response, HttpStatus.OK);
    }
}
