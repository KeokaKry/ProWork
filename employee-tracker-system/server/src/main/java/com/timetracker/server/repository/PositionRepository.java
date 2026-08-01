package com.timetracker.server.repository;

import com.timetracker.server.model.Position;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для работы с должностями.
 */
@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {

    /**
     * Поиск должности по названию.
     */
    Optional<Position> findByName(String name);

    /**
     * Проверка существования должности по названию.
     */
    boolean existsByName(String name);
}
