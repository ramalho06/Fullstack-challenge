package com.media4all.tracking.agent;

import com.media4all.tracking.external.ExternalApiException;
import com.media4all.tracking.external.agent.ExternalAgentDto;
import com.media4all.tracking.external.agent.ExternalAgentGateway;
import com.media4all.tracking.sync.SyncExecution;
import com.media4all.tracking.sync.SyncExecutionRepository;
import com.media4all.tracking.sync.SyncFailureMessage;
import com.media4all.tracking.sync.SyncStatus;
import com.media4all.tracking.sync.SyncType;
import com.media4all.tracking.sync.dto.SyncResultResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;

@Service
public class AgentSyncService {

    private final ExternalAgentGateway externalAgentGateway;
    private final AgentRepository agentRepository;
    private final AgentMapper agentMapper;
    private final SyncExecutionRepository syncExecutionRepository;
    private final TransactionTemplate transactionTemplate;

    public AgentSyncService(
            ExternalAgentGateway externalAgentGateway,
            AgentRepository agentRepository,
            AgentMapper agentMapper,
            SyncExecutionRepository syncExecutionRepository,
            TransactionTemplate transactionTemplate
    ) {
        this.externalAgentGateway = externalAgentGateway;
        this.agentRepository = agentRepository;
        this.agentMapper = agentMapper;
        this.syncExecutionRepository = syncExecutionRepository;
        this.transactionTemplate = transactionTemplate;
    }

    public SyncResultResponse syncAgents() {
        SyncExecution execution = createRunningExecution();

        try {
            List<ExternalAgentDto> externalAgents = externalAgentGateway.fetchAllAgents();
            SyncCounters counters = persistAgents(externalAgents);
            SyncExecution finishedExecution = markSuccess(execution.getId(), counters);
            return SyncResultResponse.from(finishedExecution);
        } catch (RuntimeException exception) {
            SyncExecution failedExecution = markFailed(execution.getId(), exception);

            if (exception instanceof ExternalApiException) {
                throw exception;
            }

            throw new AgentSyncException("Failed to synchronize agents", failedExecution.getId(), exception);
        }
    }

    private SyncExecution createRunningExecution() {
        return transactionTemplate.execute(status -> {
            SyncExecution execution = new SyncExecution();
            execution.setSyncType(SyncType.AGENTS);
            execution.setStatus(SyncStatus.RUNNING);
            execution.setStartedAt(Instant.now());
            execution.setItemsProcessed(0);
            execution.setItemsCreated(0);
            execution.setItemsUpdated(0);
            execution.setItemsSkipped(0);
            return syncExecutionRepository.save(execution);
        });
    }

    private SyncCounters persistAgents(List<ExternalAgentDto> externalAgents) {
        return transactionTemplate.execute(status -> {
            SyncCounters counters = new SyncCounters();

            for (ExternalAgentDto dto : externalAgents) {
                if (!isValid(dto)) {
                    counters.skipped++;
                    continue;
                }

                counters.processed++;
                Agent agent = agentRepository.findByExternalId(dto.externalId()).orElse(null);

                if (agent == null) {
                    agentRepository.save(agentMapper.createFromExternal(dto));
                    counters.created++;
                    continue;
                }

                if (!agent.getId().equals(dto.id())) {
                    throw new IllegalStateException("Agent identity conflict for externalId "
                            + dto.externalId() + ": persisted id=" + agent.getId() + ", external id=" + dto.id());
                }

                agentMapper.updateFromExternal(agent, dto);
                counters.updated++;
            }

            return counters;
        });
    }

    private SyncExecution markSuccess(Long executionId, SyncCounters counters) {
        return transactionTemplate.execute(status -> {
            SyncExecution execution = syncExecutionRepository.findById(executionId)
                    .orElseThrow(() -> new IllegalStateException("SyncExecution not found: " + executionId));

            execution.setStatus(SyncStatus.SUCCESS);
            execution.setFinishedAt(Instant.now());
            execution.setItemsProcessed(counters.processed);
            execution.setItemsCreated(counters.created);
            execution.setItemsUpdated(counters.updated);
            execution.setItemsSkipped(counters.skipped);

            return syncExecutionRepository.save(execution);
        });
    }

    private SyncExecution markFailed(Long executionId, RuntimeException exception) {
        return transactionTemplate.execute(status -> {
            SyncExecution execution = syncExecutionRepository.findById(executionId)
                    .orElseThrow(() -> new IllegalStateException("SyncExecution not found: " + executionId));

            execution.setStatus(SyncStatus.FAILED);
            execution.setFinishedAt(Instant.now());
            execution.setErrorMessage(SyncFailureMessage.resolve(exception, SyncType.AGENTS));

            if (exception instanceof ExternalApiException externalApiException) {
                execution.setHttpStatus(externalApiException.getHttpStatus());
            }

            return syncExecutionRepository.save(execution);
        });
    }

    private boolean isValid(ExternalAgentDto dto) {
        return dto != null
                && StringUtils.hasText(dto.id())
                && StringUtils.hasText(dto.externalId())
                && StringUtils.hasText(dto.name())
                && dto.active() != null
                && dto.status() != null;
    }

    private static class SyncCounters {
        private int processed;
        private int created;
        private int updated;
        private int skipped;
    }
}
