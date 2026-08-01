package com.worktrcker.app.repository;

import com.worktrcker.app.entity.GeoZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы с геозонами.
 * Предоставляет методы для поиска активных геозон.
 */
@Repository
public interface GeoZoneRepository extends JpaRepository<GeoZone, Long> {
    
    /**
     * Найти все активные геозоны
     */
    List<GeoZone> findByActiveTrue();
}
