package com.worktrcker.app.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Data
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    @Column(unique = true, nullable = false)
    private String phone;

    @Column(nullable = false)
    private String password; // Пароль выдает админ

    @ManyToOne
    @JoinColumn(name = "position_id")
    private Position position;

    @ManyToMany
    @JoinTable(
        name = "employee_geo_zones",
        joinColumns = @JoinColumn(name = "employee_id"),
        inverseJoinColumns = @JoinColumn(name = "geo_zone_id")
    )
    @JsonIgnoreProperties("employees")
    private List<GeoZone> geoZones;

    private Double startLatitude;
    private Double startLongitude;

    @OneToMany(mappedBy = "employee")
    @JsonIgnore
    private List<WorkRecord> workRecords;
}