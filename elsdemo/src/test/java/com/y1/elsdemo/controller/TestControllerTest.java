package com.y1.elsdemo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.y1.elsdemo.model.MailDocument;
import com.y1.elsdemo.service.ElasticService;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TestController.class)
public class TestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ElasticService elasticService;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSaveMail() throws Exception {
        MailDocument mail = new MailDocument(
                "test001",
                "테스트 제목",
                "me@sample.com",
                "you@sample.com",
                "테스트 내용",
                "inbox",
                Instant.now()
        );

        mockMvc.perform(post("/api/mail")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mail)))
                .andExpect(status().isOk())
                .andExpect(content().string("메일이 성공적으로 저장되었습니다."));

        verify(elasticService).save(any(MailDocument.class));
    }

    @Test
    void testSaveMailAsync() throws Exception {
        MailDocument mail = new MailDocument(
                "test002",
                "비동기 테스트 제목",
                "me@sample.com",
                "you@sample.com",
                "비동기 테스트 내용",
                "inbox",
                Instant.now()
        );

        mockMvc.perform(post("/api/mail/mq")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mail)))
                .andExpect(status().isOk())
                .andExpect(content().string("메일이 큐에 성공적으로 전송되었습니다."));

        verify(rabbitTemplate).convertAndSend(eq("mail-queue"), any(MailDocument.class));
    }

    @Test
    void testSearchMail() throws Exception {
        List<MailDocument> mockResults = Arrays.asList(
                new MailDocument("test003", "검색 결과1", "me@sample.com", "you@sample.com", "내용1", "inbox", Instant.now()),
                new MailDocument("test004", "검색 결과2", "me@sample.com", "you@sample.com", "내용2", "inbox", Instant.now())
        );

        when(elasticService.search("테스트")).thenReturn(mockResults);

        mockMvc.perform(get("/api/mail/search")
                        .param("q", "테스트"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("test003"))
                .andExpect(jsonPath("$[1].id").value("test004"));

        verify(elasticService).search("테스트");
    }

    @Test
    void testGetMailById() throws Exception {
        MailDocument mockMail = new MailDocument(
                "test005",
                "조회 테스트",
                "me@sample.com",
                "you@sample.com",
                "조회 내용",
                "inbox",
                Instant.now()
        );

        when(elasticService.findById("test005")).thenReturn(mockMail);

        mockMvc.perform(get("/api/mail/test005"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("test005"))
                .andExpect(jsonPath("$.subject").value("조회 테스트"));

        verify(elasticService).findById("test005");
    }

    @Test
    void testGetMailByIdNotFound() throws Exception {
        when(elasticService.findById("nonexistent")).thenReturn(null);

        mockMvc.perform(get("/api/mail/nonexistent"))
                .andExpect(status().isNotFound());

        verify(elasticService).findById("nonexistent");
    }

    @Test
    void testDeleteMailById() throws Exception {
        doNothing().when(elasticService).deleteById("test006");

        mockMvc.perform(delete("/api/mail/test006"))
                .andExpect(status().isOk())
                .andExpect(content().string("메일이 성공적으로 삭제되었습니다."));

        verify(elasticService).deleteById("test006");
    }

    @Test
    void testDeleteMailByIdWithException() throws Exception {
        doThrow(new RuntimeException("삭제 실패")).when(elasticService).deleteById("error");

        mockMvc.perform(delete("/api/mail/error"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string("메일 삭제 중 오류가 발생했습니다: 삭제 실패"));

        verify(elasticService).deleteById("error");
    }
}
