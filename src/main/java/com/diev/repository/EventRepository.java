package com.diev.repository;

import com.diev.entity.Event;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.List;
import java.util.Optional;

@RegisterBeanMapper(Event.class)
public interface EventRepository {

    @SqlQuery("""
        INSERT INTO events (name, created_at, folder_path)
        VALUES (:name, :createdAt, :folderPath)
        RETURNING id
    """)
    Long addEvent(@BindBean Event event);

    @SqlQuery("""
    SELECT *
    FROM events
    ORDER BY created_at DESC
""")
    List<Event> getAllEvents();

    @SqlQuery("""
        SELECT *
        FROM events
        WHERE id = :id
    """)
    Optional<Event> findEvent(@Bind("id") Long id);

    @SqlQuery("""
        SELECT COUNT(*)
        FROM events
        WHERE id = :id
    """)
    int countById(@Bind("id") Long id);

    @SqlQuery("""
        SELECT COUNT(*)
        FROM events
        WHERE name = :name
    """)
    int countByName(@Bind("name") String name);

    default boolean existsEvent(Long id) {
        return countById(id) > 0;
    }

    default boolean isNameUnique(String name) {
        return countByName(name) == 0;
    }
}