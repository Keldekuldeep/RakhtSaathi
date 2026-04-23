package com.rakhtsaathi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class RakhtSaathiApplication {
    public static void main(String[] args) {
        SpringApplication.run(RakhtSaathiApplication.class, args);
    }
}
