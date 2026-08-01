package com.timetracker.server.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * DTO для наложения штрафа на сотрудника.
 * Используется администратором для штрафа за конкретный день.
 */
@Data
public class FineDto {
    /**
     * Дата, за которую накладывается штраф.
     */
    private LocalDate date;
    
    /**
     * Сумма штрафа в рублях.
     */
    private Double amount;
    
    /**
     * Причина штрафа (например, "Опоздание", "Нарушение техники безопасности").
     */
    private String reason;
}
