package com.media4all.tracking.agent.dto;

import com.media4all.tracking.agent.AgentRole;
import com.media4all.tracking.agent.AgentStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AgentCreateRequest(
        @NotBlank
        @Size(max = 150)
        String name,

        AgentRole role,

        @Size(max = 100)
        String team,

        @Size(max = 30)
        String phone,

        @Email
        @Size(max = 150)
        String email,

        Boolean active,

        AgentStatus status
) {
}
