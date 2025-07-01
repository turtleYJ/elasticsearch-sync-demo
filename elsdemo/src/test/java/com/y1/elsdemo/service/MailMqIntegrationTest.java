package com.y1.elsdemo.service;

import com.y1.elsdemo.model.MailDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class MailMqIntegrationTest {

    @Autowired
    RabbitTemplate rabbitTemplate;
    
    @Autowired
    ElasticService elasticService;

    static final String QUEUE_NAME = "mail-queue";

    @BeforeEach
    void cleanup() {
        // 테스트 전에 기존 데이터 정리
        try {
            elasticService.deleteById("mq-test-001");
            elasticService.deleteById("mq-test-002");
        } catch (Exception e) {
            // 데이터가 없어도 무시
        }
    }

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

        // when: MQ를 통해 메일 전송 (직렬화된 객체로 전송)
        rabbitTemplate.convertAndSend(QUEUE_NAME, mail);

        // then: 비동기 처리 대기 (리스너가 ES에 저장할 때까지)
        Thread.sleep(2000);

        // 저장된 메일 확인
        MailDocument found = elasticService.findById("mq-test-001");
        assertThat(found).isNotNull();
        assertThat(found.getSubject()).isEqualTo("RabbitMQ 제목");
        assertThat(found.getSender()).isEqualTo("sender@sample.com");
        assertThat(found.getReceiver()).isEqualTo("receiver@sample.com");
        assertThat(found.getContent()).isEqualTo("본문 컨텐츠");
    }

    @Test
    void testMqMailPublishAndSearch() throws Exception {
        // given
        MailDocument mail = new MailDocument(
                "mq-test-002",
                "검색 테스트 메일",
                "search@test.com",
                "user@test.com",
                "이 메일은 검색 기능 테스트를 위한 메일입니다. 키워드: 테스트, 검색, 기능",
                "inbox",
                Instant.now()
        );

        // when: MQ를 통해 메일 전송
        rabbitTemplate.convertAndSend(QUEUE_NAME, mail);

        // then: 비동기 처리 대기
        Thread.sleep(2000);

        // 검색 기능 테스트
        List<MailDocument> searchResults = elasticService.search("테스트");
        assertThat(searchResults).isNotEmpty();
        assertThat(searchResults.stream().anyMatch(m -> m.getId().equals("mq-test-002"))).isTrue();

        List<MailDocument> keywordResults = elasticService.search("키워드");
        assertThat(keywordResults).isNotEmpty();
        assertThat(keywordResults.stream().anyMatch(m -> m.getId().equals("mq-test-002"))).isTrue();
    }
}
