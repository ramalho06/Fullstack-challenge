package com.media4all.tracking.agent.dto;

import com.media4all.tracking.agent.AgentRole;
import com.media4all.tracking.agent.AgentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados editáveis de um agente")
public record AgentUpdateRequest(
        @Schema(description = "Nome do agente", example = "Agente Atualizado")
        @NotBlank
        @Size(max = 150)
        String name,

        @Schema(description = "Função operacional do agente", example = "INSTALLER")
        AgentRole role,

        @Schema(description = "Equipe do agente", example = "Beta")
        @Size(max = 100)
        String team,

        @Schema(description = "Telefone de contato", example = "+5511888888888")
        @Size(max = 30)
        String phone,

        @Schema(description = "E-mail de contato", example = "agente.atualizado@example.com")
        @Email
        @Size(max = 150)
        String email,

        @Schema(description = "Indica se o agente está ativo", example = "true")
        @NotNull
        Boolean active,

        @Schema(description = "Status operacional editável manualmente", example = "ONLINE")
        @NotNull
        AgentStatus status
) {
}
