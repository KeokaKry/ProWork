package com.worktrcker.app.repository;

import com.worktrcker.app.entity.WorkDay;
import com.worktrcker.app.entity.WorkPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы с фотографиями рабочих дней.
 */
@Repository
public interface WorkPhotoRepository extends JpaRepository<WorkPhoto, Long> {
    
    /**
     * Найти все фото рабочего дня по объекту WorkDay
     */
    List<WorkPhoto> findByWorkDay(WorkDay workDay);
    
    /**
     * Найти все фото рабочего дня по ID
     */
    List<WorkPhoto> findByWorkDayId(Long workDayId);
}
