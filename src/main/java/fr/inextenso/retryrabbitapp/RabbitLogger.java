package fr.inextenso.retryrabbitapp;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
public class RabbitLogger {
  private final StreamBridge streamBridge;

  public RabbitLogger(StreamBridge streamBridge) {
    this.streamBridge = streamBridge;
  }

  public void log(Message<?> msg) {
    streamBridge.send("log", msg);
  }
}
