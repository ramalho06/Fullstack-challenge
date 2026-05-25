package com.media4all.tracking.sync;

import org.springframework.util.StringUtils;

import java.util.Locale;

public final class SyncFailureMessage {

    private SyncFailureMessage() {
    }

    public static String resolve(RuntimeException exception, SyncType syncType) {
        if (StringUtils.hasText(exception.getMessage())) {
            return exception.getMessage();
        }

        return "Unexpected error during " + syncType.name().toLowerCase(Locale.ROOT).replace('_', '-') + " sync";
    }
}
