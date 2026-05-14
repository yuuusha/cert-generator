package com.diev.generator.entity;

public enum DocumentType {
    CHARTER("Грамота"),
    CERTIFICATE("Сертификат"),
    DIPLOMA("Диплом");

    private final String label;

    DocumentType(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }
}