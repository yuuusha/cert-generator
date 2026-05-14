package com.diev.generator.entity;

public enum OutputFormat {
    PNG("png"),
    PDF("pdf");

    private final String extension;

    OutputFormat(String extension) {
        this.extension = extension;
    }

    public String extension() {
        return extension;
    }
}