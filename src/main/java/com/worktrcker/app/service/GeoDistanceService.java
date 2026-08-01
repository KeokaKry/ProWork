package com.worktrcker.app.service;

import org.springframework.stereotype.Service;

/**
 * Сервис для расчета расстояния между двумя географическими точками.
 * Использует формулу Haversine для точного расчета расстояния на сфере.
 */
@Service
public class GeoDistanceService {

    /**
     * Радиус Земли в метрах (средний)
     */
    private static final double EARTH_RADIUS_METERS = 6371000;

    /**
     * Расчет расстояния между двумя точками по формуле Haversine.
     * 
     * @param lat1 широта первой точки в градусах
     * @param lon1 долгота первой точки в градусах
     * @param lat2 широта второй точки в градусах
     * @param lon2 долгота второй точки в градусах
     * @return расстояние в метрах
     */
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Преобразуем градусы в радианы
        double lat1Rad = Math.toRadians(lat1);
        double lat2Rad = Math.toRadians(lat2);
        double deltaLat = Math.toRadians(lat2 - lat1);
        double deltaLon = Math.toRadians(lon2 - lon1);

        // Формула Haversine
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Расстояние в метрах
        return EARTH_RADIUS_METERS * c;
    }

    /**
     * Проверка, находится ли точка внутри геозоны.
     * 
     * @param pointLat широта проверяемой точки
     * @param pointLon долгота проверяемой точки
     * @param zoneLat широта центра геозоны
     * @param zoneLon долгота центра геозоны
     * @param radiusMeters радиус геозоны в метрах
     * @return true если точка внутри геозоны
     */
    public boolean isPointInZone(double pointLat, double pointLon, 
                                  double zoneLat, double zoneLon, 
                                  int radiusMeters) {
        double distance = calculateDistance(pointLat, pointLon, zoneLat, zoneLon);
        return distance <= radiusMeters;
    }
}
