package com.media4all.tracking.location;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationHistoryRepository extends JpaRepository<LocationHistory, Long> {
}
