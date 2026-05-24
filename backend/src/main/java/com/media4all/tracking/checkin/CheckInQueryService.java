package com.media4all.tracking.checkin;

import com.media4all.tracking.checkin.dto.CheckInResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class CheckInQueryService {

    private final CheckInRepository checkInRepository;
    private final CheckInMapper checkInMapper;

    public CheckInQueryService(CheckInRepository checkInRepository, CheckInMapper checkInMapper) {
        this.checkInRepository = checkInRepository;
        this.checkInMapper = checkInMapper;
    }

    @Transactional(readOnly = true)
    public Page<CheckInResponse> findCheckIns(
            String agentId,
            CheckInType type,
            CheckInSource source,
            Pageable pageable
    ) {
        return checkInRepository.findAll(withFilters(agentId, type, source), pageable)
                .map(checkInMapper::toResponse);
    }

    private Specification<CheckIn> withFilters(String agentId, CheckInType type, CheckInSource source) {
        return Specification.where(agentEquals(agentId))
                .and(typeEquals(type))
                .and(sourceEquals(source));
    }

    private Specification<CheckIn> agentEquals(String agentId) {
        return (root, query, criteriaBuilder) ->
                StringUtils.hasText(agentId) ? criteriaBuilder.equal(root.get("agent").get("id"), agentId) : null;
    }

    private Specification<CheckIn> typeEquals(CheckInType type) {
        return (root, query, criteriaBuilder) ->
                type == null ? null : criteriaBuilder.equal(root.get("type"), type);
    }

    private Specification<CheckIn> sourceEquals(CheckInSource source) {
        return (root, query, criteriaBuilder) ->
                source == null ? null : criteriaBuilder.equal(root.get("source"), source);
    }
}
