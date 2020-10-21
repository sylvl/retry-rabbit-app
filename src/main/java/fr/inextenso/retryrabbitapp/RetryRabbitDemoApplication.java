package fr.inextenso.retryrabbitapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(RetryProperties.class)
@SpringBootApplication
public class RetryRabbitDemoApplication {

  public static void main(String[] args) {
    SpringApplication.run(RetryRabbitDemoApplication.class, args);
  }

}
