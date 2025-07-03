package com.y1.elsdemo.controller;

import com.y1.elsdemo.model.MailDocument;
import com.y1.elsdemo.service.ElasticService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;

@RestController
@RequestMapping("/api/mail")
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final ElasticService elasticService;
    private final RabbitTemplate rabbitTemplate;

    // 메일 저장 (동기)
    @PostMapping
    public ResponseEntity<String> saveMail(@Valid @RequestBody MailDocument mail) {
        try {
            log.info("메일 저장 요청: {}", mail.getSubject());
            elasticService.save(mail);
            log.info("메일 저장 완료: {}", mail.getId());
            return ResponseEntity.ok("메일이 성공적으로 저장되었습니다.");
        } catch (Exception e) {
            log.error("메일 저장 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("메일 저장 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 메일 저장 (비동기 - MQ)
    @PostMapping("/mq")
    public ResponseEntity<String> saveMailAsync(@Valid @RequestBody MailDocument mail) {
        try {
            log.info("MQ 메일 저장 요청: {}", mail.getSubject());
            rabbitTemplate.convertAndSend("mail-queue", mail);
            log.info("MQ 메시지 발행 완료: {}", mail.getSubject());
            return ResponseEntity.ok("메일이 큐에 성공적으로 전송되었습니다.");
        } catch (Exception e) {
            log.error("MQ 메시지 발행 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("메시지 큐 전송 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    // 메일 검색 (제목, 본문 대상 키워드 검색)
    @GetMapping("/search")
    public ResponseEntity<List<MailDocument>> searchMail(@RequestParam("q") @NotBlank String keyword) {
        try {
            log.info("메일 검색 요청: {}", keyword);
            List<MailDocument> results = elasticService.search(keyword);
            log.info("검색 결과 개수: {}", results.size());
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            log.error("메일 검색 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 메일 단건 조회 (id 기반)
    @GetMapping("/{id}")
    public ResponseEntity<MailDocument> getMailById(@PathVariable String id) {
        try {
            log.info("메일 조회 요청: {}", id);
            MailDocument mail = elasticService.findById(id);
            if (mail != null) {
                log.info("메일 조회 완료: {}", id);
                return ResponseEntity.ok(mail);
            } else {
                log.warn("메일을 찾을 수 없음: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("메일 조회 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    // 메일 삭제 (id 기반)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMailById(@PathVariable String id) {
        try {
            log.info("메일 삭제 요청: {}", id);
            elasticService.deleteById(id);
            log.info("메일 삭제 완료: {}", id);
            return ResponseEntity.ok("메일이 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            log.error("메일 삭제 실패: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("메일 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
