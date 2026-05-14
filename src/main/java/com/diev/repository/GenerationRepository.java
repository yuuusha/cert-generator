package com.diev.repository;

import com.diev.entity.Generation;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.List;

@RegisterBeanMapper(Generation.class)
public interface GenerationRepository {

    @SqlQuery("""
        INSERT INTO generations
            (event_id, participant_id, template_id, document_type, output_format, file_path, status, created_at)
        VALUES
            (:eventId, :participantId, :templateId, :documentType, :outputFormat, :filePath, :status, :createdAt)
        RETURNING id
    """)
    Long saveGeneration(@BindBean Generation generation);

    @SqlQuery("""
        SELECT *
        FROM generations
        ORDER BY created_at DESC
    """)
    List<Generation> getGenerationHistory();
}