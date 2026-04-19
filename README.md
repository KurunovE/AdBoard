# AdBoard

Backend-сервис доски объявлений на `Spring Boot`.

Сервис предоставляет REST API для:
- регистрации, логина, refresh и logout через `Keycloak`
- управления объявлениями, категориями, комментариями и пользователями
- загрузки изображений объявлений в `Yandex Disk`
- отправки welcome email после успешной регистрации

## Возможности

- регистрация пользователя с созданием аккаунта в `Keycloak` и локальной записи в БД
- логин, refresh и logout через `Keycloak`
- JWT-защита API через `OAuth2 Resource Server`
- CRUD для объявлений
- фильтрация объявлений по `categoryId`, `authorId`, `minPrice`, `maxPrice`
- смена статуса объявления: `ACTIVE` / `CLOSED`
- CRUD для категорий с поддержкой родительской категории
- просмотр, создание и удаление комментариев
- загрузка, просмотр и удаление изображений объявления
- проверки доступа на уровне владельца ресурса и администратора
- единый JSON-формат ошибок через `GlobalExceptionHandler`
- Swagger / OpenAPI документация
- кэширование категорий и объявлений по `id`
- Flyway-миграции для схемы БД
- проверка покрытия тестов через `JaCoCo` с минимальным порогом `60%`

## Стек

- `Java 21`
- `Spring Boot 4.0.3`
- `Spring Web MVC`
- `Spring Security`
- `Spring OAuth2 Resource Server`
- `Spring Data JPA`
- `Spring Validation`
- `Spring Cache`
- `Spring Mail`
- `Thymeleaf`
- `PostgreSQL`
- `Flyway`
- `MapStruct`
- `Lombok`
- `Keycloak`
- `Yandex Disk API`
- `springdoc-openapi`
- `Gradle Kotlin DSL`
- `JUnit 5`
- `Mockito`

## Структура проекта

- `src/main/java/com/solarlab/adboard/controller` - REST-контроллеры
- `src/main/java/com/solarlab/adboard/service` - бизнес-логика
- `src/main/java/com/solarlab/adboard/repository` - доступ к БД
- `src/main/java/com/solarlab/adboard/model` - JPA-сущности
- `src/main/java/com/solarlab/adboard/dto` - DTO запросов и ответов
- `src/main/java/com/solarlab/adboard/mapper` - MapStruct-мапперы
- `src/main/java/com/solarlab/adboard/config` - security, properties и конфигурация
- `src/main/java/com/solarlab/adboard/exception` - обработка ошибок
- `src/main/resources/db/migration` - SQL-миграции Flyway
- `src/main/resources/templates/mail` - HTML-шаблоны email
- `src/test/java/com/solarlab/adboard` - unit и controller tests

## Безопасность и доступ

Без JWT доступны:
- `POST /v1/auth/**`
- `GET /v1/advertisements/**`
- `GET /v1/categories/**`
- `/swagger-ui.html`
- `/swagger-ui/**`
- `/v3/api-docs/**`

Остальные запросы требуют JWT.

Основные правила доступа:
- пользователь с ролью `USER` может создавать объявления и комментарии
- владелец объявления может обновлять, закрывать и удалять своё объявление
- владелец комментария может удалить свой комментарий
- владелец изображения может удалить своё изображение
- пользователь может читать и обновлять свой профиль
- администратор может удалять пользователей
- категории создаёт, обновляет и удаляет только администратор

## Поведение сервиса

- при создании объявления статус автоматически устанавливается в `ACTIVE`
- если `minPrice > maxPrice`, API возвращает `400 Bad Request`
- при удалении объявления сервис сначала удаляет связанные изображения из `Yandex Disk`, затем запись из БД
- изображения не хранятся локально: в БД сохраняются метаданные, а файл публикуется в `Yandex Disk`
- после успешной регистрации публикуется `UserRegisteredEvent`, а welcome email отправляется после commit транзакции

## Конфигурация

Основной конфиг находится в [src/main/resources/application.yaml](src/main/resources/application.yaml).

Минимальный набор переменных окружения:

```env
SERVER_PORT=8081

POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=adboard_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=password

KEYCLOAK_HOST=localhost
KEYCLOAK_PORT=9090
KEYCLOAK_REALM=adboard
KEYCLOAK_CLIENT_ID=adboard-client
KEYCLOAK_CLIENT_SECRET=your-client-secret
KEYCLOAK_ISSUER_URI=http://localhost:9090/realms/adboard
KEYCLOAK_JWK_SET_URI=http://localhost:9090/realms/adboard/protocol/openid-connect/certs
KEYCLOAK_ADMIN_USERNAME=admin
KEYCLOAK_ADMIN_PASSWORD=admin_password
KEYCLOAK_ADMIN_CLIENT_ID=admin-cli

KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=admin_password

YANDEX_DISK_TOKEN=your-yandex-disk-token

MAIL_HOST=smtp.example.com
MAIL_PORT=587
MAIL_USERNAME=no-reply@example.com
MAIL_PASSWORD=mail-password
```

## Локальный запуск

### 1. Поднять инфраструктуру

В репозитории есть `docker-compose.yaml` для локального запуска `PostgreSQL` и `Keycloak`:

```bash
docker compose up -d
```

По умолчанию:
- PostgreSQL: `localhost:5432`
- Keycloak: `localhost:9090`

### 2. Запустить приложение

Windows:

```powershell
.\gradlew.bat bootRun
```

macOS / Linux:

```bash
./gradlew bootRun
```

Сервис будет доступен по адресу `http://localhost:8081`.

## Сборка и тесты

Запуск тестов:

```powershell
.\gradlew.bat test
```

Сборка проекта:

```powershell
.\gradlew.bat build
```

HTML-отчёт JaCoCo:

```powershell
.\gradlew.bat jacocoTestReport
```

Проверка порога покрытия:

```powershell
.\gradlew.bat check
```

## API документация

Swagger UI:
- `http://localhost:8081/swagger-ui.html`
- `http://localhost:8081/swagger-ui/index.html`

OpenAPI JSON:
- `http://localhost:8081/v3/api-docs`

## Основные endpoints

### Auth

- `POST /v1/auth/register`
- `POST /v1/auth/login`
- `POST /v1/auth/refresh`
- `POST /v1/auth/logout`

### Advertisements

- `GET /v1/advertisements`
- `GET /v1/advertisements/{id}`
- `POST /v1/advertisements`
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
- `POST /v1/categories`
- `PUT /v1/categories/{id}`
- `DELETE /v1/categories/{id}`

### Comments

- `GET /v1/advertisements/{advertisementId}/comments`
- `POST /v1/advertisements/{advertisementId}/comments`
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

Пример успешного ответа:

```json
{
  "access_token": "<access_token>",
  "refresh_token": "<refresh_token>",
  "expires_in": 300,
  "refresh_expires_in": 1800,
  "token_type": "Bearer"
}
```

### Создание объявления

```http
POST /v1/advertisements
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

### Ошибка API

```json
{
  "message": "Advertisement with id 1 not found",
  "status": 404,
  "timestamp": "2026-04-13T21:00:00"
}
```

## База данных

Для схемы БД используются Flyway-миграции:

- `V1__create_tables.sql`
- `V2__create_indexes.sql`
- `V3__create_triggers.sql`

Hibernate работает в режиме `ddl-auto=validate`, поэтому схема должна соответствовать SQL-миграциям.
