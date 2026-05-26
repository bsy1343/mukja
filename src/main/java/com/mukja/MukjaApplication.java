// MukjaApplication.java — Spring Boot 애플리케이션 진입점
package com.mukja;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MukjaApplication {
    // 애플리케이션 부팅
    public static void main(String[] args) {
        SpringApplication.run(MukjaApplication.class, args);
    }
}
