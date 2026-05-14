package com.diev.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Generation {
    private Long id;
    private Long eventId;
    private Long participantId;
    private Long templateId;
    private String documentType;
    private String outputFormat;
    private String filePath;
    private String status;
    private Instant createdAt;
}
