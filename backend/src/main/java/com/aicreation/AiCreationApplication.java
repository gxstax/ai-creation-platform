package com.aicreation;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.aicreation.infrastructure.persistence.mapper")
public class AiCreationApplication {

  public static void main(String[] args) {
    SpringApplication.run(AiCreationApplication.class, args);
  }
}
