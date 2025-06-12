package com.ucapital.sharkshub.investor.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("processedFiles")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessedFileRecord {
    @Id
    private String checksum;
    private long jobExecutionId;
}