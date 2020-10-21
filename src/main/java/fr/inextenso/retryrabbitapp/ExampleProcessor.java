package fr.inextenso.retryrabbitapp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.function.Function;

@Configuration
public class ExampleProcessor {

  public static final String RETRY_NUM_HEADER = "retry-num";

  private static final Logger LOGGER = LoggerFactory.getLogger(ExampleProcessor.class);

  private final RabbitLogger rabbitLogger;

  public ExampleProcessor(RabbitLogger rabbitLogger) {
    this.rabbitLogger = rabbitLogger;
  }

  @Bean
  public Function<Message<String>, Message<String>> process() {
    return (Message<String> m) -> {
      LOGGER.info("processing payload : " + m.getPayload());
      if (m.getPayload().contains("retry")) {
        Integer retryNum = (Integer) m.getHeaders().getOrDefault(RETRY_NUM_HEADER, 0);
        if (retryNum == 0) {
          LOGGER.info("retry 1");

          return MessageBuilder.fromMessage(m)
              .setHeader(RETRY_NUM_HEADER, retryNum + 1)
              .setHeader("spring.cloud.stream.sendto.destination", "delay1")
              .setHeader("retry-destination", "example")
              .setHeader("retry" + retryNum, "delay1")
              .build();
        } else if (retryNum == 1) {
          LOGGER.info("retry 2");
          return MessageBuilder.fromMessage(m)
              .setHeader("spring.cloud.stream.sendto.destination", "delay2")
              .setHeader("retry-destination", "example")
              .setHeader(RETRY_NUM_HEADER, retryNum + 1)
              .setHeader("retry" + retryNum, "delay2").build();
        } else {
          LOGGER.info("reached maximum retries, giving up");
          rabbitLogger.log(MessageBuilder.fromMessage(m).setHeader("ERROR", "too many retries").build());
          return MessageBuilder
              .fromMessage(m).setHeader("spring.cloud.stream.sendto.destination", "parking").build();
        }
      }
      LOGGER.info("success");
      return MessageBuilder
          .withPayload("done " + m.getPayload()).setHeader("spring.cloud.stream.sendto.destination", "success").build();
    };
  }

}