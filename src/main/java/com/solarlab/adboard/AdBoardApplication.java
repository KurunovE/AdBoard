package com.solarlab.adboard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class AdBoardApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdBoardApplication.class, args);
    }

}
