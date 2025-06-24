package com.y1.elsdemo.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String MAIL_QUEUE = "mail-queue";

    @Bean
    public Queue mailQueue() {
        return new Queue(MAIL_QUEUE, true);
    }
}
