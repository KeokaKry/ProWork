package com.timetracker.server.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "geo_zones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GeoZone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // Название объекта/офиса

    @Column(nullable = false)
    private Double latitude; // Широта центра

    @Column(nullable = false)
    private Double longitude; // Долгота центра

    @Column(nullable = false)
    private Integer radiusMeters; // Радиус в метрах (например, 50)
}
