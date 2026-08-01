package com.example.tracker.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * Модель фотографии выполненной работы.
 */
@Entity
@Table(name = "work_photos")
@Data
public class WorkPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Рабочий день, к которому относится фотография
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_day_id", nullable = false)
    private WorkDay workDay;

    /**
     * Путь к файлу на сервере
     */
    @Column(nullable = false)
    private String filePath;

    /**
     * Тип контента (image/jpeg, image/png и т.д.)
     */
    @Column(nullable = false)
    private String contentType;

    /**
     * Дата и время загрузки фотографии
     */
    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    /**
     * Получение данных изображения как массив байтов.
     * Используется для отправки клиенту.
     */
    @Transient
    public byte[] getImageData() {
        try {
            return java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(filePath));
        } catch (Exception e) {
            return new byte[0];
        }
    }
}
