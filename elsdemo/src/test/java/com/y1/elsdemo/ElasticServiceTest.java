package com.y1.elsdemo;

import com.y1.elsdemo.model.MailDocument;
import com.y1.elsdemo.service.ElasticService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ElasticServiceTest {

    @Autowired
    ElasticService elasticService;

    @BeforeEach
    void cleanup() {

    }

    @Test
    void testSaveAndFindById() {
        MailDocument mail = new MailDocument(
                "test001",
                "TDD 제목",
                "me@sample.com",
                "you@sample.com",
                "본문 컨텐츠",
                "inbox",
                LocalDateTime.now().withNano(0)
        );

        elasticService.save(mail);

        MailDocument found = elasticService.findById("test001");
        assertThat(found).isNotNull();
        assertThat(found.getSubject()).isEqualTo("TDD 제목");
    }

    @Test
    void testSearchByKeyword() {
        // given
        MailDocument mail = new MailDocument(
                "test002",
                "보고서 TDD",
                "me@sample.com",
                "you@sample.com",
                "내용: TDD 연습",
                "inbox",
                LocalDateTime.now()
        );

        // when
        List<MailDocument> result = elasticService.search("TDD");

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.stream().anyMatch(m -> m.getId().equals("test002"))).isTrue();
    }
}
