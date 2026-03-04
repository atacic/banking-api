package com.aleksa.banking_api.service.impl.notification;

import com.aleksa.banking_api.config.RabbitMQConfig;
import com.aleksa.banking_api.dto.event.NotificationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationConsumer {

    @RabbitListener(queues = RabbitMQConfig.QUEUE_EMAIL)
    public void consumeEmailNotification(NotificationEvent event) {
        log.info("Received notification from RabbitMQ: {}", event);
        // Simulate sending email
        log.info("Simulating SENDING EMAIL | Subject: [{}] | Recipient: [{}] | Content: [{}]",
                event.subject(), event.recipientEmail(), event.message());
    }
}
