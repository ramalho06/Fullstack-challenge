package com.media4all.tracking.location;

import com.media4all.tracking.agent.Agent;
import com.media4all.tracking.external.location.ExternalLocationDto;
import org.springframework.stereotype.Component;

@Component
public class LocationMapper {

    public void updateAgentCurrentLocation(Agent agent, ExternalLocationDto dto) {
        agent.setCurrentLatitude(dto.latitude());
        agent.setCurrentLongitude(dto.longitude());
        agent.setCurrentAddress(dto.currentAddress());
        agent.setCurrentAccuracy(dto.accuracy());
        agent.setCurrentSpeed(dto.speed());
        agent.setBattery(dto.battery());
        agent.setStatus(dto.status());
        agent.setLastSeen(dto.lastSeen());
        agent.setCurrentLocationUpdatedAt(dto.lastSeen());
    }

    public LocationHistory toHistory(Agent agent, ExternalLocationDto dto) {
        LocationHistory history = new LocationHistory();
        history.setAgent(agent);
        history.setLatitude(dto.latitude());
        history.setLongitude(dto.longitude());
        history.setAddress(dto.currentAddress());
        history.setAccuracy(dto.accuracy());
        history.setSpeed(dto.speed());
        history.setBattery(dto.battery());
        history.setRecordedAt(dto.lastSeen());
        history.setSource(LocationSource.GPS_SYNC);
        history.setExternalEventId(null);
        return history;
    }
}
