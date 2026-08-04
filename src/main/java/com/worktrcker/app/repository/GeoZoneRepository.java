package com.worktrcker.app.repository;

import com.worktrcker.app.model.GeoZone;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GeoZoneRepository extends JpaRepository<GeoZone, Long> {
}