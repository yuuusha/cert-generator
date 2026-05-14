package com.diev.repository;

import com.diev.entity.Participant;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.List;
import java.util.Optional;

@RegisterBeanMapper(Participant.class)
public interface ParticipantRepository {

    @SqlQuery("""
        INSERT INTO participants (full_name, university, city, faculty)
        VALUES (:fullName, :university, :city, :faculty)
        RETURNING id
    """)
    Long addParticipant(@BindBean Participant participant);

    @SqlQuery("""
        SELECT *
        FROM participants
        WHERE id = :id
    """)
    Optional<Participant> findParticipant(@Bind("id") Long id);

    @SqlQuery("""
    SELECT *
    FROM participants
    ORDER BY full_name
""")
    List<Participant> getAllParticipants();
}