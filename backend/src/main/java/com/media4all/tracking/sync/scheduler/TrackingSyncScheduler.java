package com.media4all.tracking.sync.scheduler;

import com.media4all.tracking.agent.AgentSyncService;
import com.media4all.tracking.checkin.CheckInSyncService;
import com.media4all.tracking.config.SchedulerProperties;
import com.media4all.tracking.geofence.GeofenceSyncService;
import com.media4all.tracking.location.LocationSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class TrackingSyncScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrackingSyncScheduler.class);

    private final SchedulerProperties schedulerProperties;
    private final AgentSyncService agentSyncService;
    private final LocationSyncService locationSyncService;
    private final CheckInSyncService checkInSyncService;
    private final GeofenceSyncService geofenceSyncService;

    private final AtomicBoolean agentsRunning = new AtomicBoolean(false);
    private final AtomicBoolean locationsRunning = new AtomicBoolean(false);
    private final AtomicBoolean checkInsRunning = new AtomicBoolean(false);
    private final AtomicBoolean geofencesRunning = new AtomicBoolean(false);

    public TrackingSyncScheduler(
            SchedulerProperties schedulerProperties,
            AgentSyncService agentSyncService,
            LocationSyncService locationSyncService,
            CheckInSyncService checkInSyncService,
            GeofenceSyncService geofenceSyncService
    ) {
        this.schedulerProperties = schedulerProperties;
        this.agentSyncService = agentSyncService;
        this.locationSyncService = locationSyncService;
        this.checkInSyncService = checkInSyncService;
        this.geofenceSyncService = geofenceSyncService;
    }

    @Scheduled(
            fixedDelayString = "${app.schedulers.agents-fixed-delay-ms}",
            initialDelayString = "${app.schedulers.agents-initial-delay-ms}"
    )
    public void scheduleAgentsSync() {
        runIfEnabledAndNotRunning("agents", agentsRunning, agentSyncService::syncAgents);
    }

    @Scheduled(
            fixedDelayString = "${app.schedulers.locations-fixed-delay-ms}",
            initialDelayString = "${app.schedulers.locations-initial-delay-ms}"
    )
    public void scheduleLocationsSync() {
        runIfEnabledAndNotRunning("locations", locationsRunning, locationSyncService::syncLocations);
    }

    @Scheduled(
            fixedDelayString = "${app.schedulers.check-ins-fixed-delay-ms}",
            initialDelayString = "${app.schedulers.check-ins-initial-delay-ms}"
    )
    public void scheduleCheckInsSync() {
        runIfEnabledAndNotRunning("check-ins", checkInsRunning, checkInSyncService::syncCheckIns);
    }

    @Scheduled(
            fixedDelayString = "${app.schedulers.geofences-fixed-delay-ms}",
            initialDelayString = "${app.schedulers.geofences-initial-delay-ms}"
    )
    public void scheduleGeofencesSync() {
        runIfEnabledAndNotRunning("geofences", geofencesRunning, geofenceSyncService::syncGeofences);
    }

    private void runIfEnabledAndNotRunning(String syncName, AtomicBoolean runningFlag, Runnable syncTask) {
        if (!schedulerProperties.isEnabled()) {
            LOGGER.debug("Skipping {} sync scheduler because schedulers are disabled", syncName);
            return;
        }

        if (!runningFlag.compareAndSet(false, true)) {
            LOGGER.info("Skipping {} sync scheduler because a previous execution is still running", syncName);
            return;
        }

        try {
            LOGGER.info("Starting scheduled {} sync", syncName);
            syncTask.run();
            LOGGER.info("Finished scheduled {} sync", syncName);
        } catch (Exception exception) {
            LOGGER.error("Scheduled {} sync failed", syncName, exception);
        } finally {
            runningFlag.set(false);
        }
    }
}
