package com.y1.elsdemo.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.y1.elsdemo.model.MailDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ElasticService {

    private final ElasticsearchOperations elasticsearchOperations;

    public void save(MailDocument mail) {
        log.info("Elasticsearch에 메일 저장: {}", mail.getSubject());
        elasticsearchOperations.save(mail);
        log.info("Elasticsearch 저장 완료: {}", mail.getId());
    }

    public MailDocument findById(String id) {
        log.info("ID로 메일 조회: {}", id);
        return elasticsearchOperations.get(id, MailDocument.class);
    }

    public List<MailDocument> search(String keyword) {
        log.info("키워드로 메일 검색: {}", keyword);
        
        Query query = Query.of(q -> q
            .multiMatch(m -> m
                .fields("subject", "content")
                    .query(keyword)
            )
        );

        NativeQuery nativeQuery = NativeQuery.builder()
            .withQuery(query)
            .build();

        SearchHits<MailDocument> hits = elasticsearchOperations.search(nativeQuery, MailDocument.class);
        List<MailDocument> results = hits.get().map(hit -> hit.getContent()).collect(Collectors.toList());
        
        log.info("검색 결과 개수: {}", results.size());
        return results;
    }
}
