package com.media4all.tracking.external.location;

import java.util.List;

public interface ExternalLocationGateway {

    List<ExternalLocationDto> fetchAllLocations();
}
