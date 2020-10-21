package fr.inextenso.retryrabbitapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import static org.assertj.core.api.Assertions.assertThat;

class RetryNoRabbiTests {

  @Test
  void noRetry() {

    try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
        TestChannelBinderConfiguration.getCompleteConfiguration(
            RetryRabbitDemoApplication.class))
        .run("--spring.cloud.function.definition=process")) {
      InputDestination source = context.getBean(InputDestination.class);
      OutputDestination target = context.getBean(OutputDestination.class);
      final String payload = "some stuff";
      source.send(new GenericMessage<>(payload.getBytes()));
      Message<byte[]> received = target.receive(1000, "success");
      assertThat(received.getPayload()).isEqualTo(("done " + payload).getBytes());
    }

  }

  @Test
  void retries() {

    try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
        TestChannelBinderConfiguration.getCompleteConfiguration(
            RetryRabbitDemoApplication.class))
        .run("--spring.cloud.function.definition=process")) {
      InputDestination source = context.getBean(InputDestination.class);
      OutputDestination target = context.getBean(OutputDestination.class);
      final String payload = "some stuff retry";
      source.send(new GenericMessage<>(payload.getBytes()));
      Message<byte[]> received1 = target.receive(1000, "delay1");
      assertThat(received1.getPayload()).isEqualTo(payload.getBytes());
      assertThat(received1.getHeaders().get(ExampleProcessor.RETRY_NUM_HEADER)).isEqualTo(1);
      source.send(received1);
      Message<byte[]> received2 = target.receive(1000, "delay2");
      assertThat(received2.getHeaders().get(ExampleProcessor.RETRY_NUM_HEADER)).isEqualTo(2);
      assertThat(received1.getPayload()).isEqualTo(payload.getBytes());
      source.send(received2);
      Message<byte[]> received3 = target.receive(1000, "parking");
      assertThat(received3.getHeaders().get(ExampleProcessor.RETRY_NUM_HEADER)).isEqualTo(2);
      assertThat(received3.getPayload()).isEqualTo(payload.getBytes());

    }
  }
}
