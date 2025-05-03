package com.example.imageprocessing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.example.imageprocessing"})
public class ImageprocessingApplication {

	public static void main(String[] args) {
		SpringApplication.run(ImageprocessingApplication.class, args);
	}

}
