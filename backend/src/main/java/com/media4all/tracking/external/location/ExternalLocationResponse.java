package com.media4all.tracking.external.location;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExternalLocationResponse(
        List<ExternalLocationDto> data
) {
}
