ALTER TABLE checkins DROP FOREIGN KEY fk_checkins_agent;
ALTER TABLE location_history DROP FOREIGN KEY fk_location_history_agent;

ALTER TABLE agents
    MODIFY id VARCHAR(80) NOT NULL,
    MODIFY external_id VARCHAR(80) NOT NULL,
    MODIFY active BOOLEAN NOT NULL,
    MODIFY last_seen TIMESTAMP(6) NULL,
    MODIFY current_location_updated_at TIMESTAMP(6) NULL,
    MODIFY external_created_at TIMESTAMP(6) NULL,
    MODIFY external_updated_at TIMESTAMP(6) NULL,
    MODIFY created_at TIMESTAMP(6) NOT NULL,
    MODIFY updated_at TIMESTAMP(6) NOT NULL;

CREATE INDEX idx_agents_external_id ON agents (external_id);

ALTER TABLE checkins
    MODIFY id VARCHAR(80) NOT NULL,
    MODIFY agent_id VARCHAR(80) NOT NULL,
    MODIFY notes TEXT,
    MODIFY external_event_id VARCHAR(120),
    MODIFY occurred_at TIMESTAMP(6) NOT NULL,
    MODIFY synced_at TIMESTAMP(6) NULL,
    MODIFY created_at TIMESTAMP(6) NOT NULL,
    MODIFY updated_at TIMESTAMP(6) NOT NULL;

CREATE INDEX idx_checkins_external_event_id ON checkins (external_event_id);

ALTER TABLE location_history
    MODIFY agent_id VARCHAR(80) NOT NULL,
    MODIFY external_event_id VARCHAR(120),
    MODIFY recorded_at TIMESTAMP(6) NOT NULL,
    MODIFY created_at TIMESTAMP(6) NOT NULL,
    MODIFY updated_at TIMESTAMP(6) NOT NULL;

ALTER TABLE geofences
    MODIFY id VARCHAR(80) NOT NULL,
    MODIFY external_id VARCHAR(80) NOT NULL,
    MODIFY type VARCHAR(30) NOT NULL,
    MODIFY alert_on_enter BOOLEAN NOT NULL,
    MODIFY alert_on_exit BOOLEAN NOT NULL,
    MODIFY assigned_teams VARCHAR(255),
    MODIFY synced_at TIMESTAMP(6) NOT NULL,
    MODIFY created_at TIMESTAMP(6) NOT NULL,
    MODIFY updated_at TIMESTAMP(6) NOT NULL;

CREATE INDEX idx_geofences_external_id ON geofences (external_id);

ALTER TABLE sync_executions
    MODIFY started_at TIMESTAMP(6) NOT NULL,
    MODIFY finished_at TIMESTAMP(6) NULL,
    MODIFY created_at TIMESTAMP(6) NOT NULL,
    MODIFY updated_at TIMESTAMP(6) NOT NULL;

ALTER TABLE sync_states
    MODIFY last_successful_sync_at TIMESTAMP(6) NULL,
    MODIFY last_attempt_at TIMESTAMP(6) NULL,
    MODIFY created_at TIMESTAMP(6) NOT NULL,
    MODIFY updated_at TIMESTAMP(6) NOT NULL;

ALTER TABLE checkins
    ADD CONSTRAINT fk_checkins_agent FOREIGN KEY (agent_id) REFERENCES agents (id);

ALTER TABLE location_history
    ADD CONSTRAINT fk_location_history_agent FOREIGN KEY (agent_id) REFERENCES agents (id);
