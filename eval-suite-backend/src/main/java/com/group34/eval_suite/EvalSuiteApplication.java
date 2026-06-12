package com.group34.eval_suite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableAsync
public class EvalSuiteApplication {

  public static void main(String[] args) {
    SpringApplication.run(EvalSuiteApplication.class, args);
  }
}
