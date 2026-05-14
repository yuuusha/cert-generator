package com.diev.repository;

import com.diev.entity.Setting;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;

import java.util.Optional;

@RegisterBeanMapper(Setting.class)
public interface SettingsRepository {

    @SqlQuery("""
        INSERT INTO settings (key, value)
        VALUES (:key, :value)
        ON CONFLICT (key)
        DO UPDATE SET value = EXCLUDED.value
        RETURNING id
    """)
    Long saveSetting(@BindBean Setting setting);

    @SqlQuery("""
        SELECT *
        FROM settings
        WHERE key = :key
    """)
    Optional<Setting> getSettingByKey(@Bind("key") String key);
}