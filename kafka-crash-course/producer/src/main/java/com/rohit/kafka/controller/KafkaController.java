package com.rohit.kafka.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rohit.kafka.model.Course;
import com.rohit.kafka.service.KafkaService;

@RestController
@RequestMapping("/kafka")
public class KafkaController {
    
    @Autowired
    private KafkaService service;

    @PostMapping("/add-course")
    public ResponseEntity<String> addCourse(@RequestBody Course course) {
        String response = service.sendMessage(course);

        return new ResponseEntity<String>(response, HttpStatus.OK);
    }
}
