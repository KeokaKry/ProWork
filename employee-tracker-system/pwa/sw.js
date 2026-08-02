/**
 * Service Worker для PWA приложения WorkTracker
 * Обеспечивает офлайн-работу и кэширование ресурсов
 */

const CACHE_NAME = 'worktracker-v1';
const urlsToCache = [
  '/',
  '/index.html',
  '/manifest.json',
  '/styles.css',
  '/app.js'
];

// Установка Service Worker и кэширование ресурсов
self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => {
        console.log('Открыто кэш-хранилище');
        return cache.addAll(urlsToCache);
      })
      .catch(err => {
        console.error('Ошибка кэширования:', err);
      })
  );
});

// Активация Service Worker и очистка старых кэшей
self.addEventListener('activate', event => {
  event.waitUntil(
    caches.keys().then(cacheNames => {
      return Promise.all(
        cacheNames.map(cacheName => {
          if (cacheName !== CACHE_NAME) {
            console.log('Удаление старого кэша:', cacheName);
            return caches.delete(cacheName);
          }
        })
      );
    })
  );
});

// Перехват запросов и обслуживание из кэша или сети
self.addEventListener('fetch', event => {
  // Для API запросов всегда используем сеть
  if (event.request.url.includes('/api/')) {
    event.respondWith(
      fetch(event.request)
        .catch(() => {
          // Если нет сети, возвращаем ошибку
          return new Response(JSON.stringify({
            error: 'Нет соединения с сервером',
            offline: true
          }), {
            status: 503,
            headers: { 'Content-Type': 'application/json' }
          });
        })
    );
    return;
  }

  // Для статических ресурсов используем стратегию "Cache First"
  event.respondWith(
    caches.match(event.request)
      .then(response => {
        // Если есть в кэше - возвращаем из кэша
        if (response) {
          return response;
        }
        // Иначе загружаем из сети
        return fetch(event.request)
          .then(response => {
            // Проверяем корректность ответа
            if (!response || response.status !== 200 || response.type !== 'basic') {
              return response;
            }
            // Клонируем ответ для сохранения в кэш
            const responseToCache = response.clone();
            caches.open(CACHE_NAME)
              .then(cache => {
                cache.put(event.request, responseToCache);
              });
            return response;
          });
      })
      .catch(() => {
        // Если ничего не получилось, возвращаем офлайн страницу
        return caches.match('/index.html');
      })
  );
});

// Обработка сообщений от основного приложения
self.addEventListener('message', event => {
  if (event.data && event.data.type === 'SKIP_WAITING') {
    self.skipWaiting();
  }
});
