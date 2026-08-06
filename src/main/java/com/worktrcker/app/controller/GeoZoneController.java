package com.worktrcker.app.controller;

import com.worktrcker.app.model.GeoZone;
import com.worktrcker.app.model.Employee;
import com.worktrcker.app.repository.GeoZoneRepository;
import com.worktrcker.app.repository.EmployeeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/geozones")
@CrossOrigin(origins = "*")
public class GeoZoneController {

    @Autowired
    private GeoZoneRepository geoZoneRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;

    @GetMapping
    public ResponseEntity<List<GeoZone>> getAllGeoZones() {
        return ResponseEntity.ok(geoZoneRepository.findAllWithEmployees());
    }

    @PostMapping
    public ResponseEntity<GeoZone> createGeoZone(@RequestBody GeoZone geoZone) {
        return ResponseEntity.ok(geoZoneRepository.save(geoZone));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GeoZone> updateGeoZone(@PathVariable Long id, @RequestBody GeoZone geoZone) {
        return geoZoneRepository.findById(id)
            .map(existing -> {
                existing.setName(geoZone.getName());
                existing.setLatitude(geoZone.getLatitude());
                existing.setLongitude(geoZone.getLongitude());
                existing.setRadius(geoZone.getRadius());
                return ResponseEntity.ok(geoZoneRepository.save(existing));
            })
            .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGeoZone(@PathVariable Long id) {
        geoZoneRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
    
    // Назначить геозону сотруднику
    @PostMapping("/{geoZoneId}/assign/{employeeId}")
    public ResponseEntity<Employee> assignGeoZoneToEmployee(@PathVariable Long geoZoneId, @PathVariable Long employeeId) {
        return geoZoneRepository.findById(geoZoneId)
            .flatMap(geoZone -> employeeRepository.findById(employeeId).map(employee -> {
                if (employee.getGeoZones() == null) {
                    employee.setGeoZones(new java.util.ArrayList<>());
                }
                if (!employee.getGeoZones().contains(geoZone)) {
                    employee.getGeoZones().add(geoZone);
                }
                return ResponseEntity.ok(employeeRepository.save(employee));
            }))
            .orElse(ResponseEntity.notFound().build());
    }
    
    // Удалить геозону у сотрудника
    @DeleteMapping("/{geoZoneId}/unassign/{employeeId}")
    public ResponseEntity<Employee> unassignGeoZoneFromEmployee(@PathVariable Long geoZoneId, @PathVariable Long employeeId) {
        return geoZoneRepository.findById(geoZoneId)
            .flatMap(geoZone -> employeeRepository.findById(employeeId).map(employee -> {
                if (employee.getGeoZones() != null) {
                    employee.getGeoZones().remove(geoZone);
                }
                return ResponseEntity.ok(employeeRepository.save(employee));
            }))
            .orElse(ResponseEntity.notFound().build());
    }
}
