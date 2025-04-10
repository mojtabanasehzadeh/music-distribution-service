package com.ice.musicdistribution.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI musicDistributionOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Music Distribution Service API")
                        .description("API for managing music releases, streams, and artist payment reports")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("ICE Music Distribution")
                                .url("https://ice-music-distribution.example.com")
                                .email("contact@ice-music-distribution.example.com"))
                        .license(new License()
                                .name("Internal Use Only")
                                .url("https://ice-music-distribution.example.com/license")));
    }
}