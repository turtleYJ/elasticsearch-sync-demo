package com.y1.elsdemo.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.y1.elsdemo.config.RabbitMQConfig;
import com.y1.elsdemo.model.MailDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailMqListener {

    private final ElasticService elasticService;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitMQConfig.MAIL_QUEUE)
    public void receiveMailMessage(MailDocument mail) {
        try {
            log.info("MQ에서 메시지 수신: {}", mail.getSubject());
            elasticService.save(mail);
            log.info("MQ 메시지 처리 완료: {}", mail.getId());

        } catch (Exception e) {
            log.error("MQ 메시지 처리 실패: {}", e.getMessage(), e);
            // 실제 운영환경에서는 Dead Letter Queue나 재시도 로직을 구현해야 함
        }
    }
}
