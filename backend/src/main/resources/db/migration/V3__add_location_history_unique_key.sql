ALTER TABLE location_history
ADD CONSTRAINT uk_location_history_agent_recorded_source
UNIQUE (agent_id, recorded_at, source);
