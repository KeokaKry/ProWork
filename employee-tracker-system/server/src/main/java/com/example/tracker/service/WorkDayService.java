package com.example.tracker.service;

import com.example.tracker.model.*;
import com.example.tracker.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для управления рабочими днями.
 * Обрабатывает начало/конец рабочего дня, перерывы, загрузку фото.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class WorkDayService {

    private final WorkDayRepository workDayRepository;
    private final EmployeeRepository employeeRepository;
    private final WorkPhotoRepository workPhotoRepository;

    // Директория для сохранения фотографий
    private static final String UPLOAD_DIR = "uploads/work-photos/";

    /**
     * Начало рабочего дня для сотрудника.
     * @param employee Сотрудник
     * @return Созданный рабочий день
     */
    public WorkDay startWorkDay(Employee employee) {
        // Проверяем, нет ли уже активной смены сегодня
        Optional<WorkDay> existingDay = workDayRepository.findByEmployeeIdAndDateAndEndTimeIsNull(
            employee.getId(), 
            LocalDate.now()
        );
        
        if (existingDay.isPresent()) {
            throw new RuntimeException("У вас уже есть активная смена сегодня!");
        }

        WorkDay workDay = new WorkDay();
        workDay.setEmployee(employee);
        workDay.setDate(LocalDate.now());
        workDay.setStartTime(LocalDateTime.now());

        return workDayRepository.save(workDay);
    }

    /**
     * Завершение рабочего дня с комментарием.
     * @param workDayId ID рабочего дня
     * @param comment Комментарий о проделанной работе
     * @return Обновленный рабочий день
     */
    public WorkDay endWorkDay(Long workDayId, String comment) {
        WorkDay workDay = workDayRepository.findById(workDayId)
                .orElseThrow(() -> new RuntimeException("Рабочий день не найден"));

        workDay.setEndTime(LocalDateTime.now());
        workDay.setComment(comment);

        return workDayRepository.save(workDay);
    }

    /**
     * Добавление фотографии к рабочему дню.
     * @param workDayId ID рабочего дня
     * @param imageData Байты изображения
     * @param contentType Тип контента (image/jpeg, image/png и т.д.)
     * @return Сохраненная фотография
     */
    public WorkPhoto addWorkPhoto(Long workDayId, byte[] imageData, String contentType) throws IOException {
        WorkDay workDay = workDayRepository.findById(workDayId)
                .orElseThrow(() -> new RuntimeException("Рабочий день не найден"));

        // Генерируем уникальное имя файла
        String fileName = UUID.randomUUID() + "_" + System.currentTimeMillis() + "." + getExtension(contentType);
        Path path = Paths.get(UPLOAD_DIR + fileName);
        
        // Создаем директорию если не существует
        Files.createDirectories(path.getParent());
        
        // Сохраняем файл
        Files.write(path, imageData);

        WorkPhoto photo = new WorkPhoto();
        photo.setWorkDay(workDay);
        photo.setFilePath(path.toString());
        photo.setContentType(contentType);
        photo.setUploadedAt(LocalDateTime.now());

        return workPhotoRepository.save(photo);
    }

    /**
     * Поиск рабочего дня по ID.
     * @param id ID рабочего дня
     * @return Найденный рабочий день или пустой Optional
     */
    public Optional<WorkDay> findById(Long id) {
        return workDayRepository.findById(id);
    }

    /**
     * Получение текущего активного рабочего дня сотрудника.
     * @param employeeId ID сотрудника
     * @return Активный рабочий день или null
     */
    public WorkDay getCurrentWorkDay(Long employeeId) {
        return workDayRepository.findByEmployeeIdAndDateAndEndTimeIsNull(employeeId, LocalDate.now())
                .orElse(null);
    }

    /**
     * Назначение штрафа рабочему дню.
     * @param employeeId ID сотрудника
     * @param date Дата рабочего дня
     * @param amount Сумма штрафа
     * @param reason Причина штрафа
     * @return Обновленный рабочий день
     */
    public WorkDay assignFine(Long employeeId, LocalDate date, Double amount, String reason) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Сотрудник не найден"));

        WorkDay workDay = workDayRepository.findByEmployeeAndDate(employee, date)
                .orElseThrow(() -> new RuntimeException("Рабочий день не найден"));

        workDay.setPenalty(amount);
        return workDayRepository.save(workDay);
    }

    /**
     * Получение рабочих дней сотрудника за период.
     * @param employeeId ID сотрудника
     * @param startDate Дата начала
     * @param endDate Дата окончания
     * @return Список рабочих дней
     */
    public List<WorkDay> getWorkDaysByPeriod(Long employeeId, LocalDate startDate, LocalDate endDate) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Сотрудник не найден"));

        return workDayRepository.findByEmployeeAndDateBetween(employee, startDate, endDate);
    }

    /**
     * Получение всех рабочих дней за период.
     * @param startDate Дата начала
     * @param endDate Дата окончания
     * @return Список рабочих дней
     */
    public List<WorkDay> getAllWorkDaysByPeriod(LocalDate startDate, LocalDate endDate) {
        return workDayRepository.findByDateBetween(startDate, endDate);
    }

    /**
     * Получение фотографий рабочего дня.
     * @param workDayId ID рабочего дня
     * @return Список фотографий
     */
    public List<WorkPhoto> getWorkPhotos(Long workDayId) {
        WorkDay workDay = workDayRepository.findById(workDayId)
                .orElseThrow(() -> new RuntimeException("Рабочий день не найден"));

        return workPhotoRepository.findByWorkDay(workDay);
    }

    /**
     * Получение фотографии по ID.
     * @param photoId ID фотографии
     * @return Найденная фотография
     */
    public WorkPhoto getPhotoById(Long photoId) {
        return workPhotoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Фотография не найдена"));
    }

    /**
     * Получение расширения файла из content type.
     */
    private String getExtension(String contentType) {
        if (contentType == null) {
            return "jpg";
        }
        if (contentType.contains("png")) {
            return "png";
        }
        if (contentType.contains("gif")) {
            return "gif";
        }
        return "jpg";
    }
}
