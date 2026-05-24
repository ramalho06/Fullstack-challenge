package com.media4all.tracking.checkin;

import com.media4all.tracking.checkin.dto.CheckInCreateRequest;
import com.media4all.tracking.checkin.dto.CheckInResponse;
import com.media4all.tracking.common.dto.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/check-ins")
public class CheckInController {

    private final CheckInQueryService checkInQueryService;
    private final ManualCheckInService manualCheckInService;

    public CheckInController(CheckInQueryService checkInQueryService, ManualCheckInService manualCheckInService) {
        this.checkInQueryService = checkInQueryService;
        this.manualCheckInService = manualCheckInService;
    }

    @GetMapping
    public PageResponse<CheckInResponse> listCheckIns(
            @RequestParam(required = false) String agentId,
            @RequestParam(required = false) CheckInType type,
            @RequestParam(required = false) CheckInSource source,
            Pageable pageable
    ) {
        return PageResponse.from(checkInQueryService.findCheckIns(agentId, type, source, pageable));
    }

    @PostMapping
    public ResponseEntity<CheckInResponse> createManualCheckIn(@Valid @RequestBody CheckInCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(manualCheckInService.createManualCheckIn(request));
    }
}
