package com.pubsub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
@EnableScheduling
public class SalesforcePubSubApiSpringbootApplication {

    public static void main(String[] args) {
        SpringApplication.run(SalesforcePubSubApiSpringbootApplication.class, args);
    }

}
