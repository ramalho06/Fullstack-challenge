package com.media4all.tracking.external.checkin;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExternalCheckInResponse(
        List<ExternalCheckInDto> data
) {
}
