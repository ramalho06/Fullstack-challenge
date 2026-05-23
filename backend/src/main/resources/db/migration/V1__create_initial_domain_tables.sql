CREATE TABLE agents (
    id BIGINT NOT NULL AUTO_INCREMENT,
    external_id VARCHAR(100) NOT NULL,
    name VARCHAR(150) NOT NULL,
    role VARCHAR(30),
    team VARCHAR(100),
    phone VARCHAR(30),
    email VARCHAR(150),
    active BIT(1) NOT NULL,
    status VARCHAR(30) NOT NULL,
    battery DECIMAL(5, 2),
    last_seen DATETIME(6),
    current_latitude DECIMAL(10, 7),
    current_longitude DECIMAL(10, 7),
    current_address VARCHAR(255),
    current_accuracy DECIMAL(10, 2),
    current_speed DECIMAL(10, 2),
    current_location_updated_at DATETIME(6),
    external_created_at DATETIME(6),
    external_updated_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_agents_external_id UNIQUE (external_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_agents_status ON agents (status);
CREATE INDEX idx_agents_active ON agents (active);

CREATE TABLE location_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    agent_id BIGINT NOT NULL,
    latitude DECIMAL(10, 7) NOT NULL,
    longitude DECIMAL(10, 7) NOT NULL,
    address VARCHAR(255),
    accuracy DECIMAL(10, 2),
    speed DECIMAL(10, 2),
    battery DECIMAL(5, 2),
    recorded_at DATETIME(6) NOT NULL,
    source VARCHAR(30) NOT NULL,
    external_event_id VARCHAR(100),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_location_history_agent FOREIGN KEY (agent_id) REFERENCES agents (id),
    CONSTRAINT uk_location_history_external_event_id UNIQUE (external_event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_location_history_agent_recorded_at ON location_history (agent_id, recorded_at);
CREATE INDEX idx_location_history_source ON location_history (source);

CREATE TABLE checkins (
    id BIGINT NOT NULL AUTO_INCREMENT,
    agent_id BIGINT NOT NULL,
    type VARCHAR(40) NOT NULL,
    source VARCHAR(30) NOT NULL,
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    address VARCHAR(255),
    accuracy DECIMAL(10, 2),
    speed DECIMAL(10, 2),
    notes VARCHAR(500),
    distance_from_previous DECIMAL(12, 2),
    external_event_id VARCHAR(100),
    occurred_at DATETIME(6) NOT NULL,
    synced_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_checkins_agent FOREIGN KEY (agent_id) REFERENCES agents (id),
    CONSTRAINT uk_checkins_external_event_id UNIQUE (external_event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_checkins_agent_occurred_at ON checkins (agent_id, occurred_at);
CREATE INDEX idx_checkins_type ON checkins (type);
CREATE INDEX idx_checkins_source ON checkins (source);

CREATE TABLE geofences (
    id BIGINT NOT NULL AUTO_INCREMENT,
    external_id VARCHAR(100) NOT NULL,
    name VARCHAR(150) NOT NULL,
    type VARCHAR(20) NOT NULL,
    coordinates_json TEXT NOT NULL,
    alert_on_enter BIT(1) NOT NULL,
    alert_on_exit BIT(1) NOT NULL,
    assigned_teams VARCHAR(500),
    synced_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_geofences_external_id UNIQUE (external_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_geofences_type ON geofences (type);

CREATE TABLE sync_executions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    sync_type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    started_at DATETIME(6) NOT NULL,
    finished_at DATETIME(6),
    items_processed INT,
    items_created INT,
    items_updated INT,
    items_skipped INT,
    error_message TEXT,
    http_status INT,
    sync_token_before VARCHAR(1024),
    sync_token_after VARCHAR(1024),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_sync_executions_sync_type ON sync_executions (sync_type);
CREATE INDEX idx_sync_executions_status ON sync_executions (status);
CREATE INDEX idx_sync_executions_started_at ON sync_executions (started_at);

CREATE TABLE sync_states (
    id BIGINT NOT NULL AUTO_INCREMENT,
    sync_type VARCHAR(30) NOT NULL,
    last_sync_token VARCHAR(1024),
    last_successful_sync_at DATETIME(6),
    last_attempt_at DATETIME(6),
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_sync_states_sync_type UNIQUE (sync_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
