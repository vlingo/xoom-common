package io.vlingo.common.message;

public interface SelfSendingMessage extends Message {
  void send() throws Exception;
}
