package com.diev.service;

import com.diev.entity.Event;
import com.diev.entity.Generation;
import com.diev.entity.Participant;
import com.diev.entity.Template;
import com.diev.generator.DocumentGenerator;
import com.diev.generator.entity.DocumentType;
import com.diev.generator.entity.OutputFormat;
import com.diev.repository.GenerationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenerationService {

    private static final String STATUS_SUCCESS = "SUCCESS";
    private static final String STATUS_ERROR = "ERROR";

    private final EventService eventService;
    private final ParticipantService participantService;
    private final TemplateService templateService;
    private final GenerationRepository generationRepository;
    private final DocumentGenerator documentGenerator;

    public String generateDocument(
            Long eventId,
            Long participantId,
            Long templateId,
            String documentType,
            String outputFormat
    ) {
        Event event = eventService.findEvent(eventId);
        Participant participant = participantService.findParticipant(participantId);
        Template template = templateService.getTemplateForGeneration(templateId);

        DocumentType docType = parseDocumentType(documentType);
        OutputFormat format = parseOutputFormat(outputFormat);

        try {
            log.info("Starting generation: event={}, participant={}, template={}, type={}, format={}",
                    event.getName(),
                    participant.getFullName(),
                    template.getName(),
                    docType,
                    format
            );

            Path generatedFile = documentGenerator.generate(
                    Paths.get(template.getFilePath()),
                    participant,
                    docType,
                    format,
                    Paths.get(event.getFolderPath())
            );

            saveGenerationRecord(
                    eventId,
                    participantId,
                    templateId,
                    docType,
                    format,
                    generatedFile.toString(),
                    STATUS_SUCCESS
            );

            log.info("Generation finished successfully: {}", generatedFile);
            return generatedFile.toString();
        } catch (Exception e) {
            saveGenerationRecord(
                    eventId,
                    participantId,
                    templateId,
                    docType,
                    format,
                    buildExpectedOutputPath(event.getFolderPath(), participant.getFullName(), docType, format).toString(),
                    STATUS_ERROR
            );

            log.error("Generation failed", e);
            throw new RuntimeException("Document generation failed", e);
        }
    }

    public BatchGenerationResult generateDocumentsForParticipants(
            Long eventId,
            List<Long> participantIds,
            Long templateId,
            String documentType,
            String outputFormat
    ) {
        if (participantIds == null || participantIds.isEmpty()) {
            throw new IllegalArgumentException("At least one participant must be selected");
        }

        Event event = eventService.findEvent(eventId);
        Template template = templateService.getTemplateForGeneration(templateId);
        DocumentType docType = parseDocumentType(documentType);
        OutputFormat format = parseOutputFormat(outputFormat);

        List<String> generatedFiles = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (Long participantId : participantIds) {
            Participant participant = participantService.findParticipant(participantId);

            try {
                log.info("Starting generation: event={}, participant={}, template={}, type={}, format={}",
                        event.getName(),
                        participant.getFullName(),
                        template.getName(),
                        docType,
                        format
                );

                Path generatedFile = documentGenerator.generate(
                        Paths.get(template.getFilePath()),
                        participant,
                        docType,
                        format,
                        Paths.get(event.getFolderPath())
                );

                saveGenerationRecord(
                        eventId,
                        participantId,
                        templateId,
                        docType,
                        format,
                        generatedFile.toString(),
                        STATUS_SUCCESS
                );

                generatedFiles.add(generatedFile.toString());
                log.info("Generation finished successfully: {}", generatedFile);
            } catch (Exception e) {
                Path expectedPath = buildExpectedOutputPath(
                        event.getFolderPath(),
                        participant.getFullName(),
                        docType,
                        format
                );

                saveGenerationRecord(
                        eventId,
                        participantId,
                        templateId,
                        docType,
                        format,
                        expectedPath.toString(),
                        STATUS_ERROR
                );

                String message = participant.getFullName() + ": " + e.getMessage();
                errors.add(message);
                log.error("Generation failed for participant {}", participant.getFullName(), e);
            }
        }

        return new BatchGenerationResult(generatedFiles, errors);
    }

    private void saveGenerationRecord(
            Long eventId,
            Long participantId,
            Long templateId,
            DocumentType documentType,
            OutputFormat outputFormat,
            String filePath,
            String status
    ) {
        Generation generation = new Generation();
        generation.setEventId(eventId);
        generation.setParticipantId(participantId);
        generation.setTemplateId(templateId);
        generation.setDocumentType(documentType.name());
        generation.setOutputFormat(outputFormat.name());
        generation.setFilePath(filePath);
        generation.setStatus(status);
        generation.setCreatedAt(Instant.now());

        Long id = generationRepository.saveGeneration(generation);
        generation.setId(id);
    }

    private DocumentType parseDocumentType(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Document type is required");
        }
        return DocumentType.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    private OutputFormat parseOutputFormat(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Output format is required");
        }
        return OutputFormat.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }

    private Path buildExpectedOutputPath(String folderPath, String fullName, DocumentType documentType, OutputFormat format) {
        String shortName = buildShortName(fullName);
        String fileName = sanitize(documentType.label() + "_" + shortName + "_" + LocalDate.now()) + "." + format.extension();
        return Paths.get(folderPath).resolve(fileName);
    }

    private String buildShortName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return "Unknown";
        }
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length >= 2) {
            return parts[0] + "_" + parts[1];
        }
        return parts[0];
    }

    private String sanitize(String value) {
        return value.replaceAll("[\\\\/:*?\"<>|]", "_").replaceAll("\\s+", "_");
    }

    public List<Generation> getGenerationHistory() {
        return generationRepository.getGenerationHistory();
    }

    public record BatchGenerationResult(List<String> generatedFiles, List<String> errors) {
    }
}