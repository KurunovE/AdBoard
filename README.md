# AdBoard

Backend-сервис доски объявлений на `Spring Boot`.  
Проект поддерживает регистрацию и логин через `Keycloak`, CRUD для объявлений, категорий, комментариев и пользователей, а также загрузку изображений в `Yandex Disk`.

## Возможности

- регистрация и логин пользователей
- JWT-аутентификация через `Keycloak`
- создание, редактирование, закрытие и удаление объявлений
- категории объявлений
- комментарии к объявлениям
- загрузка и удаление изображений
- кэширование категорий и объявления по `id`
- Swagger / OpenAPI документация

## Технологии

- `Java 21`
- `Spring Boot 4`
- `Spring Web MVC`
- `Spring Security`
- `Spring OAuth2 Resource Server`
- `Spring Data JPA`
- `PostgreSQL`
- `Flyway`
- `MapStruct`
- `Lombok`
- `Keycloak`
- `Yandex Disk API`
- `springdoc-openapi`

## Структура

- `controller` — REST endpoints
- `service` — бизнес-логика
- `repository` — доступ к БД
- `model` — JPA-сущности
- `dto` — запросы и ответы API
- `mapper` — преобразование сущностей и DTO
- `config` — security, cache, rest clients и прочие настройки

## Запуск

### 1. Поднять инфраструктуру

В проекте есть `docker-compose.yaml` для локального запуска `PostgreSQL` и `Keycloak`.

```bash
docker compose up -d
```

По умолчанию используются:

- PostgreSQL: `localhost:5432`
- Keycloak: `localhost:9090`

### 2. Настроить конфигурацию приложения

Основной конфиг находится в:

- [application.yaml](src/main/resources/application.yaml)

По умолчанию активен профиль:

- `local`

Локальные значения можно хранить в:

- `src/main/resources/application-local.yaml`

Или передавать через переменные окружения.

Минимально нужны:

```env
POSTGRES_HOST=localhost
POSTGRES_PORT=5432
POSTGRES_DB=adboard_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres

KEYCLOAK_ISSUER_URI=http://localhost:9090/realms/adboard
KEYCLOAK_JWK_SET_URI=http://localhost:9090/realms/adboard/protocol/openid-connect/certs
KEYCLOAK_AUTH_SERVER_URL=http://localhost:9090
KEYCLOAK_REALM=adboard
KEYCLOAK_CLIENT_ID=adboard-client
KEYCLOAK_CLIENT_SECRET=your-client-secret
KEYCLOAK_ADMIN_USERNAME=admin
KEYCLOAK_ADMIN_PASSWORD=admin
KEYCLOAK_ADMIN_CLIENT_ID=admin-cli

YANDEX_DISK_TOKEN=your-yandex-token
```

### 3. Запустить приложение

```bash
./gradlew bootRun
```

Для Windows:

```powershell
.\gradlew.bat bootRun
```

Приложение стартует на:

- `http://localhost:8081`

## Swagger

Swagger UI доступен без авторизации:

- `http://localhost:8081/swagger-ui.html`
- `http://localhost:8081/swagger-ui/index.html`

OpenAPI JSON:

- `http://localhost:8081/v3/api-docs`

## Безопасность

Открыты без авторизации:

- `POST /v1/auth/login`
- `POST /v1/auth/register`
- `GET /v1/advertisements/**`
- `GET /v1/categories/**`
- Swagger endpoints

Остальные endpoints требуют JWT access token.

Дополнительно используется проверка владельца ресурса через `@PreAuthorize`, например:

- владелец объявления может редактировать и удалять своё объявление
- владелец комментария может удалить свой комментарий
- владелец изображения может удалить своё изображение

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

- `POST /v1/images/advertisements/{advertisementId}/upload`
- `DELETE /v1/images/{id}`

### Users

- `GET /v1/users/{id}`
- `PUT /v1/users/{id}`
- `DELETE /v1/users/{id}`

## База данных

Для миграций используется `Flyway`.  
Схема создаётся из SQL-миграций в:

- `src/main/resources/db/migration`

В `application.yaml` используется:

- `spring.jpa.hibernate.ddl-auto=validate`

Это значит, что Hibernate не меняет схему автоматически, а только проверяет её соответствие.

## Изображения

Изображения хранятся не локально, а в `Yandex Disk`.

При удалении объявления:

- удаляются связанные изображения в БД
- выполняется удаление файлов из Yandex Disk
- удаляются связанные комментарии

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

## Полезно знать

- проект использует `MapStruct` для маппинга DTO
- `SecurityUtils` используется для owner-based доступа
- `GlobalExceptionHandler` приводит ошибки к единому JSON-формату
