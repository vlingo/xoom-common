package io.vlingo.xoom.common.message;

public interface SelfSendingMessage extends Message {
  void send() throws Exception;
}
