package com.ice.musicdistribution;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MusicDistributionApplication {

    public static void main(String[] args) {
        SpringApplication.run(MusicDistributionApplication.class, args);
    }
}