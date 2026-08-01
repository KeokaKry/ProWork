package com.worktrcker.app.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * DTO для ответа о рабочем дне.
 * Содержит всю информацию о рабочем дне сотрудника.
 */
@Data
public class WorkDayResponse {

    private Long id;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private LocalTime lunchStart;
    private LocalTime lunchEnd;
    private List<String> shortBreaks;
    private String comment;
    private String status;
    private Double workedHours;
    private Double penalty;
    private String penaltyReason;
    private List<String> photoUrls; // URL загруженных фотографий
    private String message; // Сообщение для пользователя (например, об ошибке геолокации)
}
