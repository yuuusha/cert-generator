package com.diev.service;

import com.diev.entity.Template;
import com.diev.repository.TemplateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TemplateService {

    private final TemplateRepository templateRepository;
    private static final Path TEMPLATES_ROOT = Paths.get("src/main/resources/templates");

    @Transactional
    public Template registerTemplateFromResources(String name, String fileName, String fileType) {
        Path filePath = TEMPLATES_ROOT.resolve(fileName);

        if (!Files.exists(filePath)) {
            throw new IllegalArgumentException("Template file not found: " + filePath);
        }

        return saveTemplate(name, fileType, filePath.toString());
    }

    @Transactional
    public Template saveTemplate(String name, String fileType, String filePath) {
        validateTemplate(name, fileType, filePath);

        Template template = new Template();
        template.setName(name);
        template.setFileType(fileType.toUpperCase());
        template.setFilePath(filePath);
        template.setCreatedAt(Instant.now());

        Long templateId = templateRepository.addTemplate(template);
        template.setId(templateId);

        return template;
    }

    public Template getTemplateForGeneration(Long templateId) {
        return templateRepository.findTemplateById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Template not found with id: " + templateId));
    }

    public List<Template> getAllTemplates() {
        return templateRepository.getAllTemplates();
    }

    private void validateTemplate(String name, String fileType, String filePath) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Template name is required");
        }
        if (fileType == null || fileType.trim().isEmpty()) {
            throw new IllegalArgumentException("File type is required");
        }
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path is required");
        }
    }
}