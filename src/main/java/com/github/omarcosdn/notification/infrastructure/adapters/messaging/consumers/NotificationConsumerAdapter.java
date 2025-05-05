package com.github.omarcosdn.notification.infrastructure.adapters.messaging.consumers;

import com.github.omarcosdn.notification.infrastructure.config.RabbitConfig;
import com.github.omarcosdn.notification.infrastructure.web.socket.WebSocketServer;
import java.io.IOException;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumerAdapter {

  private final WebSocketServer webSocketServer;

  public NotificationConsumerAdapter(final WebSocketServer webSocketServer) {
    this.webSocketServer = webSocketServer;
  }

  @RabbitListener(queues = RabbitConfig.NOTIFICATION_V1_QUEUE)
  public void onMessage(final String message) throws IOException {
    webSocketServer.broadcast(message);
  }
}
