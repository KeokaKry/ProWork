package com.example.tracker.repository;

import com.example.tracker.model.GeoZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для работы с геозонами в базе данных.
 */
@Repository
public interface GeoZoneRepository extends JpaRepository<GeoZone, Long> {
}
