package com.diev.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    private Long id;
    private String name;
    private Instant createdAt;
    private String folderPath;
}
