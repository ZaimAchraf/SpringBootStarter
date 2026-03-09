package com.tottrust.starter.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = {
        "com.tottrust.starter.security",
        "com.tottrust.starter.user",
        "com.tottrust.starter.shared",
        "com.tottrust.starter.api",
        "com.tottrust.starter.storage",
        "com.tottrust.starter.mail"
})
@EnableJpaRepositories(basePackages = {
        "com.tottrust.starter.user.repository",
        "com.tottrust.starter.security.repository"
}
)
@EntityScan(basePackages = {
        "com.tottrust.starter.user.entity",
        "com.tottrust.starter.security.entity"
})
@EnableAsync
public class StarterApplication {

    public static void main(String[] args) {
        SpringApplication.run(StarterApplication.class, args);
    }

}
