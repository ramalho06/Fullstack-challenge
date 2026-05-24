package com.media4all.tracking.agent;

import com.media4all.tracking.agent.dto.AgentCreateRequest;
import com.media4all.tracking.agent.dto.AgentResponse;
import com.media4all.tracking.agent.dto.AgentUpdateRequest;
import com.media4all.tracking.common.dto.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/agents")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @GetMapping
    public PageResponse<AgentResponse> listAgents(
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) AgentStatus status,
            @RequestParam(required = false) AgentRole role,
            @RequestParam(required = false) String team,
            @RequestParam(required = false) String search,
            Pageable pageable
    ) {
        return PageResponse.from(agentService.findAgents(active, status, role, team, search, pageable));
    }

    @GetMapping("/{id}")
    public AgentResponse getAgent(@PathVariable String id) {
        return agentService.findById(id);
    }

    @PostMapping
    public ResponseEntity<AgentResponse> createAgent(@Valid @RequestBody AgentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(agentService.create(request));
    }

    @PutMapping("/{id}")
    public AgentResponse updateAgent(
            @PathVariable String id,
            @Valid @RequestBody AgentUpdateRequest request
    ) {
        return agentService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAgent(@PathVariable String id) {
        agentService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
