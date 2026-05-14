package com.diev.generator.entity;

public record TemplateLayout(
        RelativeFieldBox titleBox,
        RelativeFieldBox nameBox,
        RelativeFieldBox universityBox,
        RelativeFieldBox cityBox,
        RelativeFieldBox facultyBox,
        RelativeFieldBox dateBox
) { }