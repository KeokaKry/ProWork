package com.worktrcker.app.repository;

import com.worktrcker.app.model.GeoZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.List;

public interface GeoZoneRepository extends JpaRepository<GeoZone, Long> {
    
    @EntityGraph(attributePaths = {"employees"})
    List<GeoZone> findAllWithEmployees();
}