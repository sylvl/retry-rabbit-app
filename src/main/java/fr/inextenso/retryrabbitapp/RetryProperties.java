package fr.inextenso.retryrabbitapp;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "retry")
public class RetryProperties {

  private String routerExchangeName;

  private String inputExchangeName;

  private String parkingExchangeName;

  private String retryRoutingKey;

  private Boolean declareInputChannel;

  private String delayGroupName;

  private Map<String, DelayChannelProperties> delayChannels;

  public String getRouterExchangeName() {
    return routerExchangeName;
  }

  public void setRouterExchangeName(String routerExchangeName) {
    this.routerExchangeName = routerExchangeName;
  }

  public String getInputExchangeName() {
    return inputExchangeName;
  }

  public void setInputExchangeName(String inputExchangeName) {
    this.inputExchangeName = inputExchangeName;
  }

  public String getRetryRoutingKey() {
    return retryRoutingKey;
  }

  public void setRetryRoutingKey(String retryRoutingKey) {
    this.retryRoutingKey = retryRoutingKey;
  }

  public Boolean getDeclareInputChannel() {
    return declareInputChannel;
  }

  public void setDeclareInputChannel(Boolean declareInputChannel) {
    this.declareInputChannel = declareInputChannel;
  }

  public String getParkingExchangeName() {
    return parkingExchangeName;
  }

  public void setParkingExchangeName(String parkingExchangeName) {
    this.parkingExchangeName = parkingExchangeName;
  }

  public Map<String, DelayChannelProperties> getDelayChannels() {
    return delayChannels;
  }

  public void setDelayChannels(Map<String, DelayChannelProperties> delayChannels) {
    this.delayChannels = delayChannels;
  }

  public String getDelayGroupName() {
    return delayGroupName;
  }

  public void setDelayGroupName(String delayGroupName) {
    this.delayGroupName = delayGroupName;
  }

  static class DelayChannelProperties {
    /**
     * Backoff time in milliseconds (first retry)
     */
    private Integer waitingTime;
    private String destinationName;

    public Integer getWaitingTime() {
      return waitingTime;
    }

    public void setWaitingTime(Integer waitingTime) {
      this.waitingTime = waitingTime;
    }

    public String getDestinationName() {
      return destinationName;
    }

    public void setDestinationName(String destinationName) {
      this.destinationName = destinationName;
    }
  }
}


