package com.example.tracker.controller;

import com.example.tracker.dto.*;
import com.example.tracker.model.*;
import com.example.tracker.service.EmployeeService;
import com.example.tracker.service.WorkDayService;
import com.example.tracker.service.GeoDistanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Контроллер для сотрудников (PWA приложение).
 * Обрабатывает запросы на регистрацию, начало/конец рабочего дня,
 * загрузку фотографий и получение заданий.
 */
@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Разрешаем CORS для PWA приложения
public class EmployeeController {

    private final EmployeeService employeeService;
    private final WorkDayService workDayService;
    private final GeoDistanceService geoDistanceService;

    /**
     * Регистрация нового сотрудника.
     * POST /api/employee/register
     * 
     * @param request Данные для регистрации (имя, должность, пароль)
     * @return Ответ с ID сотрудника и токеном
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            // Валидация входных данных
            if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Имя сотрудника обязательно");
            }
            if (request.getPosition() == null || request.getPosition().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Должность обязательна");
            }
            if (request.getPassword() == null || request.getPassword().length() < 4) {
                return ResponseEntity.badRequest().body("Пароль должен быть минимум 4 символа");
            }

            // Вызов сервиса для регистрации
            Employee employee = employeeService.registerEmployee(
                request.getFullName(),
                request.getPosition(),
                request.getPassword()
            );

            // Создаем простой токен (в реальном проекте использовать JWT)
            String token = "emp_" + employee.getId();
            
            return ResponseEntity.ok(new AuthResponse(
                employee.getId(),
                employee.getFullName(),
                employee.getPosition(),
                token
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка регистрации: " + e.getMessage());
        }
    }

    /**
     * Начало рабочего дня с проверкой геозоны.
     * POST /api/employee/start-work
     * 
     * @param employeeId ID сотрудника (из токена)
     * @param request Координаты GPS телефона
     * @return Результат начала работы или ошибка если не в геозоне
     */
    @PostMapping("/start-work")
    public ResponseEntity<?> startWork(
            @RequestParam Long employeeId,
            @RequestBody StartWorkRequest request) {
        try {
            // Проверка координат
            if (request.getLatitude() == null || request.getLongitude() == null) {
                return ResponseEntity.badRequest().body("Координаты GPS обязательны");
            }

            // Находим сотрудника
            Employee employee = employeeService.findById(employeeId)
                .orElseThrow(() -> new RuntimeException("Сотрудник не найден"));

            // Проверяем геозону сотрудника
            GeoZone geoZone = employee.getGeoZone();
            if (geoZone == null) {
                return ResponseEntity.badRequest().body("Для сотрудника не назначена геозона. Обратитесь к администратору.");
            }

            // Рассчитываем расстояние между телефоном и центром геозоны
            double distance = geoDistanceService.calculateDistance(
                request.getLatitude(),
                request.getLongitude(),
                geoZone.getLatitude(),
                geoZone.getLongitude()
            );

            // Проверяем, находится ли сотрудник в радиусе геозоны
            if (distance > geoZone.getRadiusMeters()) {
                return ResponseEntity.badRequest().body(
                    String.format("Вы не на объекте! До объекта %.0f метров. Максимальное расстояние: %d метров", 
                        distance, geoZone.getRadiusMeters())
                );
            }

            // Если все ОК - начинаем рабочий день
            WorkDay workDay = workDayService.startWorkDay(employee);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Рабочий день начался успешно");
            response.put("workDayId", workDay.getId());
            response.put("startTime", workDay.getStartTime());
            response.put("nextBreakTime", workDay.getNextBreakTime());
            response.put("lunchTime", workDay.getLunchStartTime());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка начала рабочего дня: " + e.getMessage());
        }
    }

    /**
     * Завершение рабочего дня с загрузкой фотографий.
     * POST /api/employee/end-work
     * 
     * @param employeeId ID сотрудника
     * @param workDayId ID рабочего дня
     * @param comment Комментарий о проделанной работе
     * @param photos Массив фотографий (до 10 шт)
     * @return Результат завершения работы
     */
    @PostMapping("/end-work")
    public ResponseEntity<?> endWork(
            @RequestParam Long employeeId,
            @RequestParam Long workDayId,
            @RequestParam(required = false) String comment,
            @RequestParam(required = false) List<MultipartFile> photos) {
        try {
            // Находим рабочий день
            WorkDay workDay = workDayService.findById(workDayId)
                .orElseThrow(() -> new RuntimeException("Рабочий день не найден"));

            // Проверяем, что этот рабочий день принадлежит сотруднику
            if (!workDay.getEmployee().getId().equals(employeeId)) {
                return ResponseEntity.badRequest().body("Нельзя завершить чужой рабочий день");
            }

            // Завершаем рабочий день с комментарием
            workDayService.endWorkDay(workDayId, comment);

            // Загружаем фотографии если они есть
            if (photos != null && !photos.isEmpty()) {
                if (photos.size() > 10) {
                    return ResponseEntity.badRequest().body("Максимум 10 фотографий");
                }

                for (MultipartFile photo : photos) {
                    if (!photo.isEmpty()) {
                        workDayService.addWorkPhoto(workDayId, photo.getBytes(), photo.getContentType());
                    }
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Рабочий день завершен успешно");
            response.put("totalHours", workDay.getTotalHours());
            response.put("earnings", workDay.getEarnings());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка завершения рабочего дня: " + e.getMessage());
        }
    }

    /**
     * Получение заданий на текущий день недели.
     * GET /api/employee/tasks
     * 
     * @param employeeId ID сотрудника
     * @return Список заданий на сегодня
     */
    @GetMapping("/tasks")
    public ResponseEntity<?> getTasks(@RequestParam Long employeeId) {
        try {
            // Определяем текущий день недели (1-Пн, ..., 7-Вс)
            int currentDayOfWeek = LocalDate.now().getDayOfWeek().getValue();
            
            List<WeeklyTask> tasks = employeeService.getTasksForDay(employeeId, currentDayOfWeek);
            
            return ResponseEntity.ok(tasks);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка получения заданий: " + e.getMessage());
        }
    }

    /**
     * Получение информации о текущем рабочем дне.
     * GET /api/employee/current-workday
     * 
     * @param employeeId ID сотрудника
     * @return Информация о текущем рабочем дне или null если не начат
     */
    @GetMapping("/current-workday")
    public ResponseEntity<?> getCurrentWorkDay(@RequestParam Long employeeId) {
        try {
            WorkDay currentWorkDay = workDayService.getCurrentWorkDay(employeeId);
            
            if (currentWorkDay == null) {
                return ResponseEntity.ok(null);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("id", currentWorkDay.getId());
            response.put("startTime", currentWorkDay.getStartTime());
            response.put("nextBreakTime", currentWorkDay.getNextBreakTime());
            response.put("lunchStartTime", currentWorkDay.getLunchStartTime());
            response.put("lunchEndTime", currentWorkDay.getLunchEndTime());
            response.put("isOnBreak", currentWorkDay.isOnBreak());
            response.put("isOnLunch", currentWorkDay.isOnLunch());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка получения данных: " + e.getMessage());
        }
    }
}
