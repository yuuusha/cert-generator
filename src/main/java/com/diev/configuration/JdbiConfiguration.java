package com.diev.configuration;

import com.diev.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.spi.JdbiPlugin;
import org.jdbi.v3.jackson2.Jackson2Config;
import org.jdbi.v3.jackson2.Jackson2Plugin;
import org.jdbi.v3.postgres.PostgresPlugin;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

import javax.sql.DataSource;

@Configuration
public class JdbiConfiguration {
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    public Jdbi jdbi(DataSource dataSource, ObjectMapper objectMapper) {
        TransactionAwareDataSourceProxy proxy = new TransactionAwareDataSourceProxy(dataSource);
        Jdbi jdbi = Jdbi.create(proxy);
        jdbi.installPlugin(new PostgresPlugin());
        jdbi.installPlugin(new SqlObjectPlugin());
        jdbi.installPlugin(new Jackson2Plugin());
        jdbi.installPlugin(new JdbiPlugin() {
            @Override
            public void customizeJdbi(Jdbi jdbi) {
                jdbi.getConfig(Jackson2Config.class)
                        .setMapper(objectMapper.copy());
            }
        });
        return jdbi;
    }

    @Bean
    public EventRepository EventRepository(Jdbi jdbi) {
        return jdbi.onDemand(EventRepository.class);
    }

    @Bean
    public GenerationRepository GenerationRepository(Jdbi jdbi) {
        return jdbi.onDemand(GenerationRepository.class);
    }

    @Bean
    public ParticipantRepository ParticipantRepository(Jdbi jdbi) {
        return jdbi.onDemand(ParticipantRepository.class);
    }

    @Bean
    public SettingsRepository SettingsRepository(Jdbi jdbi) {
        return jdbi.onDemand(SettingsRepository.class);
    }

    @Bean
    public TemplateRepository TemplateRepository(Jdbi jdbi) {
        return jdbi.onDemand(TemplateRepository.class);
    }
}
