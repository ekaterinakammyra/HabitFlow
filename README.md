# HabitFlow

HabitFlow — REST API для отслеживания привычек.
Проект разработан на Java и Spring Boot с использованием PostgreSQL.

## Возможности

* создание, получение, обновление и удаление привычек;
* поддержка ежедневных и еженедельных привычек;
* отметка выполнения привычки;
* возможность указать дату выполнения;
* проверка корректности даты выполнения;
* защита от повторного выполнения привычки;
* расчёт текущей и лучшей серии выполнений;
* статистика по привычке;
* валидация входных данных;
* централизованная обработка ошибок;
* unit-, repository-, controller- и integration-тесты.

## Технологии

* Java 21
* Spring Boot
* Spring Data JPA
* Hibernate
* PostgreSQL
* Maven
* JUnit
* Mockito
* Docker / Docker Compose

## Запуск проекта

### 1. Клонирование

```bash
git clone https://github.com/ekaterinakammyra/HabitFlow.git
cd HabitFlow
```

### 2. Настройка переменных окружения

Создайте файл `.env` в корне проекта:

```env
DB_URL=jdbc:postgresql://localhost:5432/habitflow
DB_USERNAME=habitflow
DB_PASSWORD=your_password
```

Пример конфигурации находится в `.env.example`.

### 3. Запуск PostgreSQL

```bash
docker compose up -d
```

### 4. Запуск приложения

Перед запуском загрузите переменные окружения:

```bash
set -a
source .env
set +a
```

Затем:

```bash
./mvnw spring-boot:run
```

После запуска API доступен по адресу:

```text
http://localhost:8080
```

## Тесты

Для запуска всех тестов:

```bash
./mvnw test
```

Проект содержит 86 тестов, включая unit-, controller-, repository- и integration-тесты.

## Основные API endpoints

### Habits

```text
POST   /api/habits
GET    /api/habits
GET    /api/habits/{id}
PUT    /api/habits/{id}
DELETE /api/habits/{id}
```

### Habit completions

```text
POST /api/habits/{habitId}/completions
GET  /api/habits/{habitId}/completions
GET  /api/habits/{habitId}/statistics
```

## Структура проекта

```text
controller   — REST-контроллеры
service      — бизнес-логика
repository   — работа с базой данных
entity       — JPA-сущности
dto          — объекты запросов и ответов
exception    — обработка исключений
config       — конфигурация приложения
```

## Статус

Проект находится в рабочем состоянии и продолжает развиваться.
