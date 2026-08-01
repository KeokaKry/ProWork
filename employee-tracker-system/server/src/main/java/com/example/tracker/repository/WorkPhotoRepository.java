package com.example.tracker.repository;

import com.example.tracker.model.WorkPhoto;
import com.example.tracker.model.WorkDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы с фотографиями рабочих дней.
 */
@Repository
public interface WorkPhotoRepository extends JpaRepository<WorkPhoto, Long> {

    /**
     * Поиск всех фотографий рабочего дня.
     * @param workDay Рабочий день
     * @return Список фотографий
     */
    List<WorkPhoto> findByWorkDay(WorkDay workDay);
}
