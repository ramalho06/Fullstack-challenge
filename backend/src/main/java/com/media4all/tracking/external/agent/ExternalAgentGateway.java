package com.media4all.tracking.external.agent;

import java.util.List;

public interface ExternalAgentGateway {

    List<ExternalAgentDto> fetchAllAgents();
}
