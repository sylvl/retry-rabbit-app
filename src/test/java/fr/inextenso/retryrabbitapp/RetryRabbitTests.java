package fr.inextenso.retryrabbitapp;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.QueueInformation;
import org.springframework.amqp.rabbit.junit.RabbitAvailable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.util.AssertionErrors.assertNull;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@RabbitAvailable
@SpringBootTest(properties = {"retry.delay-channels.delay1.waiting-time=10", "retry.delay-channels.delay2.waiting-time=100",
    "spring.autoconfigure.exclude=org.springframework.cloud.stream.test.binder.TestSupportBinderAutoConfiguration"},
    classes = {RetryRabbitDemoApplication.class})
public class RetryRabbitTests {
  @Autowired
  private AmqpTemplate amqpTemplate;

  @Autowired
  private AmqpAdmin amqpAdmin;

  @Autowired
  private RabbitControl rabbitControl;

  @Test
  void noRetryWithRabbit() throws InterruptedException {
    final String payload = "test";
    amqpTemplate.convertAndSend("example", "any", payload);
    Thread.sleep(500); // wait for retries
    final QueueInformation queueInfo = amqpAdmin.getQueueInfo("parking.default");
    if (queueInfo != null) {

      Message messageParking = amqpTemplate.receive("parking.default");
      assertNull("parking queue should be empty", messageParking);
    }

    Message messageSuccess = amqpTemplate.receive("success.default");
    assertThat(messageSuccess.getBody(), equalTo(("done " + payload).getBytes()));

  }

  @Test
  void retryWithRabbit() throws InterruptedException {
    amqpTemplate.convertAndSend("example", "any", "some payload with retry");
    Thread.sleep(500); // wait for retries

    final QueueInformation queueInfo = amqpAdmin.getQueueInfo("success.default");
    if (queueInfo != null) {
      Message messageSuccess = amqpTemplate.receive("success.default");
      assertNull("success queue should be empty", messageSuccess);
    }
    Message messageParking = amqpTemplate.receive("parking.default");
    // Should get 1 message with :
    // retry num = 2
    // retry0 = delay1
    // retry1 = delay2
    assertThat(messageParking.getMessageProperties().getHeader("retry-num"), equalTo(2));
    assertThat(messageParking.getMessageProperties().getHeader("retry0"), equalTo("delay1"));
    assertThat(messageParking.getMessageProperties().getHeader("retry1"), equalTo("delay2"));
    assertThat(messageParking.getMessageProperties().getHeader("retry-destination"), equalTo("example"));
  }

  @AfterAll
  public void afterAll() {
    rabbitControl.cleanUp();
  }
}
