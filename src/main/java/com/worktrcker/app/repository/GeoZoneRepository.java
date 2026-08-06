package com.worktrcker.app.repository;

import com.worktrcker.app.model.GeoZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import java.util.List;

public interface GeoZoneRepository extends JpaRepository<GeoZone, Long> {
    
    @Query("SELECT gz FROM GeoZone gz LEFT JOIN FETCH gz.employees")
    List<GeoZone> findAllWithEmployees();
}
