ALTER TABLE events
    ADD CONSTRAINT uq_events_name UNIQUE (name);

ALTER TABLE settings
    ADD CONSTRAINT uq_settings_key UNIQUE (key);

ALTER TABLE templates
    ADD CONSTRAINT chk_templates_file_type
        CHECK (file_type IN ('PDF', 'PNG', 'JPEG', 'JPG'));

ALTER TABLE generations
    ADD CONSTRAINT chk_generations_document_type
        CHECK (document_type IN ('CHARTER', 'CERTIFICATE', 'DIPLOMA'));

ALTER TABLE generations
    ADD CONSTRAINT chk_generations_output_format
        CHECK (output_format IN ('PDF', 'PNG'));

ALTER TABLE generations
    ADD CONSTRAINT chk_generations_status
        CHECK (status IN ('SUCCESS', 'ERROR'));

ALTER TABLE generations
    ADD CONSTRAINT fk_generations_event
        FOREIGN KEY (event_id)
            REFERENCES events(id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT;

ALTER TABLE generations
    ADD CONSTRAINT fk_generations_participant
        FOREIGN KEY (participant_id)
            REFERENCES participants(id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT;

ALTER TABLE generations
    ADD CONSTRAINT fk_generations_template
        FOREIGN KEY (template_id)
            REFERENCES templates(id)
            ON UPDATE CASCADE
            ON DELETE RESTRICT;

CREATE INDEX idx_generations_event_id ON generations(event_id);
CREATE INDEX idx_generations_participant_id ON generations(participant_id);
CREATE INDEX idx_generations_template_id ON generations(template_id);
CREATE INDEX idx_generations_created_at ON generations(created_at);