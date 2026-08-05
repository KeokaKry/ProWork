package com.worktrcker.app.repository;

import com.worktrcker.app.model.GeoZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface GeoZoneRepository extends JpaRepository<GeoZone, Long> {
    
    @Query("SELECT DISTINCT g FROM GeoZone g")
    @EntityGraph(attributePaths = {"employees"})
    List<GeoZone> findAllWithEmployees();
}