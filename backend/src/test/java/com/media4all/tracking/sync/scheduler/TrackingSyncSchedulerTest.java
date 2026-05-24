package com.media4all.tracking.sync.scheduler;

import com.media4all.tracking.agent.AgentSyncService;
import com.media4all.tracking.checkin.CheckInSyncService;
import com.media4all.tracking.config.SchedulerProperties;
import com.media4all.tracking.geofence.GeofenceSyncService;
import com.media4all.tracking.location.LocationSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class TrackingSyncSchedulerTest {

    private SchedulerProperties schedulerProperties;
    private AgentSyncServiceFake agentSyncService;
    private LocationSyncServiceFake locationSyncService;
    private CheckInSyncServiceFake checkInSyncService;
    private GeofenceSyncServiceFake geofenceSyncService;
    private TrackingSyncScheduler scheduler;

    @BeforeEach
    void setUp() {
        schedulerProperties = new SchedulerProperties();
        agentSyncService = new AgentSyncServiceFake();
        locationSyncService = new LocationSyncServiceFake();
        checkInSyncService = new CheckInSyncServiceFake();
        geofenceSyncService = new GeofenceSyncServiceFake();
        scheduler = new TrackingSyncScheduler(
                schedulerProperties,
                agentSyncService,
                locationSyncService,
                checkInSyncService,
                geofenceSyncService
        );
    }

    @Test
    void doesNotCallServicesWhenSchedulersAreDisabled() {
        schedulerProperties.setEnabled(false);

        scheduler.scheduleAgentsSync();
        scheduler.scheduleLocationsSync();
        scheduler.scheduleCheckInsSync();
        scheduler.scheduleGeofencesSync();

        assertThat(agentSyncService.calls).isZero();
        assertThat(locationSyncService.calls).isZero();
        assertThat(checkInSyncService.calls).isZero();
        assertThat(geofenceSyncService.calls).isZero();
    }

    @Test
    void callsAgentSyncServiceWhenEnabled() {
        scheduler.scheduleAgentsSync();

        assertThat(agentSyncService.calls).isEqualTo(1);
    }

    @Test
    void callsLocationSyncServiceWhenEnabled() {
        scheduler.scheduleLocationsSync();

        assertThat(locationSyncService.calls).isEqualTo(1);
    }

    @Test
    void callsCheckInSyncServiceWhenEnabled() {
        scheduler.scheduleCheckInsSync();

        assertThat(checkInSyncService.calls).isEqualTo(1);
    }

    @Test
    void callsGeofenceSyncServiceWhenEnabled() {
        scheduler.scheduleGeofencesSync();

        assertThat(geofenceSyncService.calls).isEqualTo(1);
    }

    @Test
    void skipsSameSchedulerWhenPreviousExecutionIsStillRunning() {
        agentSyncService.onRun = scheduler::scheduleAgentsSync;

        scheduler.scheduleAgentsSync();

        assertThat(agentSyncService.calls).isEqualTo(1);
    }

    @Test
    void catchesServiceExceptionAndDoesNotPropagate() {
        agentSyncService.throwOnCall = true;

        assertThatCode(() -> scheduler.scheduleAgentsSync()).doesNotThrowAnyException();
        assertThat(agentSyncService.calls).isEqualTo(1);
    }

    @Test
    void releasesRunningFlagAfterException() {
        agentSyncService.throwOnCall = true;
        scheduler.scheduleAgentsSync();

        agentSyncService.throwOnCall = false;
        scheduler.scheduleAgentsSync();

        assertThat(agentSyncService.calls).isEqualTo(2);
    }

    private static class AgentSyncServiceFake extends AgentSyncService {
        private int calls;
        private boolean throwOnCall;
        private Runnable onRun;

        AgentSyncServiceFake() {
            super(null, null, null, null, null);
        }

        @Override
        public com.media4all.tracking.sync.dto.SyncResultResponse syncAgents() {
            calls++;

            if (onRun != null) {
                onRun.run();
            }

            if (throwOnCall) {
                throw new IllegalStateException("agents failed");
            }

            return null;
        }
    }

    private static class LocationSyncServiceFake extends LocationSyncService {
        private int calls;

        LocationSyncServiceFake() {
            super(null, null, null, null, null, null);
        }

        @Override
        public com.media4all.tracking.sync.dto.SyncResultResponse syncLocations() {
            calls++;
            return null;
        }
    }

    private static class CheckInSyncServiceFake extends CheckInSyncService {
        private int calls;

        CheckInSyncServiceFake() {
            super(null, null, null, null, null, null, null, null);
        }

        @Override
        public com.media4all.tracking.sync.dto.SyncResultResponse syncCheckIns() {
            calls++;
            return null;
        }
    }

    private static class GeofenceSyncServiceFake extends GeofenceSyncService {
        private int calls;

        GeofenceSyncServiceFake() {
            super(null, null, null, null, null);
        }

        @Override
        public com.media4all.tracking.sync.dto.SyncResultResponse syncGeofences() {
            calls++;
            return null;
        }
    }
}
