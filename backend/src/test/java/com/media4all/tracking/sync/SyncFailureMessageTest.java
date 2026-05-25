package com.media4all.tracking.sync;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SyncFailureMessageTest {

    @Test
    void usesExceptionMessageWhenPresent() {
        RuntimeException exception = new RuntimeException("External API unavailable");

        String message = SyncFailureMessage.resolve(exception, SyncType.GEOFENCES);

        assertThat(message).isEqualTo("External API unavailable");
    }

    @Test
    void usesFallbackMessageWhenExceptionMessageIsNull() {
        RuntimeException exception = new RuntimeException((String) null);

        String message = SyncFailureMessage.resolve(exception, SyncType.GEOFENCES);

        assertThat(message).isEqualTo("Unexpected error during geofences sync");
    }

    @Test
    void usesFallbackMessageWhenExceptionMessageIsBlank() {
        RuntimeException exception = new RuntimeException(" ");

        String message = SyncFailureMessage.resolve(exception, SyncType.CHECK_INS);

        assertThat(message).isEqualTo("Unexpected error during check-ins sync");
    }
}
