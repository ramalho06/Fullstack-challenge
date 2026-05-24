package com.media4all.tracking.agent.dto;

import com.media4all.tracking.agent.AgentRole;
import com.media4all.tracking.agent.AgentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para criação manual/local de agente")
public record AgentCreateRequest(
        @Schema(description = "Nome do agente", example = "Agente Local")
        @NotBlank
        @Size(max = 150)
        String name,

        @Schema(description = "Função operacional do agente", example = "TECHNICIAN")
        AgentRole role,

        @Schema(description = "Equipe do agente", example = "Alpha")
        @Size(max = 100)
        String team,

        @Schema(description = "Telefone de contato", example = "+5511999999999")
        @Size(max = 30)
        String phone,

        @Schema(description = "E-mail de contato", example = "agente.local@example.com")
        @Email
        @Size(max = 150)
        String email,

        @Schema(description = "Indica se o agente está ativo. Se omitido, usa true.", example = "true")
        Boolean active,

        @Schema(description = "Status operacional inicial. Se omitido, usa OFFLINE.", example = "OFFLINE")
        AgentStatus status
) {
}
