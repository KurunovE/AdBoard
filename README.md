# AdBoard

Backend-сервис доски объявлений на `Spring Boot`.

Проект предоставляет REST API для:
- регистрации и логина через `Keycloak`
- управления объявлениями, категориями, комментариями и пользователями
- загрузки изображений объявлений в `Yandex Disk`

## Возможности

- регистрация пользователя с созданием аккаунта в `Keycloak`
- логин через `Keycloak` с получением JWT access token
- CRUD-операции для объявлений
- фильтрация объявлений по категории, автору и диапазону цены
- хранение статуса объявления: `ACTIVE` / `CLOSED`
- CRUD-операции для категорий
- просмотр и создание комментариев к объявлениям
- загрузка, просмотр списка и удаление изображений объявления
- owner-based доступ через `@PreAuthorize` и `SecurityUtils`
- единый JSON-формат ошибок через `GlobalExceptionHandler`
- Swagger / OpenAPI документация
- кэширование категорий и объявления по `id`

## Технологии

- `Java 21`
- `Spring Boot 4.0.3`
- `Spring Web MVC`
- `Spring Security`
- `Spring OAuth2 Resource Server`
- `Spring Data JPA`
- `Spring Validation`
- `Spring Cache`
- `PostgreSQL`
- `Flyway`
- `MapStruct`
- `Lombok`
- `Keycloak`
- `Yandex Disk API`
- `springdoc-openapi`
- `Gradle Kotlin DSL`

## Структура проекта

- `src/main/java/com/solarlab/adboard/controller` - REST-контроллеры
- `src/main/java/com/solarlab/adboard/service` - бизнес-логика
- `src/main/java/com/solarlab/adboard/repository` - доступ к БД
- `src/main/java/com/solarlab/adboard/model` - JPA-сущности
- `src/main/java/com/solarlab/adboard/dto` - DTO запросов и ответов
- `src/main/java/com/solarlab/adboard/mapper` - MapStruct-мапперы
- `src/main/java/com/solarlab/adboard/config` - security, converters, properties, rest clients
- `src/main/resources/db/migration` - SQL-миграции Flyway
- `src/test/java/com/solarlab/adboard` - unit и controller tests

## Архитектура и поведение

### Аутентификация

- `POST /v1/auth/register` создаёт пользователя в `Keycloak`, назначает роль `USER` и сохраняет локальную запись в БД
- `POST /v1/auth/login` получает access token из `Keycloak`
- API работает как `OAuth2 Resource Server` и валидирует JWT токены

### Авторизация

Без токена доступны:
- `POST /v1/auth/**`
- `GET /v1/advertisements/**`
- `GET /v1/categories/**`
- `/swagger-ui.html`
- `/swagger-ui/**`
- `/v3/api-docs/**`

Все остальные запросы требуют JWT.

Дополнительно используется проверка владельца ресурса:
- пользователь может читать и обновлять свой профиль
- владелец объявления может обновлять, закрывать и удалять своё объявление
- владелец объявления может загружать изображения к своему объявлению
- владелец комментария может удалить свой комментарий
- владелец изображения или администратор может удалить изображение
- администратор может создавать и удалять категории, а также удалять пользователей

### Объявления

- при создании объявления статус автоматически устанавливается в `ACTIVE`
- поддерживаются фильтры:
  - `categoryId`
  - `authorId`
  - `minPrice`
  - `maxPrice`
- если `minPrice > maxPrice`, API возвращает `400 Bad Request`
- при удалении объявления сначала удаляются связанные изображения из `Yandex Disk`, затем запись объявления из БД

### Изображения

- изображения не хранятся локально
- файлы загружаются в `Yandex Disk`
- API возвращает список всех изображений объявления, отсортированный по `sortOrder`

### Ошибки

Сервис возвращает ошибки в едином формате:

```json
{
  "message": "Advertisement with id 1 not found",
  "status": 404,
  "timestamp": "2026-04-13T21:00:00"
}
```

## Конфигурация

Основной конфиг:
- [application.yaml](/c:/Users/eegor/Desktop/project/AdBoard/src/main/resources/application.yaml)

Локальный профиль:
- [application-local.yaml](/c:/Users/eegor/Desktop/project/AdBoard/src/main/resources/application-local.yaml)

По умолчанию активен профиль:
- `local`

Сервис слушает порт:
- `8081`

Рекомендуется передавать секреты через переменные окружения или `.env`, а не хранить их в локальных yaml-файлах.

Минимальный набор переменных окружения:

```env
SERVER_PORT=8081

POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=adboard_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres

KEYCLOAK_PORT=9090
KEYCLOAK_ISSUER_URI=http://localhost:9090/realms/adboard
KEYCLOAK_JWK_SET_URI=http://localhost:9090/realms/adboard/protocol/openid-connect/certs

KEYCLOAK_AUTH_SERVER_URL=http://localhost:9090
KEYCLOAK_REALM=adboard
KEYCLOAK_CLIENT_ID=adboard-client
KEYCLOAK_CLIENT_SECRET=your-client-secret
KEYCLOAK_ADMIN_USERNAME=admin
KEYCLOAK_ADMIN_PASSWORD=admin
KEYCLOAK_ADMIN_CLIENT_ID=admin-cli

KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=admin

YANDEX_DISK_TOKEN=your-yandex-disk-token
```

## Локальный запуск

### 1. Поднять инфраструктуру

В проекте есть `docker-compose.yaml` для локального запуска `PostgreSQL` и `Keycloak`.

```bash
docker compose up -d
```

По умолчанию:
- PostgreSQL: `localhost:5432`
- Keycloak: `localhost:9090`

### 2. Запустить приложение

Linux / macOS:

```bash
./gradlew bootRun
```

Windows:

```powershell
.\gradlew.bat bootRun
```

Приложение будет доступно по адресу:
- `http://localhost:8081`

## Swagger / OpenAPI

Swagger UI:
- `http://localhost:8081/swagger-ui.html`
- `http://localhost:8081/swagger-ui/index.html`

OpenAPI JSON:
- `http://localhost:8081/v3/api-docs`

## Основные endpoints

### Auth

- `POST /v1/auth/login`
- `POST /v1/auth/register`

### Advertisements

- `GET /v1/advertisements`
- `GET /v1/advertisements/{id}`
- `POST /v1/advertisements/create`
- `PUT /v1/advertisements/{id}`
- `PATCH /v1/advertisements/{id}/status`
- `DELETE /v1/advertisements/{id}`

Фильтры для `GET /v1/advertisements`:
- `categoryId`
- `authorId`
- `minPrice`
- `maxPrice`

### Categories

- `GET /v1/categories`
- `GET /v1/categories/{id}`
- `POST /v1/categories/create`
- `DELETE /v1/categories/{id}`

### Comments

- `GET /v1/advertisements/{advertisementId}/comments`
- `POST /v1/advertisements/{advertisementId}/comments/create`
- `DELETE /v1/advertisements/{advertisementId}/comments/{id}`

### Images

- `GET /v1/advertisements/{advertisementId}/images`
- `POST /v1/advertisements/{advertisementId}/images/upload`
- `DELETE /v1/advertisements/{advertisementId}/images/{id}`

### Users

- `GET /v1/users/{id}`
- `PUT /v1/users/{id}`
- `DELETE /v1/users/{id}`

## Примеры запросов

### Регистрация

```http
POST /v1/auth/register
Content-Type: application/json

{
  "name": "Ivan Ivanov",
  "email": "ivan@example.com",
  "phone": "+79990000000",
  "password": "secret"
}
```

### Логин

```http
POST /v1/auth/login
Content-Type: application/json

{
  "email": "ivan@example.com",
  "password": "secret"
}
```

### Создание объявления

```http
POST /v1/advertisements/create
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "title": "iPhone 14",
  "description": "В хорошем состоянии",
  "price": 50000,
  "categoryId": 1
}
```

### Смена статуса объявления

```http
PATCH /v1/advertisements/1/status
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "status": "CLOSED"
}
```

### Получение изображений объявления

```http
GET /v1/advertisements/1/images
```

## База данных

Для миграций используется `Flyway`.

Миграции расположены в:
- `src/main/resources/db/migration`

Hibernate работает в режиме:
- `spring.jpa.hibernate.ddl-auto=validate`

Это значит, что схема не генерируется автоматически и должна соответствовать SQL-миграциям.

## Кэширование

Включено кэширование для:
- списка категорий
- категории по `id`
- объявления по `id`

## Тесты

Запуск всех тестов:

```bash
./gradlew test
```

Для Windows:

```powershell
.\gradlew.bat test
```

## Что важно знать

- проект использует `MapStruct` для маппинга сущностей и DTO
- `SecurityUtils` централизует owner-based проверки доступа
- `GlobalExceptionHandler` приводит ошибки к единому JSON-формату