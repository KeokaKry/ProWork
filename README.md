# WorkTracker - Система учета рабочего времени сотрудников

## Описание
Backend приложение для управления сотрудниками, их рабочим временем, геозонами и уведомлениями о перерывах.

## Функционал

### 1. Управление должностями (Positions)
- `GET /api/positions` - Получить список всех должностей
- `POST /api/positions` - Создать новую должность
- `PUT /api/positions/{id}` - Редактировать должность (название, ставку)
- `DELETE /api/positions/{id}` - Удалить должность

### 2. Управление сотрудниками (Employees)
- `GET /api/employees/list` - Получить список сотрудников (для выбора в приложении)
- `POST /api/employees` - Создать сотрудника (админ назначает пароль и должность)
- `PUT /api/employees/{id}` - Обновить данные сотрудника (должность, пароль)
- `DELETE /api/employees/{id}` - Уволить сотрудника (удаление)

### 3. Аутентификация
- `POST /api/auth/login` - Вход сотрудника (ФИО + пароль)

### 4. Геозоны (GeoZones)
- `GET /api/geozones` - Получить список геозон
- `POST /api/geozones` - Создать геозону
- `PUT /api/geozones/{id}` - Редактировать геозону
- `DELETE /api/geozones/{id}` - Удалить геозону
- `POST /api/geozones/{geoZoneId}/assign/{employeeId}` - Назначить геозону сотруднику
- `DELETE /api/geozones/{geoZoneId}/unassign/{employeeId}` - Удалить геозону у сотрудника

### 5. Работа (Work Records)
- `POST /api/work/start` - Начать рабочий день (с проверкой геозоны)
- `POST /api/work/finish/{id}` - Завершить рабочий день
- `GET /api/work/history/{employeeId}` - История работы сотрудника
- `GET /api/work/active/{employeeId}` - Получить активную запись работы
- `GET /api/work/break-info/{employeeId}` - Информация о перерывах

### 6. Задания (Tasks)
- `POST /api/tasks/assign` - Админ назначает задание на день
- `PUT /api/tasks/{recordId}` - Обновить задание
- `GET /api/tasks/employee/{employeeId}` - Получить задания сотрудника

### 7. Отчеты (Reports)
- `POST /api/reports/{recordId}/upload` - Загрузить фотоотчет о работе
- `GET /api/reports/all` - Получить все отчеты (админ)
- `GET /api/reports/employee/{employeeId}` - Получить отчеты сотрудника

### 8. Уведомления (Notifications)
- `GET /api/notifications/check-break/{employeeId}` - Проверка уведомлений о перерывах
  - Короткий перерыв: за 10 минут до конца каждого часа (на 50-й минуте)
  - Обед: через 4 часа после начала работы (240-300 минуты)

## Модели данных

### Employee (Сотрудник)
- id
- fullName (ФИО)
- phone (телефон)
- password (пароль от админа)
- position (должность)
- geoZones (список геозон)
- workRecords (история работ)

### Position (Должность)
- id
- name (название)
- hourlyRate (часовая ставка)

### GeoZone (Геозона)
- id
- name (название)
- latitude (широта)
- longitude (долгота)
- radius (радиус в метрах)
- employees (сотрудники, закрепленные за зоной)

### WorkRecord (Запись работы)
- id
- employee (сотрудник)
- startTime (время начала)
- endTime (время окончания)
- startLat, startLon (координаты начала)
- endLat, endLon (координаты окончания)
- status (ACTIVE, COMPLETED, PENDING)
- dailyTask (задание на день)
- reportPhotoUrl (ссылка на фотоотчет)

## Логика работы

1. **Вход сотрудника**: Сотрудник выбирает свое ФИО из списка и вводит пароль
2. **Начало работы**: При старте проверяется геозона (если назначена)
3. **Перерывы**: 
   - Короткий перерыв 10 минут каждые 50 минут работы
   - Обед 60 минут через 4 часа работы
4. **Завершение работы**: Сотрудник загружает фотоотчет

## Технологии
- Java 17
- Spring Boot 3.2.5
- Spring Data JPA
- H2 Database
- Lombok
