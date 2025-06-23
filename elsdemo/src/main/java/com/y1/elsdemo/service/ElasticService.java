package com.y1.elsdemo.service;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.y1.elsdemo.model.MailDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ElasticService {

    private final ElasticsearchOperations elasticsearchOperations;

    public void save(MailDocument mail) {
        elasticsearchOperations.save(mail);
    }

    public MailDocument findById(String id) {
        return elasticsearchOperations.get(id, MailDocument.class);
    }

    public List<MailDocument> search(String keyword) {
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

        return hits.get().map(hit -> hit.getContent()).collect(Collectors.toList());
    }
}
