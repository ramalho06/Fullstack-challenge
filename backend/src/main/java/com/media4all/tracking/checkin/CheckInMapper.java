package com.media4all.tracking.checkin;

import com.media4all.tracking.agent.Agent;
import com.media4all.tracking.checkin.dto.CheckInResponse;
import com.media4all.tracking.external.checkin.ExternalCheckInDto;
import com.media4all.tracking.location.LocationHistory;
import com.media4all.tracking.location.LocationSource;
import org.springframework.stereotype.Component;

@Component
public class CheckInMapper {

    public CheckIn toEntity(ExternalCheckInDto dto, Agent agent) {
        CheckIn checkIn = new CheckIn();
        checkIn.setId(dto.id());
        updateEntity(checkIn, dto, agent);
        return checkIn;
    }

    public void updateEntity(CheckIn checkIn, ExternalCheckInDto dto, Agent agent) {
        checkIn.setAgent(agent);
        checkIn.setType(dto.type());
        checkIn.setSource(dto.source());
        checkIn.setLatitude(dto.latitude());
        checkIn.setLongitude(dto.longitude());
        checkIn.setAddress(dto.address());
        checkIn.setAccuracy(dto.accuracy());
        checkIn.setSpeed(dto.speed());
        checkIn.setNotes(dto.notes());
        checkIn.setDistanceFromPrevious(dto.distanceFromPrevious());
        checkIn.setExternalEventId(dto.externalEventId());
        checkIn.setOccurredAt(dto.occurredAt());
        checkIn.setSyncedAt(dto.syncedAt());
    }

    public LocationHistory toLocationHistory(CheckIn checkIn) {
        LocationHistory history = new LocationHistory();
        history.setAgent(checkIn.getAgent());
        history.setLatitude(checkIn.getLatitude());
        history.setLongitude(checkIn.getLongitude());
        history.setAddress(checkIn.getAddress());
        history.setAccuracy(checkIn.getAccuracy());
        history.setSpeed(checkIn.getSpeed());
        history.setBattery(null);
        history.setRecordedAt(checkIn.getOccurredAt());
        history.setSource(mapToLocationSource(checkIn.getSource()));
        history.setExternalEventId(checkIn.getExternalEventId());
        return history;
    }

    public LocationSource mapToLocationSource(CheckInSource source) {
        return switch (source) {
            case MANUAL -> LocationSource.MANUAL_CHECKIN;
            case GPS_SYNC -> LocationSource.GPS_SYNC;
            case EVENT_SYNC -> LocationSource.EVENT_SYNC;
        };
    }

    public CheckInResponse toResponse(CheckIn checkIn) {
        return new CheckInResponse(
                checkIn.getId(),
                checkIn.getAgent().getId(),
                checkIn.getType(),
                checkIn.getSource(),
                checkIn.getLatitude(),
                checkIn.getLongitude(),
                checkIn.getAddress(),
                checkIn.getAccuracy(),
                checkIn.getSpeed(),
                checkIn.getNotes(),
                checkIn.getDistanceFromPrevious(),
                checkIn.getExternalEventId(),
                checkIn.getOccurredAt(),
                checkIn.getSyncedAt()
        );
    }
}
