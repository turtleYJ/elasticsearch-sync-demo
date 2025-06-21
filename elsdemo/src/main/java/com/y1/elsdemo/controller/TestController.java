package com.y1.elsdemo.controller;

import com.y1.elsdemo.model.MailDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mail")
@RequiredArgsConstructor
public class TestController {

    private final ElasticService elasticService;

    // 메일 저장
    @PostMapping
    public String saveMail(@RequestBody MailDocument mail) {
        elasticService.save(mail);
        return "saved";
    }

    // 메일 검색 (제목, 본문 대상 키워드 검색)
    @GetMapping("/search")
    public List<MailDocument> searchMail(@RequestParam("q") String keyword) {
        return elasticService.search(keyword);
    }

    // 메일 단건 조회 (id 기반)
    @GetMapping("/{id}")
    public MailDocument getMailById(@PathVariable String id) {
        return elasticService.findById(id);
    }
}
