package com.y1.elsdemo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.y1.elsdemo.config.RabbitMQConfig;
import com.y1.elsdemo.model.MailDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailMqListener {

    private final ElasticService elasticService;
    private final ObjectMapper objectMapper; // JSON 역직렬화

    @RabbitListener(queues = RabbitMQConfig.MAIL_QUEUE)
    public void receiveMailMessage(String message) {
        try {
            MailDocument mail = objectMapper.readValue(message, MailDocument.class);
            elasticService.save(mail);

        } catch (Exception e) {
            // 로깅 또는 예외 처리
            e.printStackTrace();
        }
    }
}
