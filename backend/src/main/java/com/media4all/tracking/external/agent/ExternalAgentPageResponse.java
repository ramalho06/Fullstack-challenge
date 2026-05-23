package com.media4all.tracking.external.agent;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ExternalAgentPageResponse(
        List<ExternalAgentDto> data
) {
}
