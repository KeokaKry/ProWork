/**
 * WorkTracker PWA - Основное приложение для сотрудников
 * Реализует учет рабочего времени с геолокацией, перерывами и фотоотчетами
 */

// Конфигурация
const API_BASE_URL = 'http://localhost:8080/api';
const GEO_CHECK_INTERVAL = 10000; // Проверка геолокации каждые 10 секунд
const BREAK_REMINDER_TIME = 50; // Напоминание о перерыве на 50 минуте часа

// Глобальные переменные состояния
let currentUser = null;
let currentWorkDay = null;
let geoLocationWatchId = null;
let breakTimerInterval = null;
let isOnBreak = false;
let isOnLunch = false;
let breakStartTime = null;
let lunchStartTime = null;

// ============================================
// Инициализация приложения
// ============================================

/**
 * Инициализация приложения при загрузке страницы
 */
document.addEventListener('DOMContentLoaded', async () => {
    console.log('WorkTracker PWA загружено');
    
    // Регистрация Service Worker для PWA
    await registerServiceWorker();
    
    // Проверка сохраненной сессии
    await checkSession();
    
    // Настройка обработчиков событий
    setupEventListeners();
});

/**
 * Регистрация Service Worker для офлайн-работы
 */
async function registerServiceWorker() {
    if ('serviceWorker' in navigator) {
        try {
            const registration = await navigator.serviceWorker.register('sw.js');
            console.log('Service Worker зарегистрирован:', registration.scope);
            
            // Обновление Service Worker
            registration.addEventListener('updatefound', () => {
                const newWorker = registration.installing;
                newWorker.addEventListener('statechange', () => {
                    if (newWorker.state === 'installed' && navigator.serviceWorker.controller) {
                        showNotification('Доступна новая версия приложения!', 'warning');
                    }
                });
            });
        } catch (error) {
            console.error('Ошибка регистрации Service Worker:', error);
        }
    } else {
        console.warn('Service Worker не поддерживается в этом браузере');
    }
}

/**
 * Проверка сохраненной сессии пользователя
 */
async function checkSession() {
    const savedUser = localStorage.getItem('worktracker_user');
    if (savedUser) {
        currentUser = JSON.parse(savedUser);
        showEmployeeScreen();
        await loadCurrentWorkDay();
        startGeoTracking();
    }
}

/**
 * Настройка обработчиков событий формы
 */
function setupEventListeners() {
    // Обработка формы регистрации/входа
    document.getElementById('auth-form').addEventListener('submit', handleAuth);
    
    // Предпросмотр фотографий
    document.getElementById('work-photos').addEventListener('change', handlePhotoPreview);
}

// ============================================
// Аутентификация
// ============================================

/**
 * Обработка регистрации или входа сотрудника
 * @param {Event} event - событие отправки формы
 */
async function handleAuth(event) {
    event.preventDefault();
    
    const fullName = document.getElementById('fullName').value.trim();
    const phoneNumber = document.getElementById('phoneNumber').value.trim();
    const position = document.getElementById('position').value;
    const password = document.getElementById('password').value;
    
    if (!fullName || !phoneNumber || !position || !password) {
        showNotification('Заполните все поля!', 'error');
        return;
    }
    
    try {
        // Попытка входа через телефон (как username)
        const response = await fetch(`${API_BASE_URL}/employee/login`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                phoneNumber: phoneNumber,
                password: password
            })
        });
        
        if (response.ok) {
            // Успешный вход
            currentUser = await response.json();
            saveSession(currentUser);
            showEmployeeScreen();
            await loadCurrentWorkDay();
            startGeoTracking();
            showNotification(`Добро пожаловать, ${currentUser.fullName}!`, 'success');
            return;
        }
        
        // Если вход не удался, пробуем зарегистрировать
        const registerResponse = await fetch(`${API_BASE_URL}/employee/register`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                fullName: fullName,
                phoneNumber: phoneNumber,
                position: position,
                password: password
            })
        });
        
        if (registerResponse.ok) {
            currentUser = await registerResponse.json();
            saveSession(currentUser);
            showEmployeeScreen();
            await loadCurrentWorkDay();
            startGeoTracking();
            showNotification('Регистрация успешна!', 'success');
        } else {
            const error = await registerResponse.json();
            showNotification(error.message || 'Ошибка регистрации', 'error');
        }
    } catch (error) {
        console.error('Ошибка аутентификации:', error);
        showNotification('Нет соединения с сервером', 'error');
    }
}

/**
 * Сохранение сессии пользователя в localStorage
 * @param {Object} user - объект пользователя
 */
function saveSession(user) {
    localStorage.setItem('worktracker_user', JSON.stringify(user));
}

/**
 * Выход из системы
 */
function logout() {
    localStorage.removeItem('worktracker_user');
    currentUser = null;
    currentWorkDay = null;
    stopGeoTracking();
    stopBreakTimer();
    
    document.getElementById('employee-screen').classList.add('hidden');
    document.getElementById('auth-screen').classList.remove('hidden');
    document.getElementById('auth-form').reset();
    
    showNotification('Вы вышли из системы', 'success');
}

// ============================================
// Геолокация
// ============================================

/**
 * Запуск отслеживания геолокации
 */
function startGeoTracking() {
    if (!navigator.geolocation) {
        showNotification('Геолокация не поддерживается вашим устройством', 'error');
        return;
    }
    
    // Получение текущего местоположения
    getCurrentLocation();
    
    // Отслеживание изменений местоположения
    geoLocationWatchId = navigator.geolocation.watchPosition(
        getCurrentLocation,
        handleGeoError,
        {
            enableHighAccuracy: true,
            timeout: 10000,
            maximumAge: 5000
        }
    );
}

/**
 * Остановка отслеживания геолокации
 */
function stopGeoTracking() {
    if (geoLocationWatchId !== null) {
        navigator.geolocation.clearWatch(geoLocationWatchId);
        geoLocationWatchId = null;
    }
}

/**
 * Получение текущей геопозиции и проверка геозоны
 */
async function getCurrentLocation(position) {
    if (!currentUser || !currentUser.geoZoneId) {
        return;
    }
    
    const latitude = position.coords.latitude;
    const longitude = position.coords.longitude;
    const accuracy = position.coords.accuracy;
    
    try {
        // Проверка расстояния до геозоны
        const response = await fetch(`${API_BASE_URL}/employee/check-geozone?employeeId=${currentUser.id}&latitude=${latitude}&longitude=${longitude}`);
        
        if (response.ok) {
            const result = await response.json();
            updateGeoStatus(result);
        }
    } catch (error) {
        console.error('Ошибка проверки геозоны:', error);
    }
}

/**
 * Обработка ошибок геолокации
 * @param {GeolocationPositionError} error
 */
function handleGeoError(error) {
    console.error('Ошибка геолокации:', error);
    
    let message = 'Не удалось определить местоположение';
    switch (error.code) {
        case error.PERMISSION_DENIED:
            message = 'Доступ к геолокации запрещен. Разрешите доступ в настройках.';
            break;
        case error.POSITION_UNAVAILABLE:
            message = 'Информация о местоположении недоступна';
            break;
        case error.TIMEOUT:
            message = 'Превышено время ожидания геолокации';
            break;
    }
    
    showNotification(message, 'warning');
}

/**
 * Обновление отображения статуса геолокации
 * @param {Object} result - результат проверки геозоны
 */
function updateGeoStatus(result) {
    const geoStatusCard = document.getElementById('geo-status-card');
    const geoStatusContent = document.getElementById('geo-status-content');
    
    geoStatusCard.classList.remove('hidden');
    
    if (result.inside) {
        geoStatusContent.innerHTML = `
            <div class="geo-status geo-status-inside">
                ✅ Вы находитесь в геозоне
                <div class="geo-distance">${result.geoZoneName}</div>
                <div>Расстояние: ${Math.round(result.distance)} м</div>
            </div>
        `;
        // Разблокируем кнопку начала работы
        const btnStartWork = document.getElementById('btn-start-work');
        if (currentWorkDay === null && btnStartWork.classList.contains('hidden')) {
            btnStartWork.classList.remove('hidden');
        }
    } else {
        geoStatusContent.innerHTML = `
            <div class="geo-status geo-status-outside">
                ❌ Вы не на объекте!
                <div class="geo-distance">До объекта: ${Math.round(result.distance)} м</div>
                <div>Требуется: ≤ ${result.requiredDistance} м</div>
            </div>
        `;
        // Блокируем кнопку начала работы
        document.getElementById('btn-start-work').classList.add('hidden');
    }
}

// ============================================
// Управление рабочим днем
// ============================================

/**
 * Загрузка текущего рабочего дня
 */
async function loadCurrentWorkDay() {
    if (!currentUser) return;
    
    try {
        const response = await fetch(`${API_BASE_URL}/employee/current-day?employeeId=${currentUser.id}`);
        
        if (response.ok) {
            currentWorkDay = await response.json();
            updateWorkDayUI();
        }
    } catch (error) {
        console.error('Ошибка загрузки рабочего дня:', error);
    }
}

/**
 * Обновление интерфейса рабочего дня
 */
function updateWorkDayUI() {
    const dayStatus = document.getElementById('day-status');
    const timerContainer = document.getElementById('timer-container');
    const btnStartWork = document.getElementById('btn-start-work');
    const btnStartBreak = document.getElementById('btn-start-break');
    const btnStartLunch = document.getElementById('btn-start-lunch');
    const btnEndWork = document.getElementById('btn-end-work');
    
    if (!currentWorkDay || currentWorkDay.status === 'NOT_STARTED') {
        dayStatus.className = 'status-badge status-not-started';
        dayStatus.textContent = 'Не начат';
        timerContainer.classList.add('hidden');
        btnStartWork.classList.remove('hidden');
        btnStartBreak.classList.add('hidden');
        btnStartLunch.classList.add('hidden');
        btnEndWork.classList.add('hidden');
        stopBreakTimer();
    } else if (currentWorkDay.status === 'WORKING') {
        dayStatus.className = 'status-badge status-working';
        dayStatus.textContent = 'Работает';
        timerContainer.classList.remove('hidden');
        btnStartWork.classList.add('hidden');
        btnStartBreak.classList.remove('hidden');
        btnStartLunch.classList.remove('hidden');
        btnEndWork.classList.remove('hidden');
        startTimer(currentWorkDay.startTime);
        startBreakReminderTimer();
    } else if (currentWorkDay.status === 'ON_BREAK') {
        dayStatus.className = 'status-badge status-on-break';
        dayStatus.textContent = 'Перерыв';
        timerContainer.classList.remove('hidden');
        btnStartWork.classList.add('hidden');
        btnStartBreak.textContent = '▶️ Закончить перерыв';
        btnStartBreak.classList.remove('hidden');
        btnStartLunch.classList.add('hidden');
        btnEndWork.classList.add('hidden');
    } else if (currentWorkDay.status === 'ON_LUNCH') {
        dayStatus.className = 'status-badge status-on-lunch';
        dayStatus.textContent = 'Обед';
        timerContainer.classList.remove('hidden');
        btnStartWork.classList.add('hidden');
        btnStartBreak.classList.add('hidden');
        btnStartLunch.textContent = '▶️ Закончить обед';
        btnStartLunch.classList.remove('hidden');
        btnEndWork.classList.add('hidden');
    } else if (currentWorkDay.status === 'FINISHED') {
        dayStatus.className = 'status-badge status-finished';
        dayStatus.textContent = 'Завершен';
        timerContainer.classList.add('hidden');
        btnStartWork.classList.add('hidden');
        btnStartBreak.classList.add('hidden');
        btnStartLunch.classList.add('hidden');
        btnEndWork.classList.add('hidden');
        stopBreakTimer();
    }
}

/**
 * Начало рабочего дня
 */
async function startWork() {
    if (!currentUser) {
        showNotification('Пользователь не авторизован', 'error');
        return;
    }
    
    // Получаем текущие координаты
    if (!navigator.geolocation) {
        showNotification('Геолокация не поддерживается', 'error');
        return;
    }
    
    navigator.geolocation.getCurrentPosition(async (position) => {
        const latitude = position.coords.latitude;
        const longitude = position.coords.longitude;
        
        try {
            const response = await fetch(`${API_BASE_URL}/employee/start-work?employeeId=${currentUser.id}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    latitude: latitude,
                    longitude: longitude
                })
            });
            
            if (response.ok) {
                currentWorkDay = await response.json();
                updateWorkDayUI();
                startTimer(currentWorkDay.startTime);
                startBreakReminderTimer();
                showNotification('Рабочий день начался!', 'success');
            } else {
                const error = await response.json();
                showNotification(error.message || 'Ошибка начала рабочего дня', 'error');
            }
        } catch (error) {
            console.error('Ошибка начала рабочего дня:', error);
            showNotification('Нет соединения с сервером', 'error');
        }
    }, handleGeoError);
}

/**
 * Переключение перерыва
 */
async function toggleBreak() {
    if (!currentUser || !currentWorkDay) return;
    
    try {
        const response = await fetch(`${API_BASE_URL}/employee/toggle-break?workDayId=${currentWorkDay.id}`, {
            method: 'POST'
        });
        
        if (response.ok) {
            currentWorkDay = await response.json();
            updateWorkDayUI();
            
            if (currentWorkDay.status === 'ON_BREAK') {
                showNotification('Перерыв начался! Вернитесь через 10 минут.', 'warning');
                // Запускаем таймер перерыва
                startBreakTimer(10 * 60); // 10 минут
            } else {
                showNotification('Перерыв завершен. Приступайте к работе!', 'success');
                stopBreakTimer();
            }
        }
    } catch (error) {
        console.error('Ошибка переключения перерыва:', error);
        showNotification('Ошибка переключения перерыва', 'error');
    }
}

/**
 * Переключение обеда
 */
async function toggleLunch() {
    if (!currentUser || !currentWorkDay) return;
    
    try {
        const response = await fetch(`${API_BASE_URL}/employee/toggle-lunch?workDayId=${currentWorkDay.id}`, {
            method: 'POST'
        });
        
        if (response.ok) {
            currentWorkDay = await response.json();
            updateWorkDayUI();
            
            if (currentWorkDay.status === 'ON_LUNCH') {
                showNotification('Обед начался! Вернитесь через 1 час.', 'warning');
                // Запускаем таймер обеда
                startBreakTimer(60 * 60); // 60 минут
            } else {
                showNotification('Обед завершен. Приступайте к работе!', 'success');
                stopBreakTimer();
            }
        }
    } catch (error) {
        console.error('Ошибка переключения обеда:', error);
        showNotification('Ошибка переключения обеда', 'error');
    }
}

/**
 * Показ формы завершения рабочего дня
 */
function showEndWorkForm() {
    document.getElementById('end-work-form').classList.remove('hidden');
    document.getElementById('action-buttons').classList.add('hidden');
}

/**
 * Скрытие формы завершения рабочего дня
 */
function hideEndWorkForm() {
    document.getElementById('end-work-form').classList.add('hidden');
    document.getElementById('action-buttons').classList.remove('hidden');
}

/**
 * Предпросмотр загружаемых фотографий
 */
function handlePhotoPreview(event) {
    const files = event.target.files;
    const preview = document.getElementById('photo-preview');
    preview.innerHTML = '';
    
    if (files.length > 10) {
        showNotification('Можно загрузить максимум 10 фотографий', 'warning');
        event.target.value = '';
        return;
    }
    
    Array.from(files).forEach(file => {
        if (file.type.startsWith('image/')) {
            const reader = new FileReader();
            reader.onload = (e) => {
                const div = document.createElement('div');
                div.className = 'photo-item';
                div.innerHTML = `<img src="${e.target.result}" alt="Фото">`;
                preview.appendChild(div);
            };
            reader.readAsDataURL(file);
        }
    });
}

/**
 * Завершение рабочего дня с отправкой данных
 */
async function submitEndWork() {
    const comment = document.getElementById('work-comment').value.trim();
    const photosInput = document.getElementById('work-photos');
    
    if (!comment) {
        showNotification('Введите комментарий о выполненной работе', 'error');
        return;
    }
    
    if (photosInput.files.length === 0) {
        showNotification('Загрузите хотя бы одну фотографию', 'warning');
        return;
    }
    
    // Создаем FormData для отправки файлов
    const formData = new FormData();
    formData.append('comment', comment);
    
    Array.from(photosInput.files).forEach((file, index) => {
        formData.append(`photos`, file);
    });
    
    try {
        const response = await fetch(`${API_BASE_URL}/employee/end-work?workDayId=${currentWorkDay.id}`, {
            method: 'POST',
            body: formData
        });
        
        if (response.ok) {
            currentWorkDay = await response.json();
            updateWorkDayUI();
            hideEndWorkForm();
            document.getElementById('work-comment').value = '';
            photosInput.value = '';
            document.getElementById('photo-preview').innerHTML = '';
            showNotification('Рабочий день завершен! Данные отправлены.', 'success');
        } else {
            const error = await response.json();
            showNotification(error.message || 'Ошибка завершения дня', 'error');
        }
    } catch (error) {
        console.error('Ошибка завершения дня:', error);
        showNotification('Нет соединения с сервером', 'error');
    }
}

// ============================================
// Таймеры и напоминания
// ============================================

/**
 * Запуск таймера рабочего времени
 * @param {string} startTime - время начала в формате HH:mm:ss
 */
function startTimer(startTime) {
    const timerDisplay = document.getElementById('timer');
    const start = new Date();
    const [hours, minutes, seconds] = startTime.split(':').map(Number);
    const workStart = new Date();
    workStart.setHours(hours, minutes, seconds, 0);
    
    function updateTimer() {
        const now = new Date();
        const diff = now - workStart;
        
        // Учитываем перерывы и обед
        let adjustedDiff = diff;
        if (isOnBreak && breakStartTime) {
            adjustedDiff -= (now - breakStartTime);
        }
        if (isOnLunch && lunchStartTime) {
            adjustedDiff -= (now - lunchStartTime);
        }
        
        const totalSeconds = Math.floor(adjustedDiff / 1000);
        const h = Math.floor(totalSeconds / 3600);
        const m = Math.floor((totalSeconds % 3600) / 60);
        const s = totalSeconds % 60;
        
        timerDisplay.textContent = 
            `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`;
    }
    
    updateTimer();
    breakTimerInterval = setInterval(updateTimer, 1000);
}

/**
 * Остановка таймера
 */
function stopBreakTimer() {
    if (breakTimerInterval) {
        clearInterval(breakTimerInterval);
        breakTimerInterval = null;
    }
}

/**
 * Таймер для перерыва/обеда
 * @param {number} durationSeconds - длительность в секундах
 */
function startBreakTimer(durationSeconds) {
    stopBreakTimer();
    
    let remaining = durationSeconds;
    
    breakTimerInterval = setInterval(() => {
        remaining--;
        
        if (remaining <= 0) {
            showNotification('Время перерыва истекло! Пора приступать к работе.', 'warning');
            stopBreakTimer();
            // Автоматически завершаем перерыв/обед
            if (currentWorkDay?.status === 'ON_BREAK') {
                toggleBreak();
            } else if (currentWorkDay?.status === 'ON_LUNCH') {
                toggleLunch();
            }
            return;
        }
        
        const mins = Math.floor(remaining / 60);
        const secs = remaining % 60;
        
        // Показываем уведомление за 1 минуту до конца
        if (remaining === 60) {
            showNotification('До конца перерыва 1 минута', 'warning');
        }
    }, 1000);
}

/**
 * Таймер напоминаний о перерывах (каждый час в 50 минут)
 */
function startBreakReminderTimer() {
    setInterval(() => {
        const now = new Date();
        const minutes = now.getMinutes();
        
        // Напоминание о перерыве на 50 минуте
        if (minutes === BREAK_REMINDER_TIME) {
            showNotification('⏰ Через 10 минут перерыв! Подготовьтесь.', 'warning');
        }
        
        // Напоминание о начале работы на 10 минуте (после перерыва 50-60)
        if (minutes === 10) {
            showNotification('✅ Пора приступить к работе после перерыва!', 'success');
        }
    }, 60000); // Проверяем каждую минуту
}

// ============================================
// Отображение информации о сотруднике
// ============================================

/**
 * Показ экрана сотрудника
 */
function showEmployeeScreen() {
    document.getElementById('auth-screen').classList.add('hidden');
    document.getElementById('employee-screen').classList.remove('hidden');
    
    document.getElementById('employee-name').textContent = currentUser.fullName;
    document.getElementById('employee-position').textContent = currentUser.position;
    document.getElementById('employee-geozone').textContent = currentUser.geoZoneName || 'Не назначена';
    
    loadWeeklyTasks();
}

/**
 * Загрузка заданий на неделю
 */
async function loadWeeklyTasks() {
    if (!currentUser) return;
    
    try {
        const response = await fetch(`${API_BASE_URL}/employee/tasks?employeeId=${currentUser.id}`);
        
        if (response.ok) {
            const tasks = await response.json();
            displayTasks(tasks);
        }
    } catch (error) {
        console.error('Ошибка загрузки заданий:', error);
    }
}

/**
 * Отображение заданий
 * @param {Array} tasks - массив заданий
 */
function displayTasks(tasks) {
    const taskList = document.getElementById('task-list');
    const tasksCard = document.getElementById('tasks-card');
    
    if (tasks.length === 0) {
        tasksCard.classList.add('hidden');
        return;
    }
    
    tasksCard.classList.remove('hidden');
    taskList.innerHTML = '';
    
    const daysOfWeek = {
        MONDAY: 'Понедельник',
        TUESDAY: 'Вторник',
        WEDNESDAY: 'Среда',
        THURSDAY: 'Четверг',
        FRIDAY: 'Пятница',
        SATURDAY: 'Суббота',
        SUNDAY: 'Воскресенье'
    };
    
    tasks.forEach(task => {
        const li = document.createElement('li');
        li.className = 'task-item';
        li.innerHTML = `
            <span class="task-day">${daysOfWeek[task.dayOfWeek]}</span>
            <span class="task-description">${task.taskDescription}</span>
        `;
        taskList.appendChild(li);
    });
}

// ============================================
// Уведомления
// ============================================

/**
 * Показ уведомления пользователю
 * @param {string} message - текст уведомления
 * @param {string} type - тип: success, error, warning
 */
function showNotification(message, type = 'success') {
    // Удаляем предыдущие уведомления
    const existing = document.querySelectorAll('.notification');
    existing.forEach(n => n.remove());
    
    const notification = document.createElement('div');
    notification.className = `notification notification-${type}`;
    notification.textContent = message;
    
    document.body.appendChild(notification);
    
    // Автоудаление через 5 секунд
    setTimeout(() => {
        notification.remove();
    }, 5000);
}

// ============================================
// Экспорт функций для глобального доступа
// ============================================

window.startWork = startWork;
window.toggleBreak = toggleBreak;
window.toggleLunch = toggleLunch;
window.showEndWorkForm = showEndWorkForm;
window.hideEndWorkForm = hideEndWorkForm;
window.submitEndWork = submitEndWork;
window.logout = logout;
