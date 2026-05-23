package com.media4all.tracking.external.checkin;

import java.util.List;

public interface ExternalCheckInGateway {

    List<ExternalCheckInDto> fetchAllCheckIns(String syncToken);
}
