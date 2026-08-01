package com.example.tracker.service;

import com.example.tracker.model.GeoZone;
import com.example.tracker.repository.GeoZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Сервис для управления геозонами.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class GeoZoneService {

    private final GeoZoneRepository geoZoneRepository;

    /**
     * Создание новой геозоны.
     * @param name Название
     * @param latitude Широта
     * @param longitude Долгота
     * @param radiusMeters Радиус в метрах
     * @return Созданная геозона
     */
    public GeoZone createGeoZone(String name, Double latitude, Double longitude, Integer radiusMeters) {
        GeoZone geoZone = new GeoZone();
        geoZone.setName(name);
        geoZone.setLatitude(latitude);
        geoZone.setLongitude(longitude);
        geoZone.setRadiusMeters(radiusMeters);
        return geoZoneRepository.save(geoZone);
    }

    /**
     * Получение всех геозон.
     * @return Список геозон
     */
    public List<GeoZone> getAllGeoZones() {
        return geoZoneRepository.findAll();
    }

    /**
     * Удаление геозоны по ID.
     * @param id ID геозоны
     */
    public void deleteGeoZone(Long id) {
        geoZoneRepository.deleteById(id);
    }

    /**
     * Поиск геозоны по ID.
     * @param id ID геозоны
     * @return Найденная геозона
     */
    public GeoZone findById(Long id) {
        return geoZoneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Геозона не найдена"));
    }
}
