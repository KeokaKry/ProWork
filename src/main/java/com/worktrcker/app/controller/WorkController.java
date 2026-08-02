package com.worktrcker.app.controller;

import com.worktrcker.app.dto.*;
import com.worktrcker.app.entity.Employee;
import com.worktrcker.app.entity.GeoZone;
import com.worktrcker.app.entity.WorkDay;
import com.worktrcker.app.entity.WorkPhoto;
import com.worktrcker.app.repository.EmployeeRepository;
import com.worktrcker.app.repository.GeoZoneRepository;
import com.worktrcker.app.repository.WorkPhotoRepository;
import com.worktrcker.app.service.GeoDistanceService;
import com.worktrcker.app.service.WorkDayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Контроллер для работы с сотрудниками.
 * Обрабатывает запросы на вход, начало/конец рабочего дня, загрузку фото.
 */
@RestController
@RequestMapping("/api")
public class WorkController {

    private final EmployeeRepository employeeRepository;
    private final GeoZoneRepository geoZoneRepository;
    private final WorkDayService workDayService;
    private final GeoDistanceService geoDistanceService;
    private final WorkPhotoRepository workPhotoRepository;

    // Папка для хранения загруженных фотографий
    private static final String UPLOAD_DIR = "uploads/photos/";

    public WorkController(EmployeeRepository employeeRepository,
                         GeoZoneRepository geoZoneRepository,
                         WorkDayService workDayService,
                         GeoDistanceService geoDistanceService,
                         WorkPhotoRepository workPhotoRepository) {
        this.employeeRepository = employeeRepository;
        this.geoZoneRepository = geoZoneRepository;
        this.workDayService = workDayService;
        this.geoDistanceService = geoDistanceService;
        this.workPhotoRepository = workPhotoRepository;
        
        // Создаем директорию для загрузки файлов при старте
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR));
        } catch (IOException e) {
            System.err.println("Не удалось создать директорию для загрузки: " + e.getMessage());
        }
    }

    /**
     * Вход сотрудника по ФИО и паролю
     * POST /api/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Проверяем, что переданы ФИО и пароль
            if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Введите ФИО");
            }
            if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Введите пароль");
            }

            // Находим сотрудника по ФИО
            Employee employee = employeeRepository.findByFullName(request.getFullName())
                .orElseThrow(() -> new RuntimeException("Сотрудник с таким ФИО не найден"));

            // Проверяем пароль
            if (!employee.getPassword().equals(request.getPassword())) {
                return ResponseEntity.badRequest().body("Неверный пароль");
            }

            // Возвращаем информацию о сотруднике
            LoginResponse response = new LoginResponse();
            response.setId(employee.getId());
            response.setUsername(employee.getUsername());
            response.setFullName(employee.getFullName());
            response.setPosition(employee.getPosition());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Начало рабочего дня с проверкой геолокации
     * POST /api/work/start
     */
    @PostMapping("/work/start")
    public ResponseEntity<?> startWorkDay(@Valid @RequestBody StartWorkDayRequest request,
                                         @RequestParam String username) {
        try {
            // Находим сотрудника по username (телефону)
            Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Сотрудник не найден"));

            // Получаем все активные геозоны
            List<GeoZone> activeZones = geoZoneRepository.findByActiveTrue();
            
            if (activeZones.isEmpty()) {
                return ResponseEntity.badRequest().body("Геозоны не настроены. Обратитесь к администратору.");
            }

            // Проверяем, находится ли сотрудник в какой-либо геозоне
            GeoZone matchedZone = null;
            double minDistance = Double.MAX_VALUE;

            for (GeoZone zone : activeZones) {
                double distance = geoDistanceService.calculateDistance(
                    request.getLatitude(), request.getLongitude(),
                    zone.getLatitude(), zone.getLongitude()
                );

                if (distance <= zone.getRadiusMeters()) {
                    matchedZone = zone;
                    break;
                }

                if (distance < minDistance) {
                    minDistance = distance;
                }
            }

            if (matchedZone == null) {
                // Сотрудник не в геозоне - возвращаем ошибку с расстоянием до ближайшей
                return ResponseEntity.badRequest().body(
                    "Вы не на объекте! До ближайшего объекта " + Math.round(minDistance) + " метров"
                );
            }

            // Сотрудник в геозоне - начинаем рабочий день
            WorkDay workDay = workDayService.startWorkDay(employee);
            
            WorkDayResponse response = new WorkDayResponse();
            response.setId(workDay.getId());
            response.setDate(workDay.getDate());
            response.setStartTime(workDay.getStartTime());
            response.setLunchStart(workDay.getLunchStart());
            response.setLunchEnd(workDay.getLunchEnd());
            response.setShortBreaks(workDay.getShortBreaks());
            response.setStatus(workDay.getStatus().name());
            response.setMessage("Рабочий день начался. Объект: " + matchedZone.getName());

            return ResponseEntity.ok(response);

        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка сервера: " + e.getMessage());
        }
    }

    /**
     * Загрузка фотографии выполненной работы
     * POST /api/work/photo
     */
    @PostMapping("/work/photo")
    public ResponseEntity<?> uploadPhoto(@RequestParam("file") MultipartFile file,
                                        @RequestParam Long workDayId) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Файл не выбран");
            }

            // Генерируем уникальное имя файла
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID().toString() + extension;

            // Сохраняем файл на сервере
            Path filePath = Paths.get(UPLOAD_DIR + filename);
            Files.write(filePath, file.getBytes());

            // Создаем запись в базе данных
            WorkDay workDay = workDayService.getCurrentWorkDay(
                employeeRepository.findById(1L).orElse(null) // TODO: получить из контекста
            );
            
            if (workDay == null || !workDay.getId().equals(workDayId)) {
                // Для упрощения пока не проверяем принадлежность
                workDay = new WorkDay();
                workDay.setId(workDayId);
            }

            WorkPhoto photo = new WorkPhoto();
            photo.setWorkDay(workDay);
            photo.setFileName(originalFilename != null ? originalFilename : filename);
            photo.setFilePath(filePath.toString());
            photo.setContentType(file.getContentType());
            photo.setFileSize(file.getSize());

            workPhotoRepository.save(photo);

            return ResponseEntity.ok(filename);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("Ошибка сохранения файла: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка сервера: " + e.getMessage());
        }
    }

    /**
     * Завершение рабочего дня
     * POST /api/work/finish
     */
    @PostMapping("/work/finish")
    public ResponseEntity<?> finishWorkDay(@Valid @RequestBody FinishWorkDayRequest request,
                                          @RequestParam Long workDayId) {
        try {
            WorkDay workDay = workDayService.finishWorkDay(workDayId, request.getComment());
            
            WorkDayResponse response = new WorkDayResponse();
            response.setId(workDay.getId());
            response.setDate(workDay.getDate());
            response.setStartTime(workDay.getStartTime());
            response.setEndTime(workDay.getEndTime());
            response.setLunchStart(workDay.getLunchStart());
            response.setLunchEnd(workDay.getLunchEnd());
            response.setShortBreaks(workDay.getShortBreaks());
            response.setComment(workDay.getComment());
            response.setStatus(workDay.getStatus().name());
            response.setWorkedHours(workDay.getWorkedHours());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка сервера: " + e.getMessage());
        }
    }

    /**
     * Получение текущего рабочего дня сотрудника
     * GET /api/work/current?username=...
     */
    @GetMapping("/work/current")
    public ResponseEntity<?> getCurrentWorkDay(@RequestParam String username) {
        try {
            Employee employee = employeeRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Сотрудник не найден"));

            WorkDay workDay = workDayService.getCurrentWorkDay(employee);

            if (workDay == null) {
                return ResponseEntity.ok(null); // Рабочий день еще не начат
            }

            WorkDayResponse response = new WorkDayResponse();
            response.setId(workDay.getId());
            response.setDate(workDay.getDate());
            response.setStartTime(workDay.getStartTime());
            response.setEndTime(workDay.getEndTime());
            response.setLunchStart(workDay.getLunchStart());
            response.setLunchEnd(workDay.getLunchEnd());
            response.setShortBreaks(workDay.getShortBreaks());
            response.setComment(workDay.getComment());
            response.setStatus(workDay.getStatus().name());
            response.setWorkedHours(workDay.getWorkedHours());
            response.setPenalty(workDay.getPenalty());
            response.setPenaltyReason(workDay.getPenaltyReason());

            // Получаем фотографии
            List<WorkPhoto> photos = workPhotoRepository.findByWorkDayId(workDay.getId());
            List<String> photoUrls = photos.stream()
                .map(p -> "/api/files/" + p.getFilePath().replace("\\", "/"))
                .collect(Collectors.toList());
            response.setPhotoUrls(photoUrls);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка сервера: " + e.getMessage());
        }
    }

    /**
     * Получить список всех сотрудников для выбора при входе
     * GET /api/employees/list
     */
    @GetMapping("/employees/list")
    public ResponseEntity<?> getEmployeesList() {
        try {
            List<Employee> employees = employeeRepository.findAll();
            
            // Возвращаем только необходимую информацию (id и ФИО)
            List<EmployeeInfo> employeeInfos = employees.stream()
                .map(e -> new EmployeeInfo(e.getId(), e.getFullName(), e.getPosition()))
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(employeeInfos);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Ошибка сервера: " + e.getMessage());
        }
    }

    /**
     * DTO для информации о сотруднике в списке
     */
    public static class EmployeeInfo {
        private final Long id;
        private final String fullName;
        private final String position;

        public EmployeeInfo(Long id, String fullName, String position) {
            this.id = id;
            this.fullName = fullName;
            this.position = position;
        }

        public Long getId() { return id; }
        public String getFullName() { return fullName; }
        public String getPosition() { return position; }
    }
}
