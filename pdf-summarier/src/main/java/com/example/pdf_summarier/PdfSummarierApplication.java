package com.example.pdf_summarier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.example.pdf_summarier.config.AppProperties;

@SpringBootApplication
@EnableConfigurationProperties(AppProperties.class)
public class PdfSummarierApplication {

	public static void main(String[] args) {
		SpringApplication.run(PdfSummarierApplication.class, args);
	}

}
