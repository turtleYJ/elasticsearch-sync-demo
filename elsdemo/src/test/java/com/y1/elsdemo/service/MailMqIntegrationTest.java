package com.y1.elsdemo.service;

import com.y1.elsdemo.model.MailDocument;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class MailMqIntegrationTest {

    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    ElasticService elasticService;

    static final String QUEUE_NAME = "mail-queue";

    @Test
    void testMailPublishAndConsume() throws Exception {
        // given
        MailDocument mail = new MailDocument(
                "mq-test-001",
                "RabbitMQ 제목",
                "sender@sample.com",
                "receiver@sample.com",
                "본문 컨텐츠",
                "inbox",
                Instant.now()
        );

        // when: mq publish
        rabbitTemplate.convertAndSend(QUEUE_NAME, mail);

        // then: 비동기라서 잠깐 대기 (리스너가 ES에 저장할 때까지)
        Thread.sleep(1000);

        MailDocument found = elasticService.findById("mq-test-001");
        assertThat(found).isNotNull();
        assertThat(found.getSubject()).isEqualTo("RabbitMQ 제목");

    }


}
