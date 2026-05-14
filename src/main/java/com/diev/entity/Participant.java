package com.diev.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Participant {
    private Long id;
    private String fullName;
    private String university;
    private String city;
    private String faculty;
}
