package com.worktrcker.app.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name = "geo_zones")
public class GeoZone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Double latitude;
    private Double longitude;
    private Double radius; // в метрах
}