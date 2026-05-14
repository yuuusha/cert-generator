package com.diev.repository;

import com.diev.entity.Template;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.List;
import java.util.Optional;

@RegisterBeanMapper(Template.class)
public interface TemplateRepository {

    @SqlQuery("""
        INSERT INTO templates (name, file_type, file_path, created_at)
        VALUES (:name, :fileType, :filePath, :createdAt)
        RETURNING id
    """)
    Long addTemplate(@BindBean Template template);

    @SqlQuery("""
        SELECT *
        FROM templates
        ORDER BY created_at DESC
    """)
    List<Template> getAllTemplates();

    @SqlQuery("""
        SELECT *
        FROM templates
        WHERE id = :id
    """)
    Optional<Template> findTemplateById(@Bind("id") Long id);
}