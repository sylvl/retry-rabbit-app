package fr.inextenso.retryrabbitapp;

import com.rabbitmq.client.ShutdownSignalException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitControl {
  private final AmqpAdmin amqpAdmin;
  private final RetryProperties retryProperties;
  private final String parkingGroupName;
  private final String successDestinationName;
  private final String successGroupName;
  private final String inputGroupName;
  private final String logDestinationName;
  private final String logGroupName;

  public RabbitControl(AmqpAdmin amqpAdmin, RetryProperties retryProperties,
                       @Value("${spring.cloud.stream.bindings.process-out-0.destination}") String successDestinationName,
                       @Value("${spring.cloud.stream.bindings.parking.producer.required-groups}") String parkingGroupName,
                       @Value("${spring.cloud.stream.bindings.process-out-0.producer.required-groups}") String successGroupName,
                       @Value("${spring.cloud.stream.bindings.process-in-0.group}") String inputGroupName,
                       @Value("${spring.cloud.stream.bindings.log.producer.required-groups}") String logGroupName,
                       @Value("${spring.cloud.stream.bindings.log.destination}") String logDestinationName
  ) {
    this.amqpAdmin = amqpAdmin;
    this.retryProperties = retryProperties;
    this.successDestinationName = successDestinationName;
    this.parkingGroupName = parkingGroupName;
    this.successGroupName = successGroupName;
    this.inputGroupName = inputGroupName;
    this.logDestinationName = logDestinationName;
    this.logGroupName = logGroupName;
  }

  public void cleanUp() {
    amqpAdmin.deleteExchange(logDestinationName);
    amqpAdmin.deleteExchange(retryProperties.getInputExchangeName());
    amqpAdmin.deleteExchange(retryProperties.getRouterExchangeName());
    amqpAdmin.deleteExchange(retryProperties.getParkingExchangeName());
    retryProperties.getDelayChannels().forEach((delayChannelName, delayChannelProperties) -> {
      amqpAdmin.deleteQueue(delayChannelProperties.getDestinationName() + "." + retryProperties.getDelayGroupName());
      amqpAdmin.deleteExchange(delayChannelProperties.getDestinationName());
    });
    amqpAdmin.deleteQueue(retryProperties.getParkingExchangeName() + "." + parkingGroupName);
    amqpAdmin.deleteQueue(retryProperties.getInputExchangeName() + "." + inputGroupName);
    amqpAdmin.deleteExchange(successDestinationName);
    try {
      amqpAdmin.deleteQueue(successDestinationName + "." + successGroupName);
    } catch (ShutdownSignalException ex) {
      // do nothing
    }
    amqpAdmin.deleteQueue(logDestinationName + "." + logGroupName);
  }
}
