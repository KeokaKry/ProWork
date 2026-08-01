package com.example.tracker.service;

import com.example.tracker.model.Position;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сервис для управления должностями.
 * В упрощенной версии хранит должности в памяти (ConcurrentHashMap).
 * В реальной системе нужно использовать базу данных.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PositionService {

    // Хранилище должностей в памяти (для демонстрации)
    private static final ConcurrentHashMap<Long, Position> positionsStore = new ConcurrentHashMap<>();
    private static Long nextId = 1L;

    /**
     * Создание новой должности.
     * @param name Название должности
     * @param hourlyRate Ставка за час
     * @return Созданная должность
     */
    public Position createPosition(String name, Double hourlyRate) {
        Position position = new Position();
        position.setId(nextId++);
        position.setName(name);
        position.setHourlyRate(hourlyRate);
        positionsStore.put(position.getId(), position);
        return position;
    }

    /**
     * Получение всех должностей.
     * @return Список должностей
     */
    public List<Position> getAllPositions() {
        return new ArrayList<>(positionsStore.values());
    }

    /**
     * Удаление должности по ID.
     * @param id ID должности
     */
    public void deletePosition(Long id) {
        positionsStore.remove(id);
    }

    /**
     * Обновление ставки должности.
     * @param id ID должности
     * @param hourlyRate Новая ставка
     * @return Обновленная должность
     */
    public Position updatePositionRate(Long id, Double hourlyRate) {
        Position position = positionsStore.get(id);
        if (position == null) {
            throw new RuntimeException("Должность не найдена");
        }
        position.setHourlyRate(hourlyRate);
        return position;
    }

    /**
     * Поиск должности по ID.
     * @param id ID должности
     * @return Найденная должность
     */
    public Position findById(Long id) {
        return positionsStore.getOrDefault(id, null);
    }
}
