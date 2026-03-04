package com.aleksa.banking_api.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NOTIFICATIONS = "x.notifications";
    public static final String QUEUE_EMAIL = "q.email-notifications";
    public static final String ROUTING_KEY_EMAIL = "notifications.email";

    @Bean
    public DirectExchange notificationsExchange() {
        return new DirectExchange(EXCHANGE_NOTIFICATIONS);
    }

    @Bean
    public Queue emailQueue() {
        return new Queue(QUEUE_EMAIL);
    }

    @Bean
    public Binding emailBinding(Queue emailQueue, DirectExchange notificationsExchange) {
        return BindingBuilder.bind(emailQueue).to(notificationsExchange).with(ROUTING_KEY_EMAIL);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
