package com.example.tracker.service;

import org.springframework.stereotype.Service;

/**
 * Сервис для расчета расстояний между GPS координатами.
 * Использует формулу Haversine для вычисления расстояния на сфере.
 */
@Service
public class GeoDistanceService {

    /**
     * Расчет расстояния между двумя точками на Земле по их GPS координатам.
     * @param lat1 Широта первой точки
     * @param lon1 Долгота первой точки
     * @param lat2 Широта второй точки
     * @param lon2 Долгота второй точки
     * @return Расстояние в метрах
     */
    public double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        final int R = 6371000; // Радиус Земли в метрах

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }
}
