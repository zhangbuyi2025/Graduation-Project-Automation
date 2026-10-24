package com.example.edu_digization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;

//启动类排除自动配置
@SpringBootApplication(exclude = {UserDetailsServiceAutoConfiguration.class})
public class EduDigizationApplication {

    public static void main(String[] args) {
        SpringApplication.run(EduDigizationApplication.class, args);
    }

}
