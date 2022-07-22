package com.ljf;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(value = "com.ljf")
public class LjfMallApplication {

    public static void main(String[] args) {
        SpringApplication.run(LjfMallApplication.class, args);
    }

}
