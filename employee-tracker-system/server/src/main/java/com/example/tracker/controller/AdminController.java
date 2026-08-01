package com.example.tracker.controller;

import com.example.tracker.dto.*;
import com.example.tracker.model.*;
import com.example.tracker.service.EmployeeService;
import com.example.tracker.service.WorkDayService;
import com.example.tracker.service.GeoZoneService;
import com.example.tracker.service.PositionService;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Контроллер для администратора.
 * Обрабатывает запросы на управление должностями, геозонами, сотрудниками,
 * просмотр рабочих дней, фото, штрафов и экспорт в Excel.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Разрешаем CORS для PWA приложения администратора
public class AdminController {

    private final EmployeeService employeeService;
    private final WorkDayService workDayService;
    private final GeoZoneService geoZoneService;
    private final PositionService positionService;

    // ==================== УПРАВЛЕНИЕ ДОЛЖНОСТЯМИ ====================

    /**
     * Создание новой должности.
     * POST /api/admin/positions
     * 
     * @param request Данные должности (название, ставка за час)
     * @return Созданная должность
     */
    @PostMapping("/positions")
    public ResponseEntity<?> createPosition(@RequestBody PositionRequest request) {
        try {
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Название должности обязательно");
            }
            if (request.getHourlyRate() == null || request.getHourlyRate() <= 0) {
                return ResponseEntity.badRequest().body("Ставка должна быть больше 0");
            }

            Position position = positionService.createPosition(request.getName(), request.getHourlyRate());
            return ResponseEntity.ok(position);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка создания должности: " + e.getMessage());
        }
    }

    /**
     * Получение списка всех должностей.
     * GET /api/admin/positions
     * 
     * @return Список должностей
     */
    @GetMapping("/positions")
    public ResponseEntity<?> getAllPositions() {
        try {
            List<Position> positions = positionService.getAllPositions();
            return ResponseEntity.ok(positions);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка получения должностей: " + e.getMessage());
        }
    }

    /**
     * Удаление должности по ID.
     * DELETE /api/admin/positions/{id}
     * 
     * @param id ID должности
     * @return Статус удаления
     */
    @DeleteMapping("/positions/{id}")
    public ResponseEntity<?> deletePosition(@PathVariable Long id) {
        try {
            positionService.deletePosition(id);
            return ResponseEntity.ok("Должность удалена");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка удаления должности: " + e.getMessage());
        }
    }

    // ==================== УПРАВЛЕНИЕ ГЕОЗОНАМИ ====================

    /**
     * Создание новой геозоны.
     * POST /api/admin/geo-zones
     * 
     * @param request Данные геозоны (название, координаты, радиус)
     * @return Созданная геозона
     */
    @PostMapping("/geo-zones")
    public ResponseEntity<?> createGeoZone(@RequestBody GeoZoneRequest request) {
        try {
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Название геозоны обязательно");
            }
            if (request.getLatitude() == null || request.getLongitude() == null) {
                return ResponseEntity.badRequest().body("Координаты обязательны");
            }
            if (request.getRadiusMeters() == null || request.getRadiusMeters() <= 0) {
                return ResponseEntity.badRequest().body("Радиус должен быть больше 0");
            }

            GeoZone geoZone = geoZoneService.createGeoZone(
                request.getName(),
                request.getLatitude(),
                request.getLongitude(),
                request.getRadiusMeters()
            );
            return ResponseEntity.ok(geoZone);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка создания геозоны: " + e.getMessage());
        }
    }

    /**
     * Получение списка всех геозон.
     * GET /api/admin/geo-zones
     * 
     * @return Список геозон
     */
    @GetMapping("/geo-zones")
    public ResponseEntity<?> getAllGeoZones() {
        try {
            List<GeoZone> geoZones = geoZoneService.getAllGeoZones();
            return ResponseEntity.ok(geoZones);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка получения геозон: " + e.getMessage());
        }
    }

    /**
     * Удаление геозоны по ID.
     * DELETE /api/admin/geo-zones/{id}
     * 
     * @param id ID геозоны
     * @return Статус удаления
     */
    @DeleteMapping("/geo-zones/{id}")
    public ResponseEntity<?> deleteGeoZone(@PathVariable Long id) {
        try {
            geoZoneService.deleteGeoZone(id);
            return ResponseEntity.ok("Геозона удалена");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка удаления геозоны: " + e.getMessage());
        }
    }

    // ==================== УПРАВЛЕНИЕ СОТРУДНИКАМИ ====================

    /**
     * Назначение геозоны сотруднику.
     * POST /api/admin/assign-geo-zone
     * 
     * @param request ID сотрудника и ID геозоны
     * @return Обновленный сотрудник
     */
    @PostMapping("/assign-geo-zone")
    public ResponseEntity<?> assignGeoZone(@RequestBody AssignGeoZoneRequest request) {
        try {
            if (request.getEmployeeId() == null || request.getGeoZoneId() == null) {
                return ResponseEntity.badRequest().body("ID сотрудника и геозоны обязательны");
            }

            Employee employee = employeeService.assignGeoZone(request.getEmployeeId(), request.getGeoZoneId());
            return ResponseEntity.ok(employee);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка назначения геозоны: " + e.getMessage());
        }
    }

    /**
     * Получение списка всех сотрудников.
     * GET /api/admin/employees
     * 
     * @return Список сотрудников с их геозонами и должностями
     */
    @GetMapping("/employees")
    public ResponseEntity<?> getAllEmployees() {
        try {
            List<Employee> employees = employeeService.getAllEmployees();
            return ResponseEntity.ok(employees);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка получения сотрудников: " + e.getMessage());
        }
    }

    /**
     * Назначение штрафа сотруднику за конкретный день.
     * POST /api/admin/fine
     * 
     * @param employeeId ID сотрудника
     * @param date Дата штрафа
     * @param request Сумма и причина штрафа
     * @return Обновленный рабочий день
     */
    @PostMapping("/fine")
    public ResponseEntity<?> assignFine(
            @RequestParam Long employeeId,
            @RequestParam LocalDate date,
            @RequestBody FineRequest request) {
        try {
            if (request.getAmount() == null || request.getAmount() < 0) {
                return ResponseEntity.badRequest().body("Сумма штрафа должна быть >= 0");
            }

            WorkDay workDay = workDayService.assignFine(employeeId, date, request.getAmount(), request.getReason());
            return ResponseEntity.ok(workDay);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка назначения штрафа: " + e.getMessage());
        }
    }

    // ==================== УПРАВЛЕНИЕ ЗАДАНИЯМИ ====================

    /**
     * Создание задания на день недели для сотрудника.
     * POST /api/admin/tasks
     * 
     * @param request ID сотрудника, день недели, текст задания
     * @return Созданное задание
     */
    @PostMapping("/tasks")
    public ResponseEntity<?> createTask(@RequestBody WeeklyTaskRequest request) {
        try {
            if (request.getEmployeeId() == null) {
                return ResponseEntity.badRequest().body("ID сотрудника обязателен");
            }
            if (request.getDayOfWeek() == null || request.getDayOfWeek() < 1 || request.getDayOfWeek() > 7) {
                return ResponseEntity.badRequest().body("День недели должен быть от 1 до 7");
            }
            if (request.getTaskDescription() == null || request.getTaskDescription().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Описание задания обязательно");
            }

            WeeklyTask task = employeeService.createWeeklyTask(
                request.getEmployeeId(),
                request.getDayOfWeek(),
                request.getTaskDescription()
            );
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка создания задания: " + e.getMessage());
        }
    }

    // ==================== ПРОСМОТР РАБОЧИХ ДНЕЙ И ФОТО ====================

    /**
     * Получение рабочих дней сотрудника за период.
     * GET /api/admin/work-days
     * 
     * @param employeeId ID сотрудника (опционально, если не указан - все сотрудники)
     * @param startDate Дата начала периода
     * @param endDate Дата окончания периода
     * @return Список рабочих дней
     */
    @GetMapping("/work-days")
    public ResponseEntity<?> getWorkDays(
            @RequestParam(required = false) Long employeeId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        try {
            List<WorkDay> workDays;
            
            if (employeeId != null) {
                workDays = workDayService.getWorkDaysByPeriod(employeeId, startDate, endDate);
            } else {
                workDays = workDayService.getAllWorkDaysByPeriod(startDate, endDate);
            }
            
            return ResponseEntity.ok(workDays);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка получения рабочих дней: " + e.getMessage());
        }
    }

    /**
     * Получение фотографий для рабочего дня.
     * GET /api/admin/photos/{workDayId}
     * 
     * @param workDayId ID рабочего дня
     * @return Список фотографий (в base64 или как файлы)
     */
    @GetMapping("/photos/{workDayId}")
    public ResponseEntity<?> getWorkDayPhotos(@PathVariable Long workDayId) {
        try {
            List<WorkPhoto> photos = workDayService.getWorkPhotos(workDayId);
            return ResponseEntity.ok(photos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка получения фотографий: " + e.getMessage());
        }
    }

    /**
     * Скачивание фотографии.
     * GET /api/admin/photos/{photoId}/download
     * 
     * @param photoId ID фотографии
     * @return Файл фотографии
     */
    @GetMapping("/photos/{photoId}/download")
    public ResponseEntity<?> downloadPhoto(@PathVariable Long photoId) {
        try {
            WorkPhoto photo = workDayService.getPhotoById(photoId);
            
            ByteArrayResource resource = new ByteArrayResource(photo.getImageData());
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(photo.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"photo_" + photoId + ".jpg\"")
                .contentLength(photo.getImageData().length)
                .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка скачивания фото: " + e.getMessage());
        }
    }

    // ==================== ЭКСПОРТ В EXCEL ====================

    /**
     * Экспорт табеля рабочего времени в Excel.
     * GET /api/admin/export/excel
     * 
     * @param employeeId ID сотрудника (опционально, если не указан - все сотрудники)
     * @param startDate Дата начала периода
     * @param endDate Дата окончания периода
     * @return Excel файл
     */
    @GetMapping("/export/excel")
    public ResponseEntity<?> exportToExcel(
            @RequestParam(required = false) Long employeeId,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        try {
            List<WorkDay> workDays;
            
            if (employeeId != null) {
                workDays = workDayService.getWorkDaysByPeriod(employeeId, startDate, endDate);
            } else {
                workDays = workDayService.getAllWorkDaysByPeriod(startDate, endDate);
            }

            // Создаем Excel workbook
            try (Workbook workbook = new XSSFWorkbook()) {
                Sheet sheet = workbook.createSheet("Табель");

                // Создаем стили
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerFont.setFontHeightInPoints((short) 12);
                headerStyle.setFont(headerFont);
                headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

                CellStyle totalStyle = workbook.createCellStyle();
                Font totalFont = workbook.createFont();
                totalFont.setBold(true);
                totalStyle.setFont(totalFont);

                // Создаем заголовки
                Row headerRow = sheet.createRow(0);
                String[] headers = {
                    "Дата", "Сотрудник", "Должность", "Начало", "Конец", 
                    "Обед (мин)", "Перерывы (мин)", "Отработано часов", 
                    "Ставка/час", "Заработано", "Штраф", "Итого", "Комментарий"
                };

                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                // Заполняем данными
                int rowNum = 1;
                double grandTotal = 0;

                for (WorkDay day : workDays) {
                    Row row = sheet.createRow(rowNum++);
                    
                    row.createCell(0).setCellValue(day.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
                    row.createCell(1).setCellValue(day.getEmployee().getFullName());
                    row.createCell(2).setCellValue(day.getEmployee().getPosition());
                    row.createCell(3).setCellValue(day.getStartTime().toString());
                    row.createCell(4).setCellValue(day.getEndTime() != null ? day.getEndTime().toString() : "Не завершен");
                    row.createCell(5).setCellValue(day.getLunchDurationMinutes());
                    row.createCell(6).setCellValue(day.getBreakDurationMinutes());
                    row.createCell(7).setCellValue(day.getTotalHours());
                    row.createCell(8).setCellValue(day.getHourlyRate());
                    row.createCell(9).setCellValue(day.getEarnings());
                    row.createCell(10).setCellValue(day.getFineAmount() != null ? day.getFineAmount() : 0);
                    row.createCell(11).setCellValue(day.getFinalAmount());
                    row.createCell(12).setCellValue(day.getComment() != null ? day.getComment() : "");

                    grandTotal += day.getFinalAmount();
                }

                // Итоговая строка
                Row totalRow = sheet.createRow(rowNum++);
                Cell totalLabel = totalRow.createCell(0);
                totalLabel.setCellValue("ИТОГО:");
                totalLabel.setCellStyle(totalStyle);
                
                Cell totalValue = totalRow.createCell(11);
                totalValue.setCellValue(grandTotal);
                totalValue.setCellStyle(totalStyle);

                // Автоширина колонок
                for (int i = 0; i < headers.length; i++) {
                    sheet.autoSizeColumn(i);
                }

                // Сохраняем в ByteArrayOutputStream
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                workbook.write(outputStream);

                // Формируем имя файла
                String fileName = "tabel_" + 
                    startDate.format(DateTimeFormatter.BASIC_ISO_DATE) + "_" +
                    endDate.format(DateTimeFormatter.BASIC_ISO_DATE) + ".xlsx";

                ByteArrayResource resource = new ByteArrayResource(outputStream.toByteArray());

                return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentLength(outputStream.size())
                    .body(resource);
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка экспорта в Excel: " + e.getMessage());
        }
    }

    /**
     * Получение статистики по сотрудникам.
     * GET /api/admin/statistics
     * 
     * @param startDate Дата начала периода
     * @param endDate Дата окончания периода
     * @return Статистика
     */
    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics(
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate) {
        try {
            List<WorkDay> workDays = workDayService.getAllWorkDaysByPeriod(startDate, endDate);

            Map<String, Object> stats = new HashMap<>();
            stats.put("totalEmployees", workDays.stream().map(wd -> wd.getEmployee().getId()).distinct().count());
            stats.put("totalWorkDays", workDays.size());
            stats.put("totalHours", workDays.stream().mapToDouble(WorkDay::getTotalHours).sum());
            stats.put("totalEarnings", workDays.stream().mapToDouble(WorkDay::getFinalAmount).sum());
            stats.put("totalFines", workDays.stream()
                .mapToDouble(wd -> wd.getFineAmount() != null ? wd.getFineAmount() : 0)
                .sum());

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка получения статистики: " + e.getMessage());
        }
    }
}
