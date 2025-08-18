package com.example.mcpdemo;

import java.util.List;

import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class McpdemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(McpdemoApplication.class, args);
	}

  @Bean
  public List<ToolCallback> danTools(CourseService courseService) {
    return List.of(ToolCallbacks.from(courseService));
  }

}
