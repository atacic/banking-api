package com.aleksa.banking_api.service.impl.notification;

import com.aleksa.banking_api.config.RabbitMQConfig;
import com.aleksa.banking_api.dto.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationProducer {

    private final RabbitTemplate rabbitTemplate;

    public void sendEmailNotification(NotificationEvent event) {
        log.info("Sending notification event to RabbitMQ: {}", event);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NOTIFICATIONS, RabbitMQConfig.ROUTING_KEY_EMAIL, event);
    }
}
