package com.print.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = {
        "com.print.security",
        "com.print.user",
        "com.print.shared",
        "com.print.api",
        "com.print.storage",
        "com.print.mail"
})
@EnableJpaRepositories(basePackages = {
        "com.print.user.repository",
        "com.print.security.repository"
}
)
@EntityScan(basePackages = {
        "com.print.user.entity",
        "com.print.security.entity"
})
@EnableAsync
public class PrintApplication {

    public static void main(String[] args) {
        SpringApplication.run(PrintApplication.class, args);
    }

}
