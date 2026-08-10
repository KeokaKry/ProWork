package com.worktrcker.app.repository;

import com.worktrcker.app.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByFullNameAndPassword(String fullName, String password);
    
    @EntityGraph(attributePaths = {"geoZones"})
    List<Employee> findAllByOrderByFullNameAsc();
    
    @Query("SELECT DISTINCT e FROM Employee e LEFT JOIN FETCH e.geoZones")
    List<Employee> findAllWithGeoZones();
    
    @Query("SELECT DISTINCT e FROM Employee e LEFT JOIN FETCH e.geoZones WHERE e.id = :id")
    Optional<Employee> findByIdWithGeoZones(Long id);
}
