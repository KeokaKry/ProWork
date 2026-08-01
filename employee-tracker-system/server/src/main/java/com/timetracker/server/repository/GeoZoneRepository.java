package com.timetracker.server.repository;

import com.timetracker.server.model.GeoZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы с геозонами.
 */
@Repository
public interface GeoZoneRepository extends JpaRepository<GeoZone, Long> {
    /**
     * Получить все геозоны, отсортированные по дате создания.
     */
    List<GeoZone> findAllByOrderByIdDesc();
}
