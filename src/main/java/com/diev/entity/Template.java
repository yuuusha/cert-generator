package com.diev.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Template {
    private Long id;
    private String name;
    private String fileType;
    private String filePath;
    private Instant createdAt;
}
