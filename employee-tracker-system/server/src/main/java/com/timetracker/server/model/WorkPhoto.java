package com.timetracker.server.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Сущность Фотография выполненной работы.
 * Хранит информацию о загруженных сотрудником фотографиях.
 */
@Entity
@Data
@Table(name = "work_photos")
public class WorkPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Рабочий день, к которому относится фотография.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_day_id", nullable = false)
    private WorkDay workDay;

    /**
     * Имя файла фотографии (путь на сервере).
     */
    @Column(nullable = false)
    private String fileName;

    /**
     * MIME-тип файла (например, "image/jpeg").
     */
    @Column(nullable = false)
    private String contentType;

    /**
     * Размер файла в байтах.
     */
    @Column(nullable = false)
    private Long fileSize;

    /**
     * Дата и время загрузки фотографии.
     */
    @Column(nullable = false)
    private LocalDateTime uploadTime;
}
