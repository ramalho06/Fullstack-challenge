package com.media4all.tracking.checkin;

import com.media4all.tracking.agent.Agent;
import com.media4all.tracking.agent.AgentRepository;
import com.media4all.tracking.checkin.dto.CheckInCreateRequest;
import com.media4all.tracking.checkin.dto.CheckInResponse;
import com.media4all.tracking.common.exception.ResourceNotFoundException;
import com.media4all.tracking.location.LocationHistory;
import com.media4all.tracking.location.LocationHistoryRepository;
import com.media4all.tracking.location.LocationSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class ManualCheckInService {

    private static final BigDecimal MAX_LOCATION_HISTORY_ACCURACY = BigDecimal.valueOf(50);
    private static final String LOCAL_CHECK_IN_PREFIX = "local_ci_";

    private final AgentRepository agentRepository;
    private final CheckInRepository checkInRepository;
    private final LocationHistoryRepository locationHistoryRepository;
    private final CheckInMapper checkInMapper;

    public ManualCheckInService(
            AgentRepository agentRepository,
            CheckInRepository checkInRepository,
            LocationHistoryRepository locationHistoryRepository,
            CheckInMapper checkInMapper
    ) {
        this.agentRepository = agentRepository;
        this.checkInRepository = checkInRepository;
        this.locationHistoryRepository = locationHistoryRepository;
        this.checkInMapper = checkInMapper;
    }

    @Transactional
    public CheckInResponse createManualCheckIn(CheckInCreateRequest request) {
        Agent agent = agentRepository.findById(request.agentId())
                .orElseThrow(() -> new ResourceNotFoundException("Agent", request.agentId()));
        Instant occurredAt = request.occurredAt() == null ? Instant.now() : request.occurredAt();

        CheckIn checkIn = new CheckIn();
        checkIn.setId(LOCAL_CHECK_IN_PREFIX + UUID.randomUUID().toString().replace("-", ""));
        checkIn.setAgent(agent);
        checkIn.setType(request.type());
        checkIn.setSource(CheckInSource.MANUAL);
        checkIn.setLatitude(request.latitude());
        checkIn.setLongitude(request.longitude());
        checkIn.setAddress(request.address());
        checkIn.setAccuracy(request.accuracy());
        checkIn.setSpeed(request.speed());
        checkIn.setNotes(request.notes());
        checkIn.setDistanceFromPrevious(null);
        checkIn.setExternalEventId(null);
        checkIn.setOccurredAt(occurredAt);
        checkIn.setSyncedAt(Instant.now());

        CheckIn saved = checkInRepository.save(checkIn);
        createLocationHistoryIfApplicable(saved);
        return checkInMapper.toResponse(saved);
    }

    private void createLocationHistoryIfApplicable(CheckIn checkIn) {
        if (!shouldCreateLocationHistory(checkIn)) {
            return;
        }

        boolean alreadyExists = locationHistoryRepository.existsByAgentIdAndRecordedAtAndSource(
                checkIn.getAgent().getId(),
                checkIn.getOccurredAt(),
                LocationSource.MANUAL_CHECKIN
        );

        if (alreadyExists) {
            return;
        }

        LocationHistory history = new LocationHistory();
        history.setAgent(checkIn.getAgent());
        history.setLatitude(checkIn.getLatitude());
        history.setLongitude(checkIn.getLongitude());
        history.setAddress(checkIn.getAddress());
        history.setAccuracy(checkIn.getAccuracy());
        history.setSpeed(checkIn.getSpeed());
        history.setBattery(checkIn.getAgent().getBattery());
        history.setRecordedAt(checkIn.getOccurredAt());
        history.setSource(LocationSource.MANUAL_CHECKIN);
        history.setExternalEventId(null);
        locationHistoryRepository.save(history);
    }

    private boolean shouldCreateLocationHistory(CheckIn checkIn) {
        return checkIn.getLatitude() != null
                && checkIn.getLongitude() != null
                && checkIn.getOccurredAt() != null
                && (checkIn.getAccuracy() == null
                || checkIn.getAccuracy().compareTo(MAX_LOCATION_HISTORY_ACCURACY) <= 0);
    }
}
