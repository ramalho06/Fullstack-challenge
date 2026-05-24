package com.media4all.tracking.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.schedulers")
public class SchedulerProperties {

    private boolean enabled = true;
    private long agentsFixedDelayMs = 600000;
    private long agentsInitialDelayMs = 30000;
    private long locationsFixedDelayMs = 60000;
    private long locationsInitialDelayMs = 45000;
    private long checkInsFixedDelayMs = 120000;
    private long checkInsInitialDelayMs = 60000;
    private long geofencesFixedDelayMs = 1800000;
    private long geofencesInitialDelayMs = 90000;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getAgentsFixedDelayMs() {
        return agentsFixedDelayMs;
    }

    public void setAgentsFixedDelayMs(long agentsFixedDelayMs) {
        this.agentsFixedDelayMs = agentsFixedDelayMs;
    }

    public long getAgentsInitialDelayMs() {
        return agentsInitialDelayMs;
    }

    public void setAgentsInitialDelayMs(long agentsInitialDelayMs) {
        this.agentsInitialDelayMs = agentsInitialDelayMs;
    }

    public long getLocationsFixedDelayMs() {
        return locationsFixedDelayMs;
    }

    public void setLocationsFixedDelayMs(long locationsFixedDelayMs) {
        this.locationsFixedDelayMs = locationsFixedDelayMs;
    }

    public long getLocationsInitialDelayMs() {
        return locationsInitialDelayMs;
    }

    public void setLocationsInitialDelayMs(long locationsInitialDelayMs) {
        this.locationsInitialDelayMs = locationsInitialDelayMs;
    }

    public long getCheckInsFixedDelayMs() {
        return checkInsFixedDelayMs;
    }

    public void setCheckInsFixedDelayMs(long checkInsFixedDelayMs) {
        this.checkInsFixedDelayMs = checkInsFixedDelayMs;
    }

    public long getCheckInsInitialDelayMs() {
        return checkInsInitialDelayMs;
    }

    public void setCheckInsInitialDelayMs(long checkInsInitialDelayMs) {
        this.checkInsInitialDelayMs = checkInsInitialDelayMs;
    }

    public long getGeofencesFixedDelayMs() {
        return geofencesFixedDelayMs;
    }

    public void setGeofencesFixedDelayMs(long geofencesFixedDelayMs) {
        this.geofencesFixedDelayMs = geofencesFixedDelayMs;
    }

    public long getGeofencesInitialDelayMs() {
        return geofencesInitialDelayMs;
    }

    public void setGeofencesInitialDelayMs(long geofencesInitialDelayMs) {
        this.geofencesInitialDelayMs = geofencesInitialDelayMs;
    }
}
