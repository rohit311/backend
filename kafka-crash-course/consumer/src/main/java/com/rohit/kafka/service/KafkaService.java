package com.rohit.kafka.service;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.rohit.kafka.model.Course;

@Service
public class KafkaService {

    private String message;

    @KafkaListener(topics = "telusko", groupId = "telusko-group")
    public void consume(Course course) {
       message = course + "Got the data from kafka topic";

       System.out.println("Received ::: " + message);
    }

    public String getMessage() {
        return message;
    }
}
