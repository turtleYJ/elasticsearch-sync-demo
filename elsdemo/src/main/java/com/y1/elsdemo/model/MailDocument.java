package com.y1.elsdemo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "mails")
public class MailDocument {

    @Id
    private String id;

    private String subject;
    private String sender;
    private String receiver;
    private String content;
    private String folder;
    private LocalDateTime timestamp;
}
