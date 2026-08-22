package com.worktrcker.app.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Data
@Table(name = "work_records")
public class WorkRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id")
    @JsonIgnoreProperties({"geoZones", "workRecords", "hibernateLazyInitializer"})
    private Employee employee;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    
    private Double startLat;
    private Double startLon;
    private Double endLat;
    private Double endLon;

    private String status; // ACTIVE, COMPLETED, PENDING
    
    private String dailyTask; // Задание на день от админа
    
    private String reportPhotoUrl; // Ссылка на фотоотчет
    
    // Флаги для уведомлений о перерывах и обеде
    private Boolean lunchNotified; // Уведомление о начале обеда отправлено
    private Boolean lunchEndNotified; // Уведомление о конце обеда отправлено
    private Long lastBreakMinute; // Минута последнего уведомления о начале перерыва
    private Long lastBreakEndMinute; // Минута последнего уведомления о конце перерыва
}
