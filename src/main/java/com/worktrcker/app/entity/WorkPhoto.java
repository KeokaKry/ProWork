package com.worktrcker.app.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Сущность "Фотография выполненной работы"
 * Хранит информацию о фото, загруженных сотрудником
 */
@Data
@Entity
@Table(name = "work_photos")
public class WorkPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_day_id", nullable = false)
    private WorkDay workDay;

    /** Имя файла (оригинальное название) */
    @Column(nullable = false)
    private String fileName;

    /** Путь к файлу на сервере */
    @Column(nullable = false)
    private String filePath;

    /** MIME тип файла (image/jpeg, image/png и т.д.) */
    @Column(nullable = false)
    private String contentType;

    /** Размер файла в байтах */
    @Column(nullable = false)
    private Long fileSize;

    /** Дата и время загрузки */
    @Column(updatable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onCreate() {
        uploadedAt = LocalDateTime.now();
    }
}
