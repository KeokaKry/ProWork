package com.timetracker.server.service;

import org.springframework.stereotype.Service;

@Service
public class GeoDistanceService {

    /**
     * Расчет расстояния между двумя точками по формуле Haversine
     * @param lat1 широта точки 1
     * @param lon1 долгота точки 1
     * @param lat2 широта точки 2
     * @param lon2 долгота точки 2
     * @return расстояние в метрах
     */
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371000; // Радиус Земли в метрах

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    /**
     * Проверка находится ли точка внутри геозоны
     * @param userLat широта пользователя
     * @param userLon долгота пользователя
     * @param zoneLat широта центра зоны
     * @param zoneLon долгота центра зоны
     * @param radiusMeters радиус зоны в метрах
     * @return true если пользователь внутри зоны
     */
    public boolean isInsideGeoZone(double userLat, double userLon, 
                                   double zoneLat, double zoneLon, 
                                   int radiusMeters) {
        double distance = calculateDistance(userLat, userLon, zoneLat, zoneLon);
        return distance <= radiusMeters;
    }
}
