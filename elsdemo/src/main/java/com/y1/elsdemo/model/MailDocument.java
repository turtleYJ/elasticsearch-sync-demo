package com.y1.elsdemo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import java.io.Serializable;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "mails", writeTypeHint = WriteTypeHint.FALSE)
public class MailDocument implements Serializable {

    @Id
    private String id;

    private String subject;
    private String sender;
    private String receiver;
    private String content;
    private String folder;
    @Field(type = FieldType.Date)
    private Instant timestamp;
}
