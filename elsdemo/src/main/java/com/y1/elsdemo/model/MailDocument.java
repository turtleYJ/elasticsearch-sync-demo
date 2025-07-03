package com.y1.elsdemo.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

import jakarta.validation.constraints.*;
import java.time.Instant;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "mails", writeTypeHint = WriteTypeHint.FALSE)
public class MailDocument {

    @Id
    private String id;

    @NotBlank(message = "Subject is required")
    @Size(max = 200, message = "Subject must not exceed 200 characters")
    private String subject;

    @NotBlank(message = "Sender is required")
    @Email(message = "Sender must be a valid email address")
    private String sender;

    @NotBlank(message = "Receiver is required")
    @Email(message = "Receiver must be a valid email address")
    private String receiver;

    @NotBlank(message = "Content is required")
    @Size(max = 10000, message = "Content must not exceed 10000 characters")
    private String content;

    @NotBlank(message = "Folder is required")
    @Size(max = 50, message = "Folder must not exceed 50 characters")
    private String folder;

    @Field(type = FieldType.Date)
    @NotNull(message = "Timestamp is required")
    private Instant timestamp;
}
