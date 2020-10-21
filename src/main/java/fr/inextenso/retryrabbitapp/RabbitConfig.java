package fr.inextenso.retryrabbitapp;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class RabbitConfig {

  public static final String RETRY_DESTINATION_HEADER = "retry-destination";
  private final RetryProperties retryProperties;

  public RabbitConfig(RetryProperties retryProperties) {
    this.retryProperties = retryProperties;
  }

  @Bean
  public HeadersExchange routerExchange() {
    return ExchangeBuilder.headersExchange(retryProperties.getRouterExchangeName())
        .durable(true)
        .build();
  }

  @Bean
  public Exchange inputExchange() {
    return ExchangeBuilder.topicExchange(retryProperties.getInputExchangeName())
        .build();
  }

  @Bean
  public Declarables delayDeclarables() {
    List<Declarable> declarables = new ArrayList<>(retryProperties.getDelayChannels().size() * 3);
    retryProperties.getDelayChannels().forEach((key, value) -> {
      TopicExchange exchange = ExchangeBuilder.topicExchange(value.getDestinationName()).durable(true).build();
      declarables.add(exchange);
      Queue queue = QueueBuilder
          .durable(value.getDestinationName() + "." + retryProperties.getDelayGroupName())
          .ttl(value.getWaitingTime())
          .deadLetterExchange(retryProperties.getRouterExchangeName())
          .build();
      declarables.add(queue);
      declarables.add(BindingBuilder
          .bind(queue)
          .to(exchange)
          .with("#"));
    });
    return new Declarables(declarables);
  }

  @Bean
  public Binding routerBinding() {
    return BindingBuilder
        .bind(inputExchange())
        .to(routerExchange())
        .where(RETRY_DESTINATION_HEADER).matches(retryProperties.getInputExchangeName());
  }

}
