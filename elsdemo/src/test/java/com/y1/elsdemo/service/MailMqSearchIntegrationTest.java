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
public class MailMqSearchIntegrationTest {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Autowired
    private ElasticService elasticService;

    private static final String QUEUE_NAME = "mail-queue";

    @BeforeEach
    void cleanup() {
        // 테스트 전에 기존 데이터 정리 (필요시)
        try {
            elasticService.deleteById("mq-search-test-001");
            elasticService.deleteById("mq-search-test-002");
            elasticService.deleteById("mq-search-test-003");
        } catch (Exception e) {
            // 데이터가 없어도 무시
        }
    }

    @Test
    void testMailPublishViaMqAndVerifyStorage() throws Exception {
        // given: MQ를 통해 전송할 메일 데이터
        MailDocument mail = new MailDocument(
                "mq-search-test-001",
                "MQ 테스트 메일 제목",
                "sender@test.com",
                "receiver@test.com",
                "MQ를 통한 메일 저장 테스트 본문입니다.",
                "inbox",
                Instant.now()
        );

        // when: MQ를 통해 메일 전송
        rabbitTemplate.convertAndSend(QUEUE_NAME, mail);

        // then: 비동기 처리 대기 (리스너가 ES에 저장할 때까지)
        Thread.sleep(2000);

        // 저장된 메일 확인
        MailDocument savedMail = elasticService.findById("mq-search-test-001");
        assertThat(savedMail).isNotNull();
        assertThat(savedMail.getSubject()).isEqualTo("MQ 테스트 메일 제목");
        assertThat(savedMail.getSender()).isEqualTo("sender@test.com");
        assertThat(savedMail.getReceiver()).isEqualTo("receiver@test.com");
        assertThat(savedMail.getContent()).isEqualTo("MQ를 통한 메일 저장 테스트 본문입니다.");
    }

    @Test
    void testMultipleMailsPublishAndSearch() throws Exception {
        // given: 여러 개의 메일 데이터
        MailDocument mail1 = new MailDocument(
                "mq-search-test-002",
                "프로젝트 보고서",
                "manager@company.com",
                "team@company.com",
                "이번 주 프로젝트 진행상황 보고서입니다. 모든 작업이 예정대로 진행되고 있습니다.",
                "inbox",
                Instant.now()
        );

        MailDocument mail2 = new MailDocument(
                "mq-search-test-003",
                "회의 일정 안내",
                "admin@company.com",
                "all@company.com",
                "다음 주 월요일 오후 2시에 전체 회의가 예정되어 있습니다. 참석 부탁드립니다.",
                "inbox",
                Instant.now()
        );

        // when: MQ를 통해 여러 메일 전송
        rabbitTemplate.convertAndSend(QUEUE_NAME, mail1);
        rabbitTemplate.convertAndSend(QUEUE_NAME, mail2);

        // then: 비동기 처리 대기
        Thread.sleep(3000);

        // 제목으로 검색
        List<MailDocument> projectResults = elasticService.search("프로젝트");
        assertThat(projectResults).isNotEmpty();
        assertThat(projectResults.stream().anyMatch(m -> m.getId().equals("mq-search-test-002"))).isTrue();

        // 본문으로 검색
        List<MailDocument> meetingResults = elasticService.search("회의");
        assertThat(meetingResults).isNotEmpty();
        assertThat(meetingResults.stream().anyMatch(m -> m.getId().equals("mq-search-test-003"))).isTrue();

        // 키워드로 검색
        List<MailDocument> reportResults = elasticService.search("보고서");
        assertThat(reportResults).isNotEmpty();
        assertThat(reportResults.stream().anyMatch(m -> m.getId().equals("mq-search-test-002"))).isTrue();
    }

    @Test
    void testMqMailSearchWithComplexContent() throws Exception {
        // given: 복잡한 내용의 메일
        MailDocument complexMail = new MailDocument(
                "mq-search-test-004",
                "2024년 1분기 실적 보고서",
                "ceo@company.com",
                "board@company.com",
                "2024년 1분기 실적이 예상보다 15% 상승했습니다. 주요 성과: 매출 증가, 고객 만족도 향상, 신제품 출시 성공. 다음 분기 계획도 함께 검토하겠습니다.",
                "sent",
                Instant.now()
        );

        // when: MQ를 통해 전송
        rabbitTemplate.convertAndSend(QUEUE_NAME, complexMail);

        // then: 비동기 처리 대기
        Thread.sleep(2000);

        // 다양한 키워드로 검색 테스트
        List<MailDocument> quarterResults = elasticService.search("1분기");
        assertThat(quarterResults).isNotEmpty();
        assertThat(quarterResults.stream().anyMatch(m -> m.getId().equals("mq-search-test-004"))).isTrue();

        List<MailDocument> performanceResults = elasticService.search("실적");
        assertThat(performanceResults).isNotEmpty();
        assertThat(performanceResults.stream().anyMatch(m -> m.getId().equals("mq-search-test-004"))).isTrue();

        List<MailDocument> revenueResults = elasticService.search("매출");
        assertThat(revenueResults).isNotEmpty();
        assertThat(revenueResults.stream().anyMatch(m -> m.getId().equals("mq-search-test-004"))).isTrue();
    }

    @Test
    void testMqMailSearchWithSpecialCharacters() throws Exception {
        // given: 특수문자가 포함된 메일
        MailDocument specialMail = new MailDocument(
                "mq-search-test-005",
                "특수문자 테스트: @#$%^&*()",
                "test@example.com",
                "user@example.com",
                "이메일 주소: test@example.com, 전화번호: 010-1234-5678, URL: https://example.com",
                "inbox",
                Instant.now()
        );

        // when: MQ를 통해 전송
        rabbitTemplate.convertAndSend(QUEUE_NAME, specialMail);

        // then: 비동기 처리 대기
        Thread.sleep(2000);

        // 특수문자 검색 테스트
        List<MailDocument> emailResults = elasticService.search("test@example.com");
        assertThat(emailResults).isNotEmpty();
        assertThat(emailResults.stream().anyMatch(m -> m.getId().equals("mq-search-test-005"))).isTrue();

        List<MailDocument> phoneResults = elasticService.search("010-1234-5678");
        assertThat(phoneResults).isNotEmpty();
        assertThat(phoneResults.stream().anyMatch(m -> m.getId().equals("mq-search-test-005"))).isTrue();
    }
}