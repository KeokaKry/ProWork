package com.worktrcker.app.dto;

import lombok.Data;
import java.util.List;

/**
 * DTO для завершения рабочего дня.
 * Содержит комментарий и список фотографий.
 */
@Data
public class FinishWorkDayRequest {

    /**
     * Комментарий сотрудника о выполненной работе
     */
    private String comment;

    /**
     * Список базовых имен файлов загруженных фотографий
     * (файлы загружаются отдельно через multipart/form-data)
     */
    private List<String> photoFileNames;
}
