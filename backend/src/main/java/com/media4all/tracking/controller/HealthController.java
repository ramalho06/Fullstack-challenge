package com.media4all.tracking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Controller de health check da aplicação.
 *
 * Endpoint simples que confirma que a API está no ar e respondendo.
 * Útil para:
 * - Verificação rápida durante o desenvolvimento
 * - Health checks do Docker / Kubernetes
 * - Monitoramento básico
 *
 * O prefixo /api é usado para separar endpoints da API de possíveis
 * recursos estáticos ou páginas, seguindo uma convenção comum na indústria.
 */
@RestController
@RequestMapping("/api")
public class HealthController {

    /**
     * GET /api/health
     *
     * Retorna o status da aplicação com um timestamp.
     * O uso de Map<String, Object> aqui é intencional por ser um endpoint
     * simples e de infraestrutura. Para endpoints de domínio (agentes, check-ins),
     * usaremos DTOs tipados (records) quando implementarmos o CRUD.
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = Map.of(
                "status", "UP",
                "timestamp", LocalDateTime.now().toString(),
                "service", "Teams Tracking API"
        );

        return ResponseEntity.ok(response);
    }
}
